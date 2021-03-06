package org.pockito.xcp.repository;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.repository.command.XcpRepoCmdFactory;
import org.pockito.xcp.repository.command.XcpRepoCmdFactory.UnregisterCallback;
import org.pockito.xcp.repository.command.XcpRepoCommand;

public class XcpGenericRepoImpl<T> implements XcpGenericRepo<T> {

	
//	private final Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
//			.getActualTypeArguments()[0];
	
	private Class<T> entityClass = null;
	
	private ThreadLocal<XcpRepoCommand> xcpCmd = new ThreadLocal<XcpRepoCommand>(){

		@Override
		protected XcpRepoCommand initialValue() {
			return null;
		}
		
	};

	protected boolean autoCommit;

	@SuppressWarnings("unchecked")
	public Class<T> getEntityClass() {
		entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		return entityClass;
	}
	
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
	public T find(Object primaryKey) {
		return find(getEntityClass(), primaryKey);
	}

	@Override
	public <R> List<T> findByParent(Object parent, Class<R> relationClass, String optionalDqlFilter) {
		DmsTypedQuery<T> query = cmd().createChildRelativesQuery(parent, relationClass, getEntityClass(),
				optionalDqlFilter);
		return query.getResultList();
	}

	@Override
	public <R> List<T> findByChild(Object child, Class<R> relationClass, String optionalDqlFilter) {
		DmsTypedQuery<T> query = cmd().createParentRelativesQuery(child, relationClass, getEntityClass(),
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
	public XcpRepoCommand getCurrentCmd() {
		return this.xcpCmd.get();
	}

	@Override
	public void commitSharedCmd() {
		if (getCurrentCmd() != null) {
			cmd().commit();
			unregisterCmd();
		}
	}

	@Override
	public void rollbackSharedCmd() {
		if (getCurrentCmd() != null) {
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

	@Override
	public XcpRepoCommand cmd() {
		if (this.xcpCmd.get() == null) {
			XcpRepoCommand cmd = XcpRepoCmdFactory.instance.getSharedCmd();
			if (cmd == null) {
				createCmd();
			} else {
				useSharedCmd(cmd);
			}
		}
		if (this.xcpCmd.get()== null) {
			
			throw new IllegalStateException(Message.E_CMD_CREATION_FAILED.get());
		}
		return this.xcpCmd.get();
	}

	protected void unregisterCmd() {
		// reset the commands
		XcpRepoCmdFactory.instance.unregisterSharedCmd();
		// FIX: reset the owned command
		xcpCmd.set(null);
	}

	protected XcpRepoCommand createCmd() {
		this.xcpCmd.set(XcpRepoCmdFactory.instance.create());
		XcpRepoCmdFactory.instance.registerSharedCmd(null);
		setAutoCommit(true);
		return this.xcpCmd.get();
	}

	protected void useSharedCmd(XcpRepoCommand cmd) {
		setAutoCommit(false);
		XcpRepoCmdFactory.instance.useSharedCmd(new UnregisterCallback() {
			
			@Override
			public void unregistered() {
				resetCmd();
			}
		});
		this.xcpCmd.set(cmd);
	}

	protected void commit() {
		if ((getCurrentCmd() != null) && isAutoCommit()) {
			cmd().commit();
			resetCmd();
		}
	}

	private void resetCmd() {
		xcpCmd.set(null);
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	protected void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	@Override
	public String getAttachment(T entity, String filename) {
		return getAttachment(entity, cmd().getRootPath(), filename);
	}

	@Override
	public String getAttachment(T entity, String folder, String filename) {
		return cmd().getAttachment(entity, folder, filename);
	}

}
