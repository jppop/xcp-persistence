package org.pockito.xcp.repository;

public interface Transaction {

	void begin();
	
	void commit();
	
	void rollback();
	
	void setRollbackOnly();
	
	boolean getRollbackOnly();
	
	boolean isActive();
	
}
