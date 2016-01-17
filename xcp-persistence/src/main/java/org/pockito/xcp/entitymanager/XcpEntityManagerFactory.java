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
import org.pockito.xcp.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class XcpEntityManagerFactory implements DmsEntityManagerFactory {

	private Logger logger = LoggerFactory.getLogger(XcpEntityManagerFactory.class);
	
	/**
	 * Stores annotation info about our entities for easy retrieval when needed
	 */
	private final AnnotationManager annotationManager;
	private Map<String, ?> props;
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
			Preconditions.checkNotNull(props);
			this.props = props;
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
			if (Strings.isNullOrEmpty(username)) throw new IllegalArgumentException(Message.E_NO_REPOSITORY_CONTEXT.get());
			String password = (String) props.get(Password);
			String repository = (String) props.get(Repository);
			if (Strings.isNullOrEmpty(repository)) throw new IllegalArgumentException(Message.E_NO_REPOSITORY_CONTEXT.get());
			
			// create a new dctm session manager
			logger.debug("Create a new entity manager for ({}, {})", repository, username);
			dctmDriver.getSessionManager(repository, username, password);
			return new XcpEntityManager(this, props, dctmDriver);
			
		} catch (Exception e) {
			throw new XcpPersistenceException(Message.E_ENTITYMGR_CREATION_FAILED.get(), e);
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
