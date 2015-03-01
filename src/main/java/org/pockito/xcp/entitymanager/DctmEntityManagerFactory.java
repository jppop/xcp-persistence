package org.pockito.xcp.entitymanager;

import static org.pockito.xcp.entitymanager.PropertyConstants.DriverClass;
import static org.pockito.xcp.entitymanager.PropertyConstants.Password;
import static org.pockito.xcp.entitymanager.PropertyConstants.Repository;
import static org.pockito.xcp.entitymanager.PropertyConstants.Username;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsEntityManager;
import org.pockito.xcp.repository.DmsEntityManagerFactory;

public class DctmEntityManagerFactory implements DmsEntityManagerFactory {

	/**
	 * Stores annotation info about our entities for easy retrieval when needed
	 */
	private AnnotationManager annotationManager;
	@SuppressWarnings("unused")
	private String persistenceUnitName;
	private Map<String, ?> props;

	public DctmEntityManagerFactory(String persistenceUnitName) {
		this(persistenceUnitName, null);
	}

	public DctmEntityManagerFactory(Map<String, ?> props) {
		this("dctm", props);
	}

	public DctmEntityManagerFactory(String persistenceUnitName,
			Map<String, ?> props) {
		this.persistenceUnitName = persistenceUnitName;
		this.props = props;
		this.annotationManager = new AnnotationManager();
	}

	public DmsEntityManager createDmsEntityManager() {
		return createDmsEntityManager(this.props);
	}

	public DmsEntityManager createDmsEntityManager(Map<String, ?> map) {
		try {
			DctmDriver dctmDriver = (DctmDriver) map.get(DriverClass);
			if (dctmDriver == null) {
				dctmDriver = getDctmDriver();
			}
			String username = (String) map.get(Username);
			if (StringUtils.isBlank(username)) throw new IllegalArgumentException("property 'username' is required");
			String password = (String) map.get(Password);
			String repository = (String) map.get(Repository);
			if (StringUtils.isBlank(repository)) throw new IllegalArgumentException("property 'repository' is required");
			
			// set single identity for all docbases
			dctmDriver.setCredendatials(repository, username, password);
			return new DctmEntityManager(this, map, dctmDriver);
			
		} catch (Exception e) {
			throw new XcpPersistenceException(e);
		}
	}

	public void close() {
		// TODO close em
	}

	public boolean isOpen() {
		// TODO isOpen (em)
		return false;
	}

	protected AnnotationManager getAnnotationManager() {
		return annotationManager;
	}
	
	public DctmDriver getDctmDriver() {
		return new DctmDriverImpl();
	}

}
