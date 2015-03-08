package org.pockito.xcp.entitymanager;

import static org.pockito.xcp.entitymanager.PropertyConstants.DctmDriver;
import static org.pockito.xcp.entitymanager.PropertyConstants.Password;
import static org.pockito.xcp.entitymanager.PropertyConstants.Repository;
import static org.pockito.xcp.entitymanager.PropertyConstants.SessionLess;
import static org.pockito.xcp.entitymanager.PropertyConstants.Username;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pockito.xcp.entitymanager.cache.CacheElement;
import org.pockito.xcp.entitymanager.cache.CacheWrapper;
import org.pockito.xcp.entitymanager.cache.BuiltInCache;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsEntityManager;
import org.pockito.xcp.repository.DmsEntityManagerFactory;

public class XcpEntityManagerFactory implements DmsEntityManagerFactory {

	/**
	 * Stores annotation info about our entities for easy retrieval when needed
	 */
	private final AnnotationManager annotationManager;
	@SuppressWarnings("unused")
	private final String persistenceUnitName;
	private final Map<String, ?> props;
	private final boolean sessionLess;
	private CacheWrapper<String, CacheElement> firstLevelCache = null;

	public XcpEntityManagerFactory(final String persistenceUnitName) {
		this(persistenceUnitName, null);
	}

	public XcpEntityManagerFactory(final Map<String, ?> props) {
		this("dctm", props);
	}

	public XcpEntityManagerFactory(final String persistenceUnitName,
			final Map<String, ?> props) {
		this.persistenceUnitName = persistenceUnitName;
		this.props = props;
		this.annotationManager = new AnnotationManager();
		if (props.containsKey(SessionLess)) {
			this.sessionLess = Boolean.valueOf(props.containsKey(SessionLess)).booleanValue();
		} else {
			this.sessionLess = false;
		}
	}

	public DmsEntityManager createDmsEntityManager() {
		return createDmsEntityManager(this.props);
	}

	public DmsEntityManager createDmsEntityManager(final Map<String, ?> props) {
		try {
			DctmDriver dctmDriver = (DctmDriver) props.get(DctmDriver);
			if (dctmDriver == null) {
				dctmDriver = getDctmDriver();
			}
			String username = (String) props.get(Username);
			if (StringUtils.isBlank(username)) throw new IllegalArgumentException("property 'username' is required");
			String password = (String) props.get(Password);
			String repository = (String) props.get(Repository);
			if (StringUtils.isBlank(repository)) throw new IllegalArgumentException("property 'repository' is required");
			
			// set single identity for all repositories
			dctmDriver.setCredentials(repository, username, password);
			return new XcpEntityManager(this, props, dctmDriver);
			
		} catch (Exception e) {
			throw new XcpPersistenceException(e);
		}
	}

	public boolean isSessionLess() {
		return sessionLess;
	}

	public void close() {
		if (this.firstLevelCache != null) {
			this.firstLevelCache.clear();
		}
	}

	public boolean isOpen() {
		// TODO isOpen (em)
		return false;
	}

	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}
	
	public DctmDriver getDctmDriver() {
		return new DctmDriverImpl();
	}

	final CacheWrapper<String, CacheElement> getFirstLevelCache() {
		// TODO should be overridable
		this.firstLevelCache = new BuiltInCache<String, CacheElement>();
		return this.firstLevelCache;
	}

}
