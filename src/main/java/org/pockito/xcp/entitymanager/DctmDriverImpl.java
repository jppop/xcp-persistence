package org.pockito.xcp.entitymanager;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(DctmDriver.class);
	
	private String repository = null;
	
	@Override
	public final IDfSessionManager getSessionManager() throws DmsException {
		IDfSessionManager manager;
		try {
			manager = DmsRepository.getInstance().getSessionManager();
		} catch (DfException e) {
			throw new DmsException("failed to get the session manager",  e);
		}
		return manager;
	}

	@Override
	public final void setCredendatials(final String repository, final String username,
			final String password) {
		try {
			DmsRepository.getInstance().setIdentity(repository, username, password);
			setRepository(repository);
		} catch (DfException e) {
			throw new DmsException("failed to set credentials",  e);
		}
	}

	@Override
	public final IDfSession getSession() {
		IDfSession session;
		try {
			session = DmsRepository.getInstance().getSession(getRepository());
		} catch (DfException e) {
			throw new DmsException("failed to get the session manager",  e);
		}
		return session;
	}

	@Override
	public final void releaseSession(final IDfSession session) {
		DmsRepository.getInstance().releaseSession(session);
	}

	@Override
	public final IDfQuery createQuery() {
		return new DfClientX().getQuery();
	}

	@Override
	public final List<IDfId> getObjectsByQuery(final IDfSession session, final String query)
			throws DmsException {
		
		List<IDfId> results = new ArrayList<IDfId>();
		
		IDfQuery queryExecutor = createQuery();
		IDfCollection col = null;
		try {
			col = queryExecutor.execute(session, IDfQuery.DF_READ_QUERY);
			while (col.next()) {
				results.add(col.getId("r_object_id"));

			}
		} catch (DfException e) {
			throw new DmsException("Failed to query the database using: " + query, e);
		} finally {
			try {
				if (col != null) {
					col.close();
				}
			} catch (DfException ignore) {
				LOGGER.error("failed to close a collection", ignore);
			}
		}
		return results;
	}

	@Override
	public final int getCountOfObjects(final IDfSession session, final String query)
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
				if (col != null) {
					col.close();
				}
			} catch (DfException ignore) {
				LOGGER.error("failed to close a collection", ignore);
			}
		}
		return count;
	}

	public final String getRepository() {
		return repository;
	}

	public final void setRepository(final String repository) {
		this.repository = repository;
	}

}
