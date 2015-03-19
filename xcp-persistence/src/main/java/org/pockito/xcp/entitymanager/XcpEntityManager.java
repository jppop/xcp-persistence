package org.pockito.xcp.entitymanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.entitymanager.api.DctmDriver;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsException;
import org.pockito.xcp.entitymanager.api.DmsQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.entitymanager.api.Transaction;
import org.pockito.xcp.entitymanager.cache.CacheElement;
import org.pockito.xcp.entitymanager.cache.CacheWrapper;
import org.pockito.xcp.entitymanager.cache.NoopSessionCache;
import org.pockito.xcp.entitymanager.cache.SessionCache;
import org.pockito.xcp.entitymanager.cache.SessionCacheWrapper;
import org.pockito.xcp.entitymanager.query.XcpBeanQuery;
import org.pockito.xcp.entitymanager.query.XcpQuery;
import org.pockito.xcp.entitymanager.query.XcpTypedQuery;
import org.pockito.xcp.exception.XcpPersistenceException;
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
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class XcpEntityManager implements DmsEntityManager {

	private static final String DMS_RELATION_TYPE = "dm_relation";

	private Logger logger = LoggerFactory.getLogger(XcpEntityManager.class);

	private final XcpEntityManagerFactory factory;
	private final DctmDriver dctmDriver;
	private final Map<String, ?> properties;
	private final String repository;
	private final SessionCacheWrapper sessionCache;
	private Transaction currentTx = null;

	public XcpEntityManager(XcpEntityManagerFactory dmsEntityManagerFactoryImpl, Map<String, ?> props,
			DctmDriver dctmDriver) {
		this.factory = dmsEntityManagerFactoryImpl;
		this.dctmDriver = dctmDriver;
		this.properties = props;
		this.repository = (String) props.get(PropertyConstants.Repository);

		// create the session cache
		if (factory().isSessionLess()) {
			this.sessionCache = new NoopSessionCache();
		} else {
			CacheWrapper<String, CacheElement> underCache = factory().getFirstLevelCache();
			this.sessionCache = new SessionCache(this, underCache);
		}
	}

	@Override
	public <T> MetaData getMetaData(Class<T> entityClass) {
		return getAnnotationInfo(entityClass);
	}

	public <T> AnnotationInfo getAnnotationInfo(Class<T> entityClass) {
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entityClass);
		return ai;
	}

	@Override
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

	@Override
	public void remove(Object entity) {

		// get the id of the object to be deleted
		final AnnotationInfo ai = getAnnotationInfo(entity.getClass());
		final String objectId = (String) ai.getIdMethod().getProperty(entity);
		if (Strings.isNullOrEmpty(objectId)) {
			throw new IllegalStateException("An id must be provided");
		}
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			// retrieve the object
			final IDfPersistentObject dmsObj = getDmsObj(dfSession, ai, objectId);

			if (dmsObj != null) {

				// destroy the object
				dmsObj.destroy();

				// remove the entity from the cache
				sessionCache().remove(objectId);
			}

		} catch (Exception e) {
			throw new XcpPersistenceException(e);
		} finally {
			releaseSession(dfSession);
		}
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

	@Override
	public void persist(Object entity) {
		// get annotation info
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entity);
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			IDfPersistentObject dmsObj = null;
			// retrieve the object
			final String objectId = (String) ai.getIdMethod().getProperty(entity);
			if (!Strings.isNullOrEmpty(objectId)) {
				dmsObj = getDmsObj(dfSession, ai, objectId);
			}

			if (dmsObj == null) {
				// create the dms object
				if (ai.getTypeCategory() == XcpTypeCategory.RELATION) {
					dmsObj = createRelationObject(dfSession, entity, ai);
				} else {
					dmsObj = createDmsObject(dfSession, entity, ai);
				}
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

	@Override
	public void addAttachment(Object entity, String filename, String contentType) {
		// get annotation info
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entity);
		final String objectId = (String) ai.getIdMethod().getProperty(entity);
		if (Strings.isNullOrEmpty(objectId)) {
			throw new IllegalStateException("An id must be provided");
		}
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			if (ai.getTypeCategory() == XcpTypeCategory.RELATION || ai.getTypeCategory() == XcpTypeCategory.FOLDER) {
				throw new IllegalStateException("Cannot attach content to relation or folder objects");
			}

			// retrieve the object from the dms repository
			final IDfSysObject dmsObj = (IDfSysObject) getDmsObj(dfSession, ai, objectId);

			if (dmsObj == null) {
				throw new XcpPersistenceException("object not found in the repository");
			}

			// set the content type
			dmsObj.setContentType(contentType);
			// set content
			dmsObj.setFile(filename);

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

	@Override
	public String getAttachment(final Object entity, final String filename) {

		String contentFile = null;

		// get annotation info
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entity);
		final String objectId = (String) ai.getIdMethod().getProperty(entity);
		if (Strings.isNullOrEmpty(objectId)) {
			throw new IllegalStateException("An id must be provided");
		}
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			if (ai.getTypeCategory() == XcpTypeCategory.RELATION || ai.getTypeCategory() == XcpTypeCategory.FOLDER) {
				throw new IllegalStateException("This entity does not have any content");
			}

			// retrieve the object from the dms repository
			final IDfSysObject dmsObj = (IDfSysObject) getDmsObj(dfSession, ai, objectId);

			if (dmsObj == null) {
				throw new XcpPersistenceException("object not found in the repository");
			}

			// get the content
			contentFile = dmsObj.getFile(filename);

		} catch (XcpPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new XcpPersistenceException(e);
		} finally {
			releaseSession(dfSession);
		}

		return contentFile;
	}

	@Override
	public Transaction getTransaction() {
		if (this.currentTx == null) {
			this.currentTx = new DmsTransaction(getDctmDriver().getSessionManager());
		}
		return this.currentTx;
	}

	@Override
	public void close() {
		sessionCache().clear();
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
		final AnnotationInfo parentAi = getAnnotationInfo(parentObject.getClass());
		final String parentObjectId = (String) parentAi.getIdMethod().getProperty(parentObject);
		final Object childObject = childMethod.getProperty(entity);
		final AnnotationInfo childAi = getAnnotationInfo(childObject.getClass());
		final String childObjectId = (String) childAi.getIdMethod().getProperty(childObject);

		IDfPersistentObject dmsParent = getDmsObj(dfSession, parentAi.getDmsType(), parentAi.getIdMethod()
				.getAttributeName(), parentObjectId, -1);
		if (dmsParent == null) {
			throw new XcpPersistenceException("Parent object with identifier {} not found");
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
		if (Strings.isNullOrEmpty(objectId.toString())) {
			return null;
		}
		if (ai.getTypeCategory() == XcpTypeCategory.RELATION) {
//			return getDmsRelationObj(dfSession, ai, objectId, vstamp);
			return getDmsObj(dfSession, DMS_RELATION_TYPE, PersistentProperty.DMS_ATTR_OBJECT_ID, objectId, vstamp);
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

	public DctmDriver getDctmDriver() {
		return dctmDriver;
	}

	public Map<String, ?> getProperties() {
		return properties;
	}

	public String repository() {
		return repository;
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
	public <T> DmsTypedQuery<T> createNativeQuery(String dqlQuery, Class<T> entityClass) {
		return new XcpTypedQuery<T>(this, dqlQuery, true);
	}

	public <T> DmsBeanQuery<T> createBeanQuery(Class<T> entityClass) {
		return new XcpBeanQuery<T>(this, entityClass);
	}
	
	@Override
	public <T, R> DmsTypedQuery<T> createChildRelativesQuery(Object parent, Class<R> relationClass,
			Class<T> childClass, String optionalDqlFilter) {
		final String dqlQuery = buildChildRelative(parent, relationClass, childClass,
				Optional.fromNullable(optionalDqlFilter));
		return new XcpTypedQuery<T>(this, dqlQuery, true);
	}

	@Override
	public <T, R> DmsTypedQuery<T> createParentRelativesQuery(Object child, Class<R> relationClass,
			Class<T> parentClass, String optionalDqlFilter) {
		final String dqlQuery = buildParentRelative(child, relationClass, parentClass,
				Optional.fromNullable(optionalDqlFilter));
		return new XcpTypedQuery<T>(this, dqlQuery, true);
	}

	private <T, R> String buildChildRelative(Object parent, Class<R> relationClass, Class<T> childClass,
			Optional<String> dqlFilter) {

		final AnnotationInfo relationAi = getAnnotationInfo(relationClass);
		final AnnotationInfo parentAi = getAnnotationInfo(parent.getClass());
		final AnnotationInfo childAi = getAnnotationInfo(childClass);
		final String parentObjectId = (String) parentAi.getIdMethod().getProperty(parent);

		final StringBuffer buffer = new StringBuffer();
		buffer.append("select c.r_object_id").append(" from dm_relation r, ").append(childAi.getDmsType()).append(" c")
				.append(" where r.relation_name = '").append(relationAi.getDmsType()).append("'")
				.append(" and r.parent_id = '").append(parentObjectId).append("'")
				.append(" and r.child_id = c.r_object_id");
		if (dqlFilter.isPresent()) {
			buffer.append(" ").append(dqlFilter.get());
		}
		return buffer.toString();
	}

	private <T, R> String buildParentRelative(Object child, Class<R> relationClass, Class<T> parentClass,
			Optional<String> dqlFilter) {

		final AnnotationInfo relationAi = getAnnotationInfo(relationClass);
		final AnnotationInfo childAi = getAnnotationInfo(child.getClass());
		final AnnotationInfo parentAi = getAnnotationInfo(parentClass);
		final String childObjectId = (String) childAi.getIdMethod().getProperty(child);

		final StringBuffer buffer = new StringBuffer();
		buffer.append("select p.r_object_id").append(" from dm_relation r, ").append(parentAi.getDmsType())
				.append(" p").append(" where r.relation_name = '").append(relationAi.getDmsType()).append("'")
				.append(" and r.child_id = '").append(childObjectId).append("'")
				.append(" and r.parent_id = p.r_object_id");
		if (dqlFilter.isPresent()) {
			buffer.append(" ").append(dqlFilter.get());
		}
		return buffer.toString();
	}

	SessionCacheWrapper sessionCache() {
		return sessionCache;
	}

}
