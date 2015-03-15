package org.pockito.xcp.repository;

import java.util.List;

import org.pockito.xcp.repository.command.XcpRepoCommand;

public interface XcpGenericRepo<T> {

	void add(T object);
	void add(List<T> objects);
	void update(T object);
	void update(List<T> objects);
	void remove(T object);
	void remove(List<T> objects);
	T find(Class<T> entityClass, Object primaryKey);
	
//	XcpRepoCommand createCmd();
	XcpRepoCommand createSharedCmd();
	XcpRepoCommand getCmd();
	void commitSharedCmd();
	void rollbackSharedCmd();
}
