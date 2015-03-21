package org.pockito.xcp.entitymanager;

import org.pockito.xcp.entitymanager.api.Transaction;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.message.Message;

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
			throw new IllegalStateException(Message.E_TX_ALREADY_STARTED.get());
		}
		try {
			this.sessionManager.beginTransaction();
		} catch (DfServiceException e) {
			throw new XcpPersistenceException(Message.E_TX_START_FAILED.get(), e);
		}
		this.isActive = true;
		this.isRollbackOnly = false;
	}

	@Override
	public void commit() {
		if (!this.isActive) {
			throw new IllegalStateException(Message.E_TX_START_FAILED.get());
		}
		if (this.isRollbackOnly) {
			throw new IllegalStateException(Message.E_TX_CANNNOT_COMMIT.get());
		}
		try {
			this.sessionManager.commitTransaction();
		} catch (DfServiceException e) {
			throw new XcpPersistenceException(Message.E_TX_COMMIT_FAILED.get(), e);
		}
		this.isActive = false;
	}

	@Override
	public void rollback() {
		if (!this.isActive) {
			throw new IllegalStateException(Message.E_TX_NOT_ACTIVE.get());
		}
		try {
			this.sessionManager.abortTransaction();
		} catch (DfServiceException e) {
			throw new XcpPersistenceException(Message.E_TX_ROLLBACK_FAILED.get(), e);
		}
		this.isActive = false;
	}

	@Override
	public void setRollbackOnly() {
		if (!this.isActive) {
			throw new IllegalStateException(Message.E_TX_NOT_ACTIVE.get());
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
