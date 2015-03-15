package org.pockito.xcp.entitymanager;

import java.util.ArrayList;
import java.util.List;

import org.pockito.xcp.entitymanager.api.DctmDriver;
import org.pockito.xcp.entitymanager.api.DmsException;
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
import com.google.common.base.Stopwatch;

public class DctmDriverImpl implements DctmDriver {

	private static final Logger logger = LoggerFactory.getLogger(DctmDriver.class);
	
	private static final IDfClientX CLIENTX = new DfClientX();
	
	private IDfSessionManager dfSessionManager = null;
	
	private String repository = null;
	
	@Override
	public final IDfSessionManager getSessionManager() throws DmsException {
		return this.dfSessionManager;
	}

	@Override
	public final IDfSessionManager getSessionManager(final String repository, final String username,
			final String password) throws DmsException {
		IDfSessionManager manager = null;
		try {
//			manager = DmsRepository.getInstance().getSessionManager();
			if (this.dfSessionManager != null) {
				this.dfSessionManager.clearIdentities();
				this.dfSessionManager.flushSessions();
				this.dfSessionManager = null;
			}
		    IDfLoginInfo login = new DfLoginInfo();
		    login.setUser(username);
		    login.setPassword(password);
		    manager = DfClient.getLocalClient().newSessionManager();
		    manager.setIdentity(repository, login);
		    setDfSessionManager(manager);
		    setRepository(repository);
		} catch (DfException e) {
			throw new DmsException("failed to get the session manager",  e);
		}
		return manager;
	}

	@Override
	public final IDfSession getSession() {
		IDfSession session;
		checkSessionMgr();
		try {
			session = sessionManager().getSession(getRepository());
		} catch (DfException e) {
			throw new DmsException("failed to get the session",  e);
		}
		return session;
	}

	@Override
	public final void releaseSession(final IDfSession session) {
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
			throw new DmsException("Failed to query the database using: " + query, e);
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
		Stopwatch stopwatch = Stopwatch.createStarted();
		
		int count = 0;
		
		IDfQuery queryExecutor = createQuery();
		queryExecutor.setDQL(query);
		IDfCollection col = null;
		try {
			col = queryExecutor.execute(session, IDfQuery.DF_READ_QUERY);
			while (col.next()) {
				count++;
				rowHandler.handleRow(session, col);
			}
		} catch (DfException e) {
			logger.debug("failed to query the repository", e);
			throw new DmsException("Failed to query the database using: " + query, e);
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
			throw new DmsException("Failed to query the database using: " + query, e);
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
			throw new DmsException("No session manager");
		}
	}

	private IDfSessionManager sessionManager() {
		return dfSessionManager;
	}

	private void setDfSessionManager(IDfSessionManager dfSessionManger) {
		this.dfSessionManager = dfSessionManger;
	}

}