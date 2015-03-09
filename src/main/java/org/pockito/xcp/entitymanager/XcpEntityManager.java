package org.pockito.xcp.entitymanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.entitymanager.cache.CacheElement;
import org.pockito.xcp.entitymanager.cache.CacheWrapper;
import org.pockito.xcp.entitymanager.cache.NoopSessionCache;
import org.pockito.xcp.entitymanager.cache.SessionCache;
import org.pockito.xcp.entitymanager.cache.SessionCacheWrapper;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsEntityManager;
import org.pockito.xcp.repository.DmsException;
import org.pockito.xcp.repository.DmsQuery;
import org.pockito.xcp.repository.DmsTypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfValue;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfValue;
import com.google.common.base.Strings;

public class XcpEntityManager implements DmsEntityManager {

	private static final String DMS_RELATION_TYPE = "dm_relation";

	private Logger logger = LoggerFactory.getLogger(XcpEntityManager.class);

	private final XcpEntityManagerFactory factory;
	private final DctmDriver dctmDriver;
	private final Map<String, ?> properties;
	private final String repository;
	private final SessionCacheWrapper sessionCache;

	public XcpEntityManager(XcpEntityManagerFactory dmsEntityManagerFactoryImpl, Map<String, ?> map,
			DctmDriver dctmDriver) {
		this.factory = dmsEntityManagerFactoryImpl;
		this.dctmDriver = dctmDriver;
		this.properties = map;
		this.repository = (String) map.get(PropertyConstants.Repository);

		// create the session cache
		if (factory().isSessionLess()) {
			this.sessionCache = new NoopSessionCache();
		} else {
			CacheWrapper<String, CacheElement> underCache = factory().getFirstLevelCache();
			this.sessionCache = new SessionCache(this, underCache);
		}
	}

