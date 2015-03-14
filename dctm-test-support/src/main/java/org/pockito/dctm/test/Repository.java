package org.pockito.dctm.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocbrokerClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

public enum Repository {

	instance;

	private static final Logger logger = LoggerFactory.getLogger(Repository.class);

	private static final IDfClientX clientX = new DfClientX();

	private static final Map<String, IDfSessionManager> sessMgrMemoryCache = new ConcurrentHashMap<String, IDfSessionManager>();

	public static Repository getInstance() {
		return instance;
	}

	/**
	 * Gets a new session. The session is not managed.
	 * 
	 * @param repository
	 * @param username
	 * @param password
	 * @return
	 * @throws DmsException
	 * @throws DfException
	 */
	public IDfSession getPrivateSession(String repository, String username, String password) throws DfException {
		IDfLoginInfo li = new DfLoginInfo();
		li.setUser(username);
		li.setPassword(password);
		li.setDomain(null);

		IDfSession dfSession = null;
		IDfClient client = DfClient.getLocalClient();
		dfSession = client.newSession(repository, li);
		return dfSession;
	}

	/**
	 * Gets a session manager.
	 * 
	 * @param repository
	 * @param username
	 * @param password
	 * @return
	 * @throws DfException
	 */
	public IDfSessionManager getSessionManager(String repository, String username, String password) throws DfException {
		final String key = repository + ":" + username;
		IDfSessionManager sessMgr = sessMgrMemoryCache.get(key);
		if (sessMgr == null) {
			sessMgr = newSessionManager(repository, username, password);
			sessMgrMemoryCache.put(key, sessMgr);
		}
		return sessMgr;
	}

	/**
	 * Creates a new session manager.
	 * 
	 * @return
	 * @throws DfException
	 */
	public IDfSessionManager newSessionManager(String repository, String username, String password) throws DfException {
		final IDfClient localClient = DfClient.getLocalClient();
		final IDfTypedObject clientConfig = localClient.getClientConfig();
		String docbroker = getBroker();
		if (docbroker != null) {
			clientConfig.setString("primary_host", docbroker);
		}
		String port = getBrokerPort();
		if (port != null) {
			clientConfig.setString("primary_port", port);
		}
		IDfSessionManager sessMgr = localClient.newSessionManager();

		// set credentials
		IDfLoginInfo li = clientX.getLoginInfo();
		li.setUser(username);
		li.setPassword(password);
		li.setDomain(null);

		sessMgr.setIdentity(repository, li);
		return sessMgr;
	}

	/**
	 * Gets a DFC session connected to <tt>repository</tt> as <tt>username</tt>.
	 * 
	 * @param repository
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public IDfSession getSession(String repository, String username, String password) throws DfException {

		IDfSessionManager sessMgr = getSessionManager(repository, username, password);
		IDfSession dfSession = sessMgr.getSession(repository);

		return dfSession;
	}

	/**
	 * Gets a DFC session connected to <tt>repository</tt> as <tt>username</tt>
	 * using a login ticket.
	 * 
	 * @param repository
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public IDfSession getSession(String repository, String username) throws DfException {

		String ticket = getLoginTicketForUser(repository, username);
		return getSession(repository, username, ticket);
	}

	public IDfSession getSessionForOperator(String repository) throws DfException {
		return getSession(repository, getOperatorName());
	}

	/**
	 * Releases a session.
	 * 
	 * @param dfSession
	 */
	public void releaseSession(IDfSession dfSession) {
		try {
			if (dfSession != null) {
				IDfSessionManager sMgr = dfSession.getSessionManager();
				sMgr.release(dfSession);
			}
		} catch (Exception ignore) {
			logger.trace("error while releasing session {}. Error was {}", dfSession.toString(), ignore.getMessage());
		}
	}

	/**
	 * Gets a privileged (super user) session.
	 * 
	 * @param repository
	 * @return
	 * @throws DfException
	 */
	public IDfSession getPrivilegedSession(String repository) throws DfException {
		IDfSession dfSession = null;
		try {

			IDfClient localClient = clientX.getLocalClient();
			IDfTypedObject clientConfig = localClient.getClientConfig();
			String docbroker = getBroker();
			if (docbroker != null) {
				clientConfig.setString("primary_host", docbroker);
			}
			String port = getBrokerPort();
			if (port != null) {
				clientConfig.setString("primary_port", port);
			}

			IDfLoginInfo li = clientX.getLoginInfo();
			li.setUser(getSuUsername());
			li.setPassword(getSuPassword());
			li.setDomain(null);

			dfSession = clientX.getLocalClient().newSession(repository, li);
		} catch (DfException e) {
			throw e;
		}
		return dfSession;
	}

