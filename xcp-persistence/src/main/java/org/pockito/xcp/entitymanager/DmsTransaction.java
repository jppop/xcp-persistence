package org.pockito.xcp.entitymanager;

import org.pockito.xcp.entitymanager.api.Transaction;
import org.pockito.xcp.exception.XcpPersistenceException;

import com.documentum.fc.client.DfServiceException;
import com.documentum.fc.client.IDfSessionManager;

public class DmsTransaction implements Transaction {

	private final IDfSessionManager sessionManager;
	private boolean isActive = false;
	private boolean isRollbackOnly = false;
	
	DmsTransaction(IDfSessionManager sessionManager) {
		super();
		this.sessionManager = sessionManager;
	}

	@Override
	public void begin() {
		if (this.isActive) {
			throw new IllegalStateException("transaction is still active");
		}
		try {
			this.sessionManager.beginTransaction();
		} catch (DfServiceException e) {
			throw new XcpPersistenceException("failed to start a transaction", e);
		}
		this.isActive = true;
		this.isRollbackOnly = false;
	}

	@Override
	public void commit() {
		if (!this.isActive) {
			throw new IllegalStateException("transaction is not active");
		}
		if (this.isRollbackOnly) {
			throw new IllegalStateException("transaction is rollback only");
		}
		try {
			this.sessionManager.commitTransaction();
		} catch (DfServiceException e) {
			throw new XcpPersistenceException("failed to commit the transaction", e);
		}
		this.isActive = false;
	}

	@Override
	public void rollback() {
		if (!this.isActive) {
			throw new IllegalStateException("transaction is not active");
		}
		try {
			this.sessionManager.abortTransaction();
		} catch (DfServiceException e) {
			throw new XcpPersistenceException("failed to abort the transaction", e);
		}
		this.isActive = false;
	}

	@Override
	public void setRollbackOnly() {
		if (!this.isActive) {
			throw new IllegalStateException("transaction is not active");
		}
		this.isRollbackOnly = true;
	}

	@Override
	public boolean getRollbackOnly() {
		return isRollbackOnly;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

}