	public <T> AnnotationInfo getAnnotationInfo(Class<T> entityClass) {
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entityClass);
		return ai;
	}

	public <T> T find(Class<T> entityClass, Object primaryKey) {

		// Note: primary key is unique through all entities (as r_object_id)
		@SuppressWarnings("unchecked")
		T bean = (T) cacheGet(primaryKey.toString());
		if (bean != null) {
			return bean;
		}
		AnnotationInfo ai = getAnnotationInfo(entityClass);
		return doFind(entityClass, ai, primaryKey);
	}

	private <B> B doFind(Class<B> entityClass, AnnotationInfo ai, Object primaryKey) {

		B newInstance = null;
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			IDfPersistentObject dmsObj = getDmsObj(dfSession, ai, primaryKey);

			if (dmsObj != null) {

				logger.debug("Found dms object {}", primaryKey.toString());

				newInstance = entityClass.newInstance();

				Collection<PersistentProperty> persistentProperties = ai.getPersistentProperties();
				for (PersistentProperty field : persistentProperties) {

					if (field.isAttribute()) {

						logger.trace("reading property {} bound to {}", field.getFieldName(), field.getAttributeName());
						if (field.isRepeating()) {
							List<Object> values = getRepeatingValues(dmsObj, field);
							field.setProperty(newInstance, values);
						} else {
							IDfValue dfValue = dmsObj.getValue(field.getAttributeName());
							field.setProperty(newInstance, dfValue);
						}

					} else if ((field.isChild() || field.isParent())
							&& (ai.getTypeCategory() == XcpTypeCategory.RELATION)) {

						// relation field

						logger.trace("OneToOne relation {}", field.getFieldName());
						Class<?> beanClass = field.getRawClass();
						if (beanClass.isPrimitive()) {
							throw new XcpPersistenceException("cannot handle a primitive field: "
									+ field.getFieldName());
						}
						String foreignKey = field.isChild() ? PersistentProperty.DMS_ATTR_CHILD_ID
								: PersistentProperty.DMS_ATTR_PARENT_ID;
						IDfId externalKey = dmsObj.getId(foreignKey);
						Object bean = doFind(beanClass, getAnnotationInfo(beanClass), externalKey);
						field.setProperty(newInstance, bean);

					} else if (field.isParentFolder()) {

						// parent folder
						logger.trace("reading parent folder property {}", field.getFieldName());
						if (field.isAssignableFrom(String.class)) {
							if (field.isRepeating()) {
								List<Object> values = new ArrayList<Object>();
								final String attributeName = field.getAttributeName();
								final int valueCount = dmsObj.getValueCount(attributeName);
								for (int index = 0; index < valueCount; index++) {
									final IDfValue dfValue = getFolderPathAsDfValue(dfSession, dmsObj, index);
									values.add(dfValue.asString());
								}
								field.setProperty(newInstance, values);
							} else {
								final IDfValue dfValue = getFolderPathAsDfValue(dfSession, dmsObj, 0);
								field.setProperty(newInstance, dfValue);
							}
						} else {
							logger.warn("Field {} type is not a String. Cannot read folder path", field.getFieldName());
						}
					}
				}

				// cache the instance
				cachePut(newInstance, ai);
			}
		} catch (Exception e) {
			throw new XcpPersistenceException(e);
		} finally {
			releaseSession(dfSession);
		}
		return newInstance;
	}

	private IDfValue getFolderPathAsDfValue(IDfSession dfSession, IDfPersistentObject dmsObj, int index)
			throws DfException {
		final IDfId dfId = dmsObj.getId(PersistentProperty.DMS_ATTR_FOLDER_ID);
		final IDfFolder parentFolder = (IDfFolder) dfSession.getObject(dfId);
		final IDfValue dfValue = new DfValue(parentFolder.getFolderPath(index));
		return dfValue;
	}

	private List<Object> getRepeatingValues(IDfPersistentObject dmsObj, PersistentProperty field) throws DfException {
		List<Object> values = new ArrayList<Object>();
		String attributeName = field.getAttributeName();
		int valueCount = dmsObj.getValueCount(attributeName);
		for (int i = 0; i < valueCount; i++) {
			values.add(field.dfValueToObject(dmsObj.getRepeatingValue(attributeName, i)));
		}
		return values;
	}

	public void persist(Object entity) {
		// get annotation info
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entity);
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			final IDfPersistentObject dmsObj;
			if (ai.getTypeCategory() == XcpTypeCategory.RELATION) {
				dmsObj = createRelationObject(dfSession, entity, ai);
			} else {
				dmsObj = createDmsObject(dfSession, entity, ai);
			}

			// transfer bean fields to dctm attributes
			for (PersistentProperty field : ai.getPersistentProperties()) {
				if (field.isGeneratedValue() || field.isReadonly() || field.isParent() || field.isChild()) {
					continue;
				}

				Object fieldValue = field.getProperty(entity);

				if (fieldValue != null) {
					if (field.isParentFolder() && dmsObj.hasAttr(PersistentProperty.DMS_ATTR_FOLDER_ID)) {
						updateFolderLink(dfSession, (IDfSysObject) dmsObj, fieldValue);
					} else {
						if (field.isRepeating()) {
							String attributeName = field.getAttributeName();
							dmsObj.truncate(attributeName, 0);
							int valueIndex = 0;
							for (Object each : (Collection<?>) fieldValue) {
								dmsObj.setRepeatingValue(attributeName, valueIndex++, field.objToDfValue(each));
							}
						} else {
							dmsObj.setValue(field.getAttributeName(), field.objToDfValue(fieldValue));
						}
					}
				}
			}

			// save dms object
			dmsObj.save();

			// update system generated value
			dms2Entity(dmsObj, entity, ai);

			// refresh the cache
			cachePut(entity, ai);

		} catch (XcpPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new XcpPersistenceException(e);
		} finally {
			releaseSession(dfSession);
		}
	}

	private void updateFolderLink(IDfSession dfSession, IDfSysObject dmsObj, Object fieldValue) throws DfException {

		Class<?> clazz = fieldValue.getClass();
		final boolean isFieldCollection = Collection.class.isAssignableFrom(clazz);

		// unlink the object from all its folders
		int valueCount = dmsObj.getValueCount(PersistentProperty.DMS_ATTR_FOLDER_ID);
		for (int index = valueCount - 1; index >= 0; index--) {
			IDfId folderId = dmsObj.getRepeatingId(PersistentProperty.DMS_ATTR_FOLDER_ID, index);
			dmsObj.unlink(folderId.getId());
		}

		// then link to the specified folders
		if (isFieldCollection) {
			@SuppressWarnings("unchecked")
			Collection<Object> folderPaths = (Collection<Object>) fieldValue;
			for (Object path : folderPaths) {
				dmsObj.link(path.toString());
			}
		} else {
			dmsObj.link(fieldValue.toString());
		}
	}

	private IDfPersistentObject createDmsObject(IDfSession dfSession, Object entity, AnnotationInfo ai)
			throws DfException {
		// create a DMS object if needed
		IDfPersistentObject dmsObj = null;
		String identifier = (String) ai.getIdMethod().getProperty(entity);
		if (!Strings.isNullOrEmpty(identifier)) {
			dmsObj = getDmsObj(dfSession, ai, identifier);
		}
		if (dmsObj == null) {
			dmsObj = dfSession.newObject(ai.getDmsType());
			logger.debug("created new object: {}", dmsObj.getObjectId().toString());
		}

		return dmsObj;
	}

	private IDfPersistentObject createRelationObject(IDfSession dfSession, Object entity, AnnotationInfo ai)
			throws DfException {

		// retrieve the parent_id and child_id
		PersistentProperty parentMethod = ai.getParentMethod();
		if (parentMethod == null) {
			throw new XcpPersistenceException("Relation must be annotated with @Parent");
		}
		PersistentProperty childMethod = ai.getChildMethod();
		if (childMethod == null) {
			throw new XcpPersistenceException("Relation must be annotated with @Child");
		}
		final Object parentObject = parentMethod.getProperty(entity);
		final AnnotationInfo parentAi = factory().getAnnotationManager().getAnnotationInfo(parentObject);
		final String parentObjectId = (String) parentAi.getIdMethod().getProperty(parentObject);
		final Object childObject = childMethod.getProperty(entity);
		final AnnotationInfo childAi = factory().getAnnotationManager().getAnnotationInfo(childObject);
		final String childObjectId = (String) childAi.getIdMethod().getProperty(childObject);

		IDfPersistentObject dmsParent = getDmsObj(dfSession, parentAi.getDmsType(), parentAi.getIdMethod()
				.getAttributeName(), parentObjectId, -1);
		if (dmsParent == null) {
			throw new XcpPersistenceException("Object with identifier {} not found");
		}
		IDfRelation relationObj = dmsParent.addChildRelative(ai.getDmsType(), new DfId(childObjectId), null, false,
				ai.getLabel());

		return relationObj;
	}

	private final void cachePut(Object entity, AnnotationInfo ai) {
		sessionCache().put(entity, ai);
	}

	private final Object cacheGet(String key) {
		return sessionCache().get(key);
	}

	private final void dms2Entity(IDfPersistentObject dmsObj, Object entity, AnnotationInfo ai) throws DfException {
		for (PersistentProperty field : ai.getPersistentProperties()) {
			if (field.isGeneratedValue() || field.isReadonly()) {
				if (field.isRepeating()) {
					List<Object> values = new ArrayList<Object>();
					String attributeName = field.getAttributeName();
					int valueCount = dmsObj.getValueCount(attributeName);
					for (int i = 0; i < valueCount; i++) {
						values.add(field.dfValueToObject(dmsObj.getRepeatingValue(attributeName, i)));
					}
					field.setProperty(entity, values);
				} else {
					IDfValue dfValue = dmsObj.getValue(field.getAttributeName());
					field.setProperty(entity, dfValue);
				}
			}
		}
	}

	IDfPersistentObject getDmsObj(IDfSession dfSession, AnnotationInfo ai, Object objectId) throws DfException {
		return getDmsObj(dfSession, ai, objectId, -1);
	}

	public IDfPersistentObject getDmsObj(IDfSession dfSession, AnnotationInfo ai, Object objectId, int vstamp)
			throws DfException {
		if (ai.getTypeCategory() == XcpTypeCategory.RELATION) {
			return getDmsRelationObj(dfSession, ai, objectId, vstamp);
		} else {
			return getDmsObj(dfSession, ai.getDmsType(), ai.getIdMethod().getAttributeName(), objectId, vstamp);
		}
	}

	private IDfPersistentObject getDmsObj(IDfSession dfSession, String objectType, String keyIdentifier,
			Object objectId, int vstamp) throws DfException {
		StringBuilder buffer = new StringBuilder();
		buffer.append(objectType);
		buffer.append(" where ");
		buffer.append(keyIdentifier).append(" = '").append(objectId.toString()).append("'");
		if (vstamp >= 0) {
			buffer.append(" and i_vstamp = ").append(vstamp);
		}

		// retrieve the persisted object
		if (logger.isDebugEnabled()) {
			logger.debug("find by qualification: {}", buffer.toString());
		}
		IDfPersistentObject dmsObj = dfSession.getObjectByQualification(buffer.toString());
		return dmsObj;
	}

	private IDfRelation getDmsRelationObj(IDfSession dfSession, AnnotationInfo ai, Object objectId, int vstamp)
			throws DfException {
		StringBuilder buffer = new StringBuilder();
		buffer.append(DMS_RELATION_TYPE);
		buffer.append(" where ");
		buffer.append("relation_name = '").append(ai.getDmsType()).append("'");
		buffer.append(" and r_object_id = '").append(objectId.toString()).append("'");
		if (vstamp >= 0) {
			buffer.append(" and i_vstamp = ").append(vstamp);
		}

		// retrieve the persisted object
		if (logger.isDebugEnabled()) {
			logger.debug("find by qualification: {}", buffer.toString());
		}
		IDfRelation dmsObj = (IDfRelation) dfSession.getObjectByQualification(buffer.toString());
		return dmsObj;
	}

	public XcpEntityManagerFactory factory() {
		return factory;
	}

	public IDfSession getSession() throws DmsException {
		return getDctmDriver().getSession();
	}

	public void releaseSession(IDfSession dfSession) {
		if (dfSession != null) {
			getDctmDriver().releaseSession(dfSession);
		}
	}

	protected DctmDriver getDctmDriver() {
		return dctmDriver;
	}

	public Map<String, ?> getProperties() {
		return properties;
	}

	public String repository() {
		return repository;
	}

	@Override
	public void remove(Object entity) {
		throw new NotYetImplemented();
	}

	@Override
	public <T> DmsTypedQuery<T> createNamedQuery(String qlString) {
		throw new NotYetImplemented();
	}

	@Override
	public DmsQuery createNativeQuery(String dqlString) {
		return new XcpQuery(this, dqlString);
	}

	@Override
	public <T> DmsTypedQuery<T> createNativeQuery(String dqlString, Class<T> entityClass) {
		return new XcpTypedQuery<T>(this, dqlString, entityClass, true);
	}

	SessionCacheWrapper sessionCache() {
		return sessionCache;
	}

}
