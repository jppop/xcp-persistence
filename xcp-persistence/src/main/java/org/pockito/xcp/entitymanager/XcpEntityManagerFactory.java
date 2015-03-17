package org.pockito.xcp.entitymanager;

import static org.pockito.xcp.entitymanager.PropertyConstants.DctmDriver;
import static org.pockito.xcp.entitymanager.PropertyConstants.Password;
import static org.pockito.xcp.entitymanager.PropertyConstants.Repository;
import static org.pockito.xcp.entitymanager.PropertyConstants.SessionLess;
import static org.pockito.xcp.entitymanager.PropertyConstants.Username;

import java.util.HashMap;
import java.util.Map;

import org.pockito.xcp.entitymanager.api.DctmDriver;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsEntityManagerFactory;
import org.pockito.xcp.entitymanager.cache.BuiltInCache;
import org.pockito.xcp.entitymanager.cache.CacheElement;
import org.pockito.xcp.entitymanager.cache.CacheWrapper;
import org.pockito.xcp.exception.XcpPersistenceException;

import com.google.common.base.Strings;

public class XcpEntityManagerFactory implements DmsEntityManagerFactory {

	/**
	 * Stores annotation info about our entities for easy retrieval when needed
	 */
	private final AnnotationManager annotationManager;
	private final Map<String, ?> props;
	private boolean sessionLess = false;
	private CacheWrapper<String, CacheElement> firstLevelCache = null;

	public XcpEntityManagerFactory() {
		this(new HashMap<String, Object>());
	}

	public XcpEntityManagerFactory(final Map<String, ?> props) {
		this.props = props;
		this.annotationManager = new AnnotationManager();
	}

	public DmsEntityManager createDmsEntityManager() {
		return createDmsEntityManager(this.props);
	}

	public DmsEntityManager createDmsEntityManager(final Map<String, ?> props) {
		try {
			if (props == null ) {
				throw new IllegalArgumentException("props cannot be null");
			}
			if (props.containsKey(SessionLess)) {
				this.sessionLess = Boolean.valueOf(props.containsKey(SessionLess)).booleanValue();
			} else {
				this.sessionLess = false;
			}
			DctmDriver dctmDriver = (DctmDriver) props.get(DctmDriver);
			if (dctmDriver == null) {
				dctmDriver = getDctmDriver();
			}
			String username = (String) props.get(Username);
			if (Strings.isNullOrEmpty(username)) throw new IllegalArgumentException("property 'username' is required");
			String password = (String) props.get(Password);
			String repository = (String) props.get(Repository);
			if (Strings.isNullOrEmpty(repository)) throw new IllegalArgumentException("property 'repository' is required");
			
			// create a new dctm session manager
			dctmDriver.getSessionManager(repository, username, password);
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
		return true;
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