	public IDfSession getPrivilegedSession() throws DfException {
		return getPrivilegedSession(getRepositoryName());
	}

	/**
	 * Releases the privileged session.
	 * 
	 * @param dfSession
	 */
	public void releasePrivilegedSession(IDfSession dfSession) {
		if (dfSession != null) {
			try {
				dfSession.disconnect();
			} catch (DfException ignore) {
				ignore.printStackTrace();
			}
		}
	}

	/**
	 * Gets a login ticket for an user.
	 * 
	 * @param repository
	 * @param username
	 * @return
	 * @throws DfException
	 */
	public String getLoginTicketForUser(String repository, String username) throws DfException {
		String ticket = null;
		IDfSession dfPriviligedSession = getPrivilegedSession(repository);
		try {
			ticket = dfPriviligedSession.getLoginTicketForUser(username);
		} catch (DfException e) {
			throw e;
		} finally {
			releasePrivilegedSession(dfPriviligedSession);
		}
		return ticket;
	}

	public static String getBroker() {
		String docbroker = RepositoryConfig.BROKER_HOST.getValue();
		if (docbroker == null || docbroker.trim().length() == 0 || "_default_".equals(docbroker.trim())) {
			docbroker = null;
		}
		return docbroker;
	}

	public static String getBrokerPort() {
		String docbrokerPort = RepositoryConfig.BROKER_PORT.getValue();
		if (docbrokerPort == null || docbrokerPort.trim().length() == 0 || "_default_".equals(docbrokerPort.trim())) {
			docbrokerPort = null;
		}
		return docbrokerPort;
	}

	public String getSuUsername() {
		return RepositoryConfig.SUPERUSER_NAME.getValue();
	}

	public String getSuPassword() {
		return RepositoryConfig.SUPERUSER_PWD.getValue();
	}

	public String getOperatorName() {
		return RepositoryConfig.OPERATOR_NAME.getValue();
	}

	public String getOperatorPassword() {
		return RepositoryConfig.OPERATOR_PWD.getValue();
	}

	public String getRepositoryName() {
		return RepositoryConfig.NAME.getValue();
	}

	/**
	 * Gets the repository where an object is stored.
	 * 
	 * @param anObjectId
	 * @return
	 */
	public static String getDocbaseNameFromId(final String anObjectId) {
		String docbaseName = null;
		try {
			final IDfId objectId = new DfId(anObjectId);
			if (objectId.isObjectId()) {
				docbaseName = getDocbaseNameFromId(objectId);
			}
		} catch (DfException ignore) {
			logger.debug("failed to retrieve the docbase name from the object id", ignore);
		}
		return docbaseName;
	}

	/**
	 * Gets the repository where an object is stored.
	 * 
	 * @param objectId
	 * @return
	 * @throws DfException
	 */
	public static String getDocbaseNameFromId(final IDfId objectId) throws DfException {
		String retval = null;
		if (objectId != null && objectId.isObjectId()) {

			IDfClient localClient = clientX.getLocalClient();
			IDfTypedObject clientConfig = localClient.getClientConfig();
			String docbroker = getBroker();
			if (docbroker != null) {
				clientConfig.setString("primary_host", docbroker);
			}
			String port = getBrokerPort();
			if (port != null) {
				clientConfig.setString("primary_port", port);
			}

			final IDfDocbrokerClient docbrokerClient = clientX.getDocbrokerClient();
			retval = docbrokerClient.getDocbaseNameFromId(objectId);
		}
		return retval;
	}

	/**
	 * Indicates whether this ID is a valid object ID in the repository. Checks
	 * also the type against the type passed as an argument.
	 * 
	 * @param anObjectId
	 * @param repository
	 * @param type
	 * @return
	 */
	public static boolean isObjectId(final String anObjectId, String repository, int type) {
		Boolean isAnId = null;
		try {
			final IDfId objectId = new DfId(anObjectId);
			if (objectId.isObjectId()) {
				if (repository == null) {
					isAnId = Boolean.TRUE;
				} else {
					String docbaseName = getDocbaseNameFromId(objectId);
					isAnId = Boolean.valueOf(repository.equals(docbaseName));
				}
				if (type == -1) {
					if (isAnId == null) {
						isAnId = Boolean.TRUE;
					}
				} else {
					int actualTypePart = objectId.getTypePart();
					isAnId = Boolean.valueOf(type == actualTypePart);
				}
			} else {
				isAnId = Boolean.FALSE;
			}
		} catch (DfException ignore) {
			logger.debug("failed to retrieve the docbase name from the object id", ignore);
		}
		return isAnId.booleanValue();
	}

}
