package org.pockito.xcp.repository;

import java.util.List;

import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.IDfId;

public interface DctmDriver {

	/**
	 * Creates a IDfSessionManager with no prepopulated identities.
	 * 
	 * @return
	 * @throws DfException
	 */
	IDfSessionManager getSessionManager() throws DmsException;

	void setCredendatials(String repository, String username,
			String password);

	IDfSession getSession();

	void releaseSession(IDfSession session);

	IDfQuery createQuery();

	List<IDfId> getObjectsByQuery(IDfSession session, String query)
			throws DmsException;

	int getCountOfObjects(IDfSession session, String query) throws DmsException;

}
