package org.pockito.xcp.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;

public class DctmDriverImpl implements DctmDriver {

	private static final Logger logger = LoggerFactory.getLogger(DctmDriver.class);
	
	private String repository = null;
	
	@Override
	public IDfSessionManager getSessionManager() throws DmsException {
		IDfSessionManager manager;
		try {
			manager = Repository.getInstance().getSessionManager();
		} catch (DfException e) {
			throw new DmsException("failed to get the session manager",  e);
		}
		return manager;
	}

	@Override
	public void setCredendatials(String repository, String username,
			String password) {
		try {
			Repository.getInstance().setIdentity(repository, username, password);
			setRepository(repository);
		} catch (DfException e) {
			throw new DmsException("failed to set credentials",  e);
		}
	}

	@Override
	public IDfSession getSession() {
		IDfSession session;
		try {
			session = Repository.getInstance().getSession(getRepository());
		} catch (DfException e) {
			throw new DmsException("failed to get the session manager",  e);
		}
		return session;
	}

	@Override
	public void releaseSession(IDfSession session) {
		Repository.getInstance().releaseSession(session);
	}

	@Override
	public IDfQuery createQuery() {
		return new DfClientX().getQuery();
	}

	@Override
	public List<IDfId> getObjectsByQuery(IDfSession session, String query)
			throws DmsException {
		
		List<IDfId> results = new ArrayList<IDfId>();
		
		IDfQuery queryExecutor = createQuery();
		IDfCollection col = null;
		try {
			col = queryExecutor.execute(session, IDfQuery.DF_READ_QUERY);
			while (col.next()) {
				results.add(col.getId("r_object_id"));

			}
		} catch(DfException e) {
			throw new DmsException("Failed to query the database using: " + query, e);
		} finally {
			try {
				if (col != null) col.close();
			} catch (DfException ignore) {
				logger.error("failed to close a collection", ignore);
			}
		}
		return results;
	}

	@Override
	public int getCountOfObjects(IDfSession session, String query)
			throws DmsException {
		
		int count = -1;
		
		IDfQuery queryExecutor = createQuery();
		IDfCollection col = null;
		try {
			col = queryExecutor.execute(session, IDfQuery.DF_READ_QUERY);
			if (col.next()) {
				IDfAttr attr = col.getAttr(0);
				count = col.getInt(attr.getName());
			}
		} catch (DfException e) {
			throw new DmsException("Failed to query the database using: " + query, e);
		} finally {
			try {
				if (col != null) col.close();
			} catch (DfException ignore) {
				logger.error("failed to close a collection", ignore);
			}
		}
		return count;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

}
