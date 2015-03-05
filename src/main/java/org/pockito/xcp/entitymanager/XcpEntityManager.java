package org.pockito.xcp.entitymanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsEntityManager;
import org.pockito.xcp.repository.DmsException;
import org.pockito.xcp.repository.DmsJoinTypedQuery;
import org.pockito.xcp.repository.DmsQuery;
import org.pockito.xcp.repository.DmsTypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfValue;

public class XcpEntityManager implements DmsEntityManager {

	private Logger LOGGER = LoggerFactory.getLogger(XcpEntityManager.class);
	
	private final XcpEntityManagerFactory factory;
	private final DctmDriver dctmDriver;
	private final Map<String, ?> properties;
	private final String repository;

	public XcpEntityManager(
			XcpEntityManagerFactory dmsEntityManagerFactoryImpl,
			Map<String, ?> map,
			DctmDriver dctmDriver) {
		this.factory = dmsEntityManagerFactoryImpl;
		this.dctmDriver = dctmDriver;
		this.properties = map;
		this.repository = (String) map.get(PropertyConstants.Repository);
	}

	public <T> AnnotationInfo getAnnotationInfo(Class<T> entityClass) {
		AnnotationInfo ai = factory().getAnnotationManager().getAnnotationInfo(entityClass);
		return ai;
	}
	
	public <T> T find(Class<T> entityClass, Object primaryKey) {

		AnnotationInfo ai = getAnnotationInfo(entityClass);
		return (T) doFind(entityClass, ai, primaryKey);
	}

	private <T> T doFind(Class<T> entityClass, AnnotationInfo ai,
			Object primaryKey) {

		T newInstance = null;
		IDfSession dfSession = null;
		try {
			// get a DFC session
			dfSession = getSession();
			
			IDfPersistentObject dmsObj = getDmsObj(dfSession, ai, primaryKey);
			if (dmsObj != null) {
				
				LOGGER.debug("Found dms object {}", primaryKey.toString());
				
				newInstance = entityClass.newInstance();

				Collection<PersistentProperty> persistentProperties = ai
						.getPersistentProperties();
				for (PersistentProperty field : persistentProperties) {
					LOGGER.trace("reading property {} bound to {}", field.getFieldName(), field.getAttributeName());
					if (field.isRepeating()) {
						List<Object> values = getRepeatingValues(dmsObj, field);
						field.setProperty(newInstance, values);
					} else {
						IDfValue dfValue = dmsObj.getValue(field
								.getAttributeName());
						field.setProperty(newInstance, dfValue);
					}
				}
			}
		} catch (Exception e) {
			throw new XcpPersistenceException(e);
		} finally {
			releaseSession(dfSession);
		}
		return newInstance;
	}

	private List<Object> getRepeatingValues(IDfPersistentObject dmsObj,
			PersistentProperty field) throws DfException {
		List<Object> values = new ArrayList<Object>();
		String attributeName = field.getAttributeName();;
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
			
			// create a DMS object if needed
			IDfPersistentObject dmsObj = null;
			String identifier = (String) ai.getIdMethod().getProperty(entity);
			if (StringUtils.isNotBlank(identifier)) {
				dmsObj = getDmsObj(dfSession, ai, identifier);
			}
			if (dmsObj == null) {
				dmsObj = dfSession.newObject(ai.getDmsType());
				LOGGER.debug("created new object: {}", dmsObj.getObjectId().toString());
			}
			
			for (PersistentProperty field : ai.getPersistentProperties()) {
				if (field.isGeneratedValue() || field.isReadonly()) {
					continue;
				}
				Object fieldValue = field.getProperty(entity);
				if (field.isRepeating()) {
					String attributeName = field.getAttributeName();
					dmsObj.truncate(attributeName, 0);
					int valueIndex = 0;
					for (Object each : (Collection<?>)fieldValue) {
						dmsObj.setRepeatingValue(attributeName, valueIndex++, field.objToDfValue(each));
					}
				} else {
					dmsObj.setValue(field.getAttributeName(), field.objToDfValue(fieldValue));
				}
			}
			
			// save dms object
			dmsObj.save();
			
			// update system generated value
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
						IDfValue dfValue = dmsObj.getValue(field
								.getAttributeName());
						field.setProperty(entity, dfValue);
					}
				}
			}
			
		} catch (Exception e) {
			throw new XcpPersistenceException(e);
		} finally {
			releaseSession(dfSession);
		}
	}

	private IDfPersistentObject getDmsObj(IDfSession dfSession,
			AnnotationInfo ai, Object objectId) throws DfException {
		StringBuilder buffer = new StringBuilder();
		buffer.append(ai.getDmsType());
		buffer.append(" where ");
		buffer.append(ai.getIdMethod().getAttributeName()).append(" = '")
				.append(objectId.toString()).append("'");
		
		// retrieve the persisted object
		IDfPersistentObject dmsObj = dfSession.getObjectByQualification(buffer.toString());
		return dmsObj;
	}

	protected XcpEntityManagerFactory factory() {
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

	@Override
	public <T, B> DmsJoinTypedQuery<T, B> createJoinQuery(Class<T> joinTable, Class<B> beanClass) {
		return new XcpJoinTypedQuery<T, B>(this, joinTable, beanClass);
	}

}
