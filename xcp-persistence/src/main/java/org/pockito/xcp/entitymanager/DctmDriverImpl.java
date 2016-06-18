package org.pockito.xcp.entitymanager;

import java.util.ArrayList;
import java.util.List;

import org.pockito.xcp.entitymanager.api.DctmDriver;
import org.pockito.xcp.entitymanager.api.DmsException;
import org.pockito.xcp.entitymanager.cache.BuiltInCache;
import org.pockito.xcp.entitymanager.cache.CacheWrapper;
import org.pockito.xcp.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.impl.util.RegistryPasswordUtils;
import com.google.common.base.Stopwatch;

public class DctmDriverImpl implements DctmDriver {

	private static final Logger logger = LoggerFactory.getLogger(DctmDriverImpl.class);
	
	private static final IDfClientX CLIENTX = new DfClientX();
	
	private IDfSessionManager dfSessionManager = null;
	
	private String repository = null;
	
	private static final ThreadLocal<CacheWrapper<String, IDfSessionManager>> sessMgrCache = new ThreadLocal<CacheWrapper<String, IDfSessionManager>>(){

		@Override
		protected CacheWrapper<String, IDfSessionManager> initialValue() {
			return  new BuiltInCache<String, IDfSessionManager>();
		}
		
	};
	
	@Override
	public final IDfSessionManager getSessionManager() throws DmsException {
		return this.dfSessionManager;
	}

	@Override
	public final IDfSessionManager getSessionManager(final String repository, final String username,
			final String password) throws DmsException {
		IDfSessionManager manager = null;
		try {
			// retrieve he session manager from the cache
			final String key = repository + "::" + username;
			manager = sessMgrCache.get().get(key);
			if (manager != null) {
				logger.trace("got the session manager for ({}, {}) from the cache", repository, username);
			} else {
				logger.trace("Getting an new session manager for ({}, {})", repository, username);
				if (this.dfSessionManager != null) {
					this.dfSessionManager.clearIdentities();
					this.dfSessionManager.flushSessions();
					this.dfSessionManager = null;
				}
			    IDfLoginInfo login = new DfLoginInfo();
			    login.setUser(username);
			    login.setPassword(decrypt(password));
			    manager = DfClient.getLocalClient().newSessionManager();
			    manager.setIdentity(repository, login);
			    // cache the session manager
			    sessMgrCache.get().put(key, manager);
			}
		    setDfSessionManager(manager);
		    setRepository(repository);
		} catch (DfException e) {
			throw new DmsException(Message.E_DFC_SESSMGR_FAILED.get(),  e);
		}
		return manager;
	}

	private String decrypt(final String password) {
		if (password != null && password.startsWith("enc:")) {
			String decryptedPwd;
			try {
				decryptedPwd = RegistryPasswordUtils.decrypt(password.substring(4));
			} catch (DfException e) {
				decryptedPwd = password;
			}
			return decryptedPwd;
		} else {
			return password;
		}
	}

	@Override
	public final IDfSession getSession() {
		logger.trace("getting a managed sesssion");
		IDfSession session;
		checkSessionMgr();
		try {
			session = sessionManager().getSession(getRepository());
			logger.trace("got session: {}", session.getSessionConfig().getString("session_id") );
		} catch (DfException e) {
			throw new DmsException(Message.E_DFC_SESSION_FAILED.get(),  e);
		}
		return session;
	}

	@Override
	public final void releaseSession(final IDfSession session) {
		logger.trace("releasing sesssion");
		try {
			if (session != null) {
				IDfSessionManager sMgr = session.getSessionManager();
				sMgr.release(session);
			}
		} catch (Exception ignore) {
			logger.trace("got exception while releasing session {}", ignore.getMessage());
		}
	}

	@Override
	public final IDfQuery createQuery() {
		return CLIENTX.getQuery();
	}

	@Override
	public final List<IDfId> getObjectsByQuery(final IDfSession session, final String query)
			throws DmsException {
		
		logger.trace("executing query: {}", query);

		Stopwatch stopwatch = Stopwatch.createStarted();
		
		List<IDfId> results = new ArrayList<IDfId>();
		
		IDfQuery queryExecutor = createQuery();
		queryExecutor.setDQL(query);
		IDfCollection col = null;
		try {
			logger.debug("query: {}", query);
			col = queryExecutor.execute(session, IDfQuery.DF_READ_QUERY);
			while (col.next()) {
				results.add(col.getId("r_object_id"));
			}
		} catch (DfException e) {
			logger.debug("failed to query the repository", e);
			throw new DmsException(Message.E_DFC_QUERY_FAILED.get(query), e);
		} finally {
			try {
				if (col != null) {
					col.close();
				}
			} catch (DfException ignore) {
				logger.error("failed to close a collection", ignore);
			}
		}
		stopwatch.stop();
		logger.debug("query executed in: {}", stopwatch);
		return results;
	}

	@Override
	public int getObjectsByQuery(IDfSession session, String query, RowHandler rowHandler) throws DmsException {

		logger.trace("executing query: {}", query);

		Stopwatch stopwatch = Stopwatch.createStarted();
		
		int count = 0;
		
		IDfQuery queryExecutor = createQuery();
		queryExecutor.setDQL(query);
		IDfCollection col = null;
		try {
			col = queryExecutor.execute(session, IDfQuery.DF_READ_QUERY);
			while (col.next()) {
				count++;
				rowHandler.handleRow(session, col.getTypedObject());
			}
		} catch (DfException e) {
			logger.debug("failed to query the repository", e);
			throw new DmsException(Message.E_DFC_QUERY_FAILED.get(query), e);
		} finally {
			try {
				if (col != null) {
					col.close();
				}
			} catch (DfException ignore) {
				logger.error("failed to close a collection", ignore);
			}
		}
		stopwatch.stop();
		logger.debug("query executed in: {}", stopwatch);
		return count;
	}

	@Override
	public final int executeQuery(final IDfSession session, final String query)
			throws DmsException {
		
		logger.trace("executing query: {}", query);
		int count = -1;
		
		IDfQuery queryExecutor = createQuery();
		queryExecutor.setDQL(query);
		IDfCollection col = null;
		try {
			col = queryExecutor.execute(session, IDfQuery.DF_QUERY);
			if (col.next()) {
				IDfAttr attr = col.getAttr(0);
				count = col.getInt(attr.getName());
			}
		} catch (DfException e) {
			throw new DmsException(Message.E_DFC_QUERY_FAILED.get(query), e);
		} finally {
			try {
				if (col != null) {
					col.close();
				}
			} catch (DfException ignore) {
				logger.error("failed to close a collection", ignore);
			}
		}
		return count;
	}

	public final String getRepository() {
		return repository;
	}

	private final void setRepository(final String repository) {
		this.repository = repository;
	}

	private void checkSessionMgr() {
		if (dfSessionManager == null) {
			throw new DmsException(Message.E_DFC_NO_SESSIONMGR.get());
		}
	}

	private IDfSessionManager sessionManager() {
		return dfSessionManager;
	}

	private void setDfSessionManager(IDfSessionManager dfSessionManger) {
		this.dfSessionManager = dfSessionManger;
	}

}
