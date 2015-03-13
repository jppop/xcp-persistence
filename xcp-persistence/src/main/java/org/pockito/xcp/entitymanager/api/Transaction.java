package org.pockito.xcp.entitymanager.api;

public interface Transaction {

	void begin();
	
	void commit();
	
	void rollback();
	
	void setRollbackOnly();
	
	boolean getRollbackOnly();
	
	boolean isActive();
	
}
