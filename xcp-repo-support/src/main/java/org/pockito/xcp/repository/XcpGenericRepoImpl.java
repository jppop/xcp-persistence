package org.pockito.xcp.repository;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.repository.command.XcpRepoCmdFactory;
import org.pockito.xcp.repository.command.XcpRepoCommand;

public class XcpGenericRepoImpl<T> implements XcpGenericRepo<T> {

	private XcpRepoCommand xcpCmd = null;

	protected boolean autoCommit;

	@Override
	public void add(T object) {
		doAdd(object);
		commit();
	}

	@Override
	public void add(List<T> objects) {
		for (T obj : objects) {
			doAdd(obj);
		}
		commit();
	}

	@Override
	public void update(T object) {
		doUpdate(object);
		commit();
	}

	@Override
	public void update(List<T> objects) {
		for (T obj : objects) {
			doUpdate(obj);
		}
		commit();
	}

	@Override
	public void remove(T object) {
		doRemove(object);
		commit();
	}

	@Override
	public void remove(List<T> objects) {
		for (T obj : objects) {
			doRemove(obj);
		}
		commit();
	}

	@Override
	public T find(Class<T> entityClass, Object primaryKey) {
		return cmd().find(entityClass, primaryKey);
	}

	@Override
	public <R> List<T> findChildren(Object parent, Class<R> relationClass, String optionalDqlFilter) {
		@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		DmsTypedQuery<T> query = cmd().getEntityManager().createChildRelativesQuery(parent, relationClass, cls,
				optionalDqlFilter);
		return query.getResultList();
	}

	@Override
	public <R> List<T> findParents(Object child, Class<R> relationClass, String optionalDqlFilter) {
		@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		DmsTypedQuery<T> query = cmd().getEntityManager().createParentRelativesQuery(child, relationClass, cls,
				optionalDqlFilter);
		return query.getResultList();
	}

	@Override
	public XcpRepoCommand createSharedCmd() {
		XcpRepoCommand cmd = XcpRepoCmdFactory.instance.create();
		cmd.setOwner(this); // unused
		XcpRepoCmdFactory.instance.registerSharedCmd(cmd);
		useSharedCmd(cmd);
		return cmd;
	}

	@Override
	public XcpRepoCommand getCmd() {
		return this.xcpCmd;
	}

	@Override
	public void commitSharedCmd() {
		if (getCmd() != null) {
			cmd().commit();
			unregisterCmd();
		}
	}

	@Override
	public void rollbackSharedCmd() {
		if (getCmd() != null) {
			cmd().rollback();
			unregisterCmd();
		}
	}

	private void doAdd(T object) {
		cmd().withinTransaction().create(object);
	}

	private void doUpdate(T object) {
		cmd().withinTransaction().update(object);
	}

	private void doRemove(T object) {
		cmd().withinTransaction().remove(object);
	}

	protected XcpRepoCommand cmd() {
		if (this.xcpCmd == null) {
			XcpRepoCommand cmd = XcpRepoCmdFactory.instance.getSharedCmd();
			if (cmd == null) {
				createCmd();
			} else {
				useSharedCmd(cmd);
			}
		}
		if (this.xcpCmd == null) {
			// TODO
			throw new IllegalStateException("No cmd");
		}
		return this.xcpCmd;
	}

	protected void unregisterCmd() {
		// reset the commands
		XcpRepoCmdFactory.instance.registerSharedCmd(null);
	}

	protected XcpRepoCommand createCmd() {
		this.xcpCmd = XcpRepoCmdFactory.instance.create();
		XcpRepoCmdFactory.instance.registerSharedCmd(null);
		setAutoCommit(true);
		return this.xcpCmd;
	}

	protected void useSharedCmd(XcpRepoCommand cmd) {
		setAutoCommit(false);
		this.xcpCmd = cmd;
	}

	protected void commit() {
		if ((getCmd() != null) && isAutoCommit()) {
			cmd().commit();
		}
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	protected void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

}
