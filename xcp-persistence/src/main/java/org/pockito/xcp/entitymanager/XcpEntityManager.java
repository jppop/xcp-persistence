package org.pockito.xcp.entitymanager;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
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
import org.pockito.xcp.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfValue;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfValue;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class XcpEntityManager implements DmsEntityManager {

	public static final String DMS_RELATION_TYPE = "dm_relation";
	public static final String DOCUMENTUM_NAMESPACE = "dm";

	private Logger logger = LoggerFactory.getLogger(XcpEntityManager.class);

	private final XcpEntityManagerFactory factory;
	private final DctmDriver dctmDriver;
	private final Map<String, ?> properties;
	private final String repository;
	private final SessionCacheWrapper sessionCache;
	private Transaction currentTx = null;

	public XcpEntityManager(XcpEntityManagerFactory dmsEntityManagerFactoryImpl, Map<String, ?> props, DctmDriver dctmDriver) {
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
		checkNotNull(primaryKey);

		// Note: primary key is unique through all entities (as r_object_id)
		@SuppressWarnings("unchecked")
		T bean = (T) cacheGet(cacheGetKey(entityClass, primaryKey.toString()));
		if (bean != null) {
			return bean;
		}
		AnnotationInfo ai = getAnnotationInfo(entityClass);
		return doFind(entityClass, ai, primaryKey);
	}

	@Override
	public void remove(Object entity) {

		checkNotNull(entity);

		// get the id of the object to be deleted
		final AnnotationInfo ai = getAnnotationInfo(entity.getClass());
		final String objectId = (String) ai.getIdProperty().getProperty(entity);
		if (Strings.isNullOrEmpty(objectId)) {
			throw new IllegalStateException(Message.E_NO_ID_KEY.get());
		}
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			// retrieve the object
			final IDfTypedObject dmsObj = getDmsObj(dfSession, ai, objectId);

			if (dmsObj != null) {

				if (!(dmsObj instanceof IDfPersistentObject)) {
					throw new XcpPersistenceException(Message.E_NOT_PERSISTENT_OBJECT.get(ai.getDmsType()));
				}
				// destroy the object
				((IDfPersistentObject) dmsObj).destroy();
				logger.trace("removed object {} from the repository", objectId);

				// remove the entity from the cache
				sessionCache().remove(objectId);
			}

		} catch (Exception e) {
			throw new XcpPersistenceException(Message.E_REMOVE_FAILED.get(objectId), e);
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

			IDfTypedObject dmsObj = getDmsObj(dfSession, ai, primaryKey);

			if (dmsObj != null) {

				logger.debug("Found dms object {}", primaryKey.toString());

				newInstance = load(entityClass, ai, dfSession, dmsObj);
			}
		} catch (XcpPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new XcpPersistenceException(Message.E_FIND_FAILED.get((String) primaryKey), e);
		} finally {
			releaseSession(dfSession);
		}
		return newInstance;
	}

	public <B> B load(Class<B> entityClass, IDfTypedObject dmsObj) {
		checkNotNull(dmsObj);

		B newInstance = null;
		String primaryKey = "?";
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();
			AnnotationInfo ai = getAnnotationInfo(entityClass);
			primaryKey = ai.getIdProperty().getAttributeName();

			newInstance = load(entityClass, ai, dfSession, dmsObj);
		} catch (XcpPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new XcpPersistenceException(Message.E_FIND_FAILED.get((String) primaryKey), e);
		} finally {
			releaseSession(dfSession);
		}
		return newInstance;
	}

	private <B> B load(Class<B> entityClass, AnnotationInfo ai, IDfSession dfSession, IDfTypedObject dmsObj)
			throws InstantiationException, IllegalAccessException, DfException {
		B newInstance;
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

			} else if ((field.isChild() || field.isParent()) && (ai.getTypeCategory() == XcpTypeCategory.RELATION)) {

				// relation field

				logger.trace("OneToOne relation {}", field.getFieldName());
				Class<?> beanClass = field.getRawClass();
				if (beanClass.isPrimitive()) {
					throw new XcpPersistenceException(Message.E_NOT_PRIMITIVE_TYPE.get(field.getFieldName()));
				}
				String foreignKey = field.isChild() ? PersistentProperty.DMS_ATTR_CHILD_ID : PersistentProperty.DMS_ATTR_PARENT_ID;
				IDfId externalKey = dmsObj.getId(foreignKey);
				Object bean = doFind(beanClass, getAnnotationInfo(beanClass), externalKey);
				field.setProperty(newInstance, bean);

			} else if (field.isParentFolder()) {

				// parent folder
				logger.trace("reading parent folder property {}", field.getFieldName());
				if (field.isPropClassAssignableFrom(String.class)) {
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
					// TODO
					logger.warn("Field {} type is not a String. Cannot read folder path", field.getFieldName());
				}
			}
		}

		// cache the instance
		cachePut(newInstance, ai);
		return newInstance;
	}

	private IDfValue getFolderPathAsDfValue(IDfSession dfSession, IDfTypedObject dmsObj, int index) throws DfException {
		final IDfId dfId = dmsObj.getId(PersistentProperty.DMS_ATTR_FOLDER_ID);
		final IDfFolder parentFolder = (IDfFolder) dfSession.getObject(dfId);
		// ISSUE @2 -- Out of bounds
		final IDfValue dfValue = new DfValue(parentFolder.getFolderPath(0));
		return dfValue;
	}

	private List<Object> getRepeatingValues(IDfTypedObject dmsObj, PersistentProperty field) throws DfException {
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

		checkNotNull(entity);

		// get annotation info
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entity);
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			IDfTypedObject dmsObj = null;
			// retrieve the object
			final String objectId = (String) ai.getIdProperty().getProperty(entity);
			if (!Strings.isNullOrEmpty(objectId)) {
				dmsObj = getDmsObj(dfSession, ai, objectId);
				if (!(dmsObj instanceof IDfPersistentObject)) {
					throw new XcpPersistenceException(Message.E_NOT_PERSISTENT_OBJECT.get(ai.getDmsType()));
				}
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
			((IDfPersistentObject) dmsObj).save();
			logger.trace("Saved object {} to the repository", dmsObj.getObjectId().getId());

			// update system generated value
			dms2Entity(dmsObj, entity, ai);

			// refresh the cache
			cachePut(entity, ai);

		} catch (XcpPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new XcpPersistenceException(Message.E_PERSIST_FAILED.get(entity.toString()), e);
		} finally {
			releaseSession(dfSession);
		}
	}

	@Override
	public void addAttachment(Object entity, String filename, String contentType) {
		checkNotNull(entity);
		checkNotNull(filename);
		checkNotNull(contentType);

		// get annotation info
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entity);
		final String objectId = (String) ai.getIdProperty().getProperty(entity);
		if (Strings.isNullOrEmpty(objectId)) {
			throw new IllegalStateException(Message.E_NO_ID_KEY.get());
		}
		IDfSession dfSession = null;
		IDfCollection collection = null;
		try {
			// get a DFC session
			dfSession = getSession();

			if (ai.getTypeCategory() == XcpTypeCategory.RELATION || ai.getTypeCategory() == XcpTypeCategory.FOLDER) {
				throw new IllegalStateException(Message.E_ILLEGAL_RELATION_USE.get());
			}

			// retrieve the object from the dms repository
			final IDfSysObject dmsObj = (IDfSysObject) getDmsObj(dfSession, ai, objectId);

			if (dmsObj == null) {
				throw new XcpPersistenceException(Message.E_REPO_OBJ_NOT_FOUND.get(objectId));
			}

			if (dmsObj.getContentType() == null || dmsObj.getContentType().trim().equals("")) {
				// set the content type
				dmsObj.setContentType(contentType);
				// set content
				dmsObj.setFile(filename);
			} else {
				if (dmsObj.getContentType().equals(contentType)) {
					throw new IllegalStateException(Message.E_ILLEGAL_ADD_RENDITION.get());
				} else {
					collection = dmsObj.getRenditions(null);
					while (collection.next()) {
						if (collection.getString("full_format").equals(contentType)) {
							dmsObj.removeRendition(contentType);
							break;
						}
					}
					dmsObj.addRendition(filename, contentType);
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
			throw new XcpPersistenceException(Message.E_ADD_ATTACHMENT_FAILED.get(filename, objectId), e);
		} finally {
			if (collection != null) {
				try {
					collection.close();
				} catch (DfException e) {
				}
			}
			releaseSession(dfSession);
		}
	}

	@Override
	public String getAttachment(final Object entity, final String folder, final String filename) {

		checkNotNull(entity);
		checkNotNull(folder);
		checkNotNull(filename);

		return doGetAttachment(entity, folder, filename, Optional.fromNullable((String)null));
	}

	@Override
	public String getAttachment(Object entity, String folder, String filename, String contentType) {
		checkNotNull(entity);
		checkNotNull(folder);
		checkNotNull(filename);
		checkNotNull(contentType);

		return doGetAttachment(entity, folder, filename, Optional.fromNullable(contentType));
	}

	private String doGetAttachment(Object entity, String folder, String filename, Optional<String> contentType) {
		String filePath = folder + File.separator + filename;
		String contentFile = null;

		// get annotation info
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entity);
		final String objectId = (String) ai.getIdProperty().getProperty(entity);
		if (Strings.isNullOrEmpty(objectId)) {
			throw new IllegalStateException(Message.E_NO_ID_KEY.get());
		}
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();

			if (ai.getTypeCategory() == XcpTypeCategory.RELATION || ai.getTypeCategory() == XcpTypeCategory.FOLDER) {
				throw new IllegalStateException(Message.E_CONTENTLESS_OBJ.get(objectId));
			}

			// retrieve the object from the dms repository
			final IDfSysObject dmsObj = (IDfSysObject) getDmsObj(dfSession, ai, objectId);

			if (dmsObj == null) {
				throw new XcpPersistenceException(Message.E_REPO_OBJ_NOT_FOUND.get(objectId));
			}

			// get the content
			contentFile = dmsObj.getFileEx(filePath, contentType.or(dmsObj.getContentType()), 0, false);
			
		} catch (XcpPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new XcpPersistenceException(Message.E_GET_ATTACHMENT_FAILED.get(objectId), e);
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

	private IDfTypedObject createDmsObject(IDfSession dfSession, Object entity, AnnotationInfo ai) throws DfException {
		// create a DMS object if needed
		IDfTypedObject dmsObj = null;
		String identifier = (String) ai.getIdProperty().getProperty(entity);
		if (!Strings.isNullOrEmpty(identifier)) {
			dmsObj = getDmsObj(dfSession, ai, identifier);
		}
		if (dmsObj == null) {
			dmsObj = dfSession.newObject(ai.getDmsType());
			logger.debug("created new object: {}", dmsObj.getObjectId().toString());
		}

		return dmsObj;
	}

	private IDfTypedObject createRelationObject(IDfSession dfSession, Object entity, AnnotationInfo ai) throws DfException {

		// retrieve the parent_id and child_id
		PersistentProperty parentMethod = ai.getParentMethod();
		if (parentMethod == null) {
			throw new XcpPersistenceException(Message.E_PARENT_MISSING.get());
		}
		PersistentProperty childMethod = ai.getChildMethod();
		if (childMethod == null) {
			throw new XcpPersistenceException(Message.E_CHILD_MISSING.get());
		}
		final Object parentObject = parentMethod.getProperty(entity);
		final AnnotationInfo parentAi = getAnnotationInfo(parentObject.getClass());
		final String parentObjectId = (String) parentAi.getIdProperty().getProperty(parentObject);
		final Object childObject = childMethod.getProperty(entity);
		final AnnotationInfo childAi = getAnnotationInfo(childObject.getClass());
		final String childObjectId = (String) childAi.getIdProperty().getProperty(childObject);

		IDfTypedObject dmsParent = getDmsObj(dfSession, parentAi.getDmsType(), parentAi.getIdProperty().getAttributeName(), parentObjectId, -1);
		if (dmsParent == null) {
			throw new XcpPersistenceException(Message.E_REPO_OBJ_NOT_FOUND.get(parentObjectId));
		}
		if (!(dmsParent instanceof IDfPersistentObject)) {
			throw new XcpPersistenceException(Message.E_NOT_PERSISTENT_OBJECT.get(ai.getDmsType()));
		}
		IDfRelation relationObj = ((IDfPersistentObject) dmsParent).addChildRelative(ai.getDmsRelationName(), new DfId(childObjectId), null, false, ai.getLabel());

		return relationObj;
	}

	private final void cachePut(Object entity, AnnotationInfo ai) {
		sessionCache().put(entity, ai);
	}

	private final Object cacheGet(String key) {
		return sessionCache().get(key);
	}

	private final String cacheGetKey(Class<?> entityClass, String key) {
		return entityClass.getName() + "::" + key;
	}

	private final void dms2Entity(IDfTypedObject dmsObj, Object entity, AnnotationInfo ai) throws DfException {
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

	IDfTypedObject getDmsObj(IDfSession dfSession, AnnotationInfo ai, Object objectId) throws DfException {
		return getDmsObj(dfSession, ai, objectId, -1);
	}

	public IDfTypedObject getDmsObj(IDfSession dfSession, AnnotationInfo ai, Object objectId, int vstamp) throws DfException {
		if (Strings.isNullOrEmpty(objectId.toString())) {
			return null;
		}
		if (ai.getTypeCategory() == XcpTypeCategory.RELATION) {
			// return getDmsRelationObj(dfSession, ai, objectId, vstamp);
			return getDmsObj(dfSession, ai.getDmsType(), PersistentProperty.DMS_ATTR_OBJECT_ID, objectId, vstamp);
		} else {
			return getDmsObj(dfSession, ai.getDmsType(), ai.getIdProperty().getAttributeName(), objectId, vstamp);
		}
	}

	private IDfTypedObject getDmsObj(IDfSession dfSession, String objectType, String keyIdentifier, Object objectId, int vstamp) throws DfException {
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
		IDfTypedObject dmsObj = null;
		if (PersistentProperty.DMS_ATTR_OBJECT_ID.equals(keyIdentifier)) {
			dmsObj = dfSession.getObjectByQualification(buffer.toString());
		} else {
			buffer.insert(0, "select * from ");
			dmsObj = getObjectByQuery(dfSession, buffer.toString());
		}
		return dmsObj;
	}

	private IDfTypedObject getObjectByQuery(IDfSession dfSession, String dql) throws DfException {
		IDfTypedObject dmsObj = null;
		
		IDfQuery query = getDctmDriver().createQuery();
		query.setDQL(dql);
		IDfCollection results = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
		try {
			if (results.next()) {
				dmsObj = results.getTypedObject();
			}
		} finally {
			results.close();
		}
		
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
		return new XcpTypedQuery<T>(this, dqlQuery, entityClass, true);
	}

	@Override
	public <T> DmsBeanQuery<T> createBeanQuery(Class<T> entityClass) {
		return new XcpBeanQuery<T>(this, entityClass);
	}

	@Override
	public <T, R> DmsTypedQuery<T> createChildRelativesQuery(Object parent, Class<R> relationClass, Class<T> childClass, String optionalDqlFilter) {
		final String dqlQuery = buildChildRelative(parent, relationClass, childClass, Optional.fromNullable(optionalDqlFilter));
		return new XcpTypedQuery<T>(this, dqlQuery, childClass, true);
	}

	@Override
	public <T, R> DmsTypedQuery<T> createParentRelativesQuery(Object child, Class<R> relationClass, Class<T> parentClass, String optionalDqlFilter) {
		final String dqlQuery = buildParentRelative(child, relationClass, parentClass, Optional.fromNullable(optionalDqlFilter));
		return new XcpTypedQuery<T>(this, dqlQuery, parentClass, true);
	}

	private <T, R> String buildChildRelative(Object parent, Class<R> relationClass, Class<T> childClass, Optional<String> dqlFilter) {

		final AnnotationInfo relationAi = getAnnotationInfo(relationClass);
		final AnnotationInfo parentAi = getAnnotationInfo(parent.getClass());
		final AnnotationInfo childAi = getAnnotationInfo(childClass);
		final String parentObjectId = (String) parentAi.getIdProperty().getProperty(parent);

		final StringBuffer buffer = new StringBuffer();
		buffer.append("select c.r_object_id").append(" from ").append(relationAi.getDmsType()).append(" r, ").append(childAi.getDmsType()).append(" c")
				.append(" where r.relation_name = '").append(relationAi.getDmsRelationName()).append("'").append(" and r.parent_id = '").append(parentObjectId)
				.append("'").append(" and r.child_id = c.r_object_id");
		if (dqlFilter.isPresent()) {
			buffer.append(" ").append(dqlFilter.get());
		}
		return buffer.toString();
	}

	private <T, R> String buildParentRelative(Object child, Class<R> relationClass, Class<T> parentClass, Optional<String> dqlFilter) {

		final AnnotationInfo relationAi = getAnnotationInfo(relationClass);
		final AnnotationInfo childAi = getAnnotationInfo(child.getClass());
		final AnnotationInfo parentAi = getAnnotationInfo(parentClass);
		final String childObjectId = (String) childAi.getIdProperty().getProperty(child);

		final StringBuffer buffer = new StringBuffer();
		buffer.append("select p.r_object_id").append(" from ").append(relationAi.getDmsType()).append(" r, ").append(parentAi.getDmsType()).append(" p")
				.append(" where r.relation_name = '").append(relationAi.getDmsRelationName()).append("'").append(" and r.child_id = '").append(childObjectId)
				.append("'").append(" and r.parent_id = p.r_object_id");
		if (dqlFilter.isPresent()) {
			buffer.append(" ").append(dqlFilter.get());
		}
		return buffer.toString();
	}

	SessionCacheWrapper sessionCache() {
		return sessionCache;
	}

	

}
