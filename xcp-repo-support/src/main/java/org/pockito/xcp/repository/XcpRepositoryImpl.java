package org.pockito.xcp.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsEntityManagerFactory;
import org.pockito.xcp.entitymanager.api.DmsQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.entitymanager.api.Transaction;
import org.pockito.xcp.repository.command.XcpPersistCommand;
import org.pockito.xcp.repository.command.XcpPersistCommandCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XcpRepositoryImpl implements XcpRepository {

	private Logger logger = LoggerFactory.getLogger(XcpRepositoryImpl.class);
	
	@SuppressWarnings("unused")
	private final Provider<DmsEntityManagerFactory> emFactoryProvider;
	private final DmsEntityManager em;

	@Inject @Named("org.pockito.xcp.repository.name")
	private String repository;

	@Inject @Named("org.pockito.xcp.repository.username")
	private String username;

	@Inject @Named("org.pockito.xcp.repository.password")
	private String pasword;
	
	private XcpPersistCommandCatalog cmd = null;
	private final List<XcpPersistCommand> commands = new ArrayList<XcpPersistCommand>();
	private final Transaction tx;

	private boolean txRequested = false;
	private Object parent;
	private Object child;

	private boolean commandInProgress;
	
	@Inject
	XcpRepositoryImpl(Provider<DmsEntityManagerFactory> emFactoryProvider, String repository, String username,
			String password) {
		this.emFactoryProvider = emFactoryProvider;
		this.em = emFactoryProvider.get().createDmsEntityManager();
		this.repository = repository;
		this.username = username;
		this.pasword = password;
		this.tx = this.em.getTransaction();
	}

	private DmsEntityManager em() {
		return em;
	}
	
	@Override
	public XcpRepository create(Object entity) {
		if (!hasCmdStarted()) {
			throw new IllegalStateException("no command started either by a withTransaction nor a withoutTx call");
		}
		commands.add(cmd().persistCmd(entity));
		return this;
	}

	@Override
	public <T> T find(Class<T> entityClass, SystemId primaryKey) {
		return em().find(entityClass, primaryKey);
	}

	@Override
	public XcpRepository update(Object entity) {
		if (!hasCmdStarted()) {
			throw new IllegalStateException("no command started either by a withTransaction nor a withoutTx call");
		}
		commands.add(cmd().persistCmd(entity));
		return this;
	}

	@Override
	public XcpRepository remove(Object entity) {
		if (!hasCmdStarted()) {
			throw new IllegalStateException("no command started either by a withTransaction nor a withoutTx call");
		}
		commands.add(cmd().removeCmd(entity));
		return this;
	}

	@Override
	public <T> DmsTypedQuery<T> createNativeQuery(String qlString, Class<T> entityClass) {
		return em.createNativeQuery(qlString, entityClass);
	}

	@Override
	public DmsQuery createNativeQuery(String dqlString) {
		return em().createNativeQuery(dqlString);
	}

	@Override
	public XcpRepository withinTransaction() {
		startNewCommand();
		setTxRequested(true);
		return this;
	}

	@Override
	public void commit() {
		if (!hasCmdStarted()) {
			throw new IllegalStateException("no command started either by a withTransaction nor a withoutTx call");
		}
		executeCommand();
	}

	@Override
	public void rollback() {
		if (!hasCmdStarted()) {
			throw new IllegalStateException("no command started either by a withTransaction nor a withoutTx call");
		}
		if (isTxActive()) {
			this.tx.rollback();
		}
	}

	@Override
	public XcpRepository withoutTransaction() {
		startNewCommand();
		setTxRequested(false);
		return this;
	}

	@Override
	public void go() {
		if (!hasCmdStarted()) {
			throw new IllegalStateException("no command started either by a withTransaction nor a withoutTx call");
		}
		executeCommand();
	}

	@Override
	public void abort() {
		if (!hasCmdStarted()) {
			throw new IllegalStateException("no command started either by a withTransaction nor a withoutTx call");
		}
		setTxRequested(false);
		commands.clear();
	}
	
	@Override
	public XcpRepository rollbackOn(Class<? extends Exception> e) {
		throw new NotYetImplemented();
	}

	@Override
	public Transaction getTransaction() {
		return this.tx;
	}

	@Override
	public XcpRepository link(Object parent) {
		rememberParent(parent);
		return this;
	}

	@Override
	public XcpRepository to(Object child) {
		if (this.parent == null) {
			throw new IllegalStateException("No parent given");
		}
		rememberChild(child);
		return this;
	}

	@Override
	public <T> XcpRepository with(Class<T> relationType) throws Exception {
		return with(relationType, null);
			
	}

	@Override
	public <T> XcpRepository with(Class<T> relationType, Map<String, Object> extraAttributes) throws Exception {
		if (this.parent == null) {
			throw new IllegalStateException("No parent given");
		}
		if (this.child == null) {
			throw new IllegalStateException("No child given");
		}
		T instance = relationType.newInstance();
		if (extraAttributes != null) {
			MetaData metaData = em().getMetaData(relationType);
			for (Entry<String, Object> entry : extraAttributes.entrySet()) {
				PersistentProperty prop = metaData.getPersistentProperty(entry.getKey());
				if (prop != null) {
					prop.setProperty(instance, entry.getValue());
				}
			}
		}
		return with(instance);
	}

	@Override
	public XcpRepository with(Object relation) {
		if (this.parent == null) {
			throw new IllegalStateException("No parent given");
		}
		if (this.child == null) {
			throw new IllegalStateException("No child given");
		}
		MetaData metaData = em().getMetaData(relation.getClass());
		if (metaData.getTypeCategory() != XcpTypeCategory.RELATION) {
			throw new IllegalArgumentException("the object type is not a RELATION type");
		}
		final PersistentProperty parentProp = metaData.getParentMethod();
		if (parentProp == null) {
			// TODO create custom exception
			throw new IllegalArgumentException("parent field not found");
		}
		final PersistentProperty childProp = metaData.getChildMethod();
		if (childProp == null) {
			// TODO create custom exception
			throw new IllegalArgumentException("child field not found");
		}
		// set parent & child
		parentProp.setProperty(relation, this.parent);
		childProp.setProperty(relation, this.child);
		// queue the persist cmd for the relation
		cmd().persistCmd(relation);
		return this;
	}

	@Override
	public XcpRepository addAttachment(Object entity, String filename, String contentType) {
		cmd().addAttachmentCmd(entity, filename, contentType);
		return this;
	}

	@Override
	public String getAttachment(Object entity, String filename) {
		return em().getAttachment(entity, filename);
	}

	@Override
	public DmsEntityManager getEntityManager() {
		return this.em;
	}

	private void executeCommand() {
		boolean txStarted = false;
		try {
			if (isTxRequested() && !isTxActive()) {
				tx.begin();
			}
			for (XcpPersistCommand cmd : commands) {
				logger.trace("Executing command: {}", cmd.toString());
				cmd.execute();
			}
		} catch (Exception e) {
			if (txStarted) {
				tx.rollback();
			}
			throw e;
		} finally {
			if (txStarted) {
				tx.commit();
			}
			this.commandInProgress = false;
		}
	}
	
	private boolean isTxActive() {
		return this.tx.isActive();
	}

	private boolean hasCmdStarted() {
		return this.commandInProgress;
	}

	private XcpPersistCommandCatalog cmd() {
		if (cmd == null) {
			cmd = new XcpPersistCommandCatalog(em());
			commands.clear();
		}
		return cmd;
	}

	private void startNewCommand() {
		this.commandInProgress = true;
		commands.clear();
		rememberParent(null);
		rememberChild(null);
	}

	public String getRepository() {
		return repository;
	}

	public String getUsername() {
		return username;
	}

	public String getPasword() {
		return pasword;
	}

	public boolean isTxRequested() {
		return txRequested;
	}

	public void setTxRequested(boolean txRequested) {
		this.txRequested = txRequested;
	}

	public class NotYetImplemented extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
	}
	
	private void rememberParent(Object parent) {
		this.parent = parent;
	}

	private void rememberChild(Object child) {
		this.child = child;
	}

}
