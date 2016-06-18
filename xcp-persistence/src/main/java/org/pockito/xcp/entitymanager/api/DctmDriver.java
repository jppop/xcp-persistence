package org.pockito.xcp.entitymanager.api;

import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;

public interface DctmDriver {

	/**
	 * Creates a IDfSessionManager with no prepopulated identities.
	 * 
	 * @return
	 * @throws DfException
	 */
	IDfSessionManager getSessionManager() throws DmsException;

	IDfSessionManager getSessionManager(String repository, String username,
			String password) throws DmsException;

	IDfSession getSession();

	void releaseSession(IDfSession session);

	IDfQuery createQuery();

	List<IDfId> getObjectsByQuery(IDfSession session, String query)
			throws DmsException;

	interface RowHandler {
		void handleRow(IDfSession session, IDfTypedObject row) throws DfException;
	}
	
	int getObjectsByQuery(IDfSession session, String query, RowHandler rowHandler)
			throws DmsException;
	
	int executeQuery(IDfSession session, String query) throws DmsException;

}
