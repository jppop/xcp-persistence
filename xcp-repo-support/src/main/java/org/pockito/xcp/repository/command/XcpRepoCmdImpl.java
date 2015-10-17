package org.pockito.xcp.repository.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.entitymanager.PropertyConstants;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsEntityManagerFactory;
import org.pockito.xcp.entitymanager.api.DmsQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.entitymanager.api.Transaction;
import org.pockito.xcp.repository.Message;
import org.pockito.xcp.repository.NotYetImplemented;
import org.pockito.xcp.repository.XcpRepoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XcpRepoCmdImpl implements XcpRepoCommand {

	private Logger logger = LoggerFactory.getLogger(XcpRepoCmdImpl.class);

	private final DmsEntityManagerFactory emFactory;
	private DmsEntityManager em;
	private Transaction tx = null;

	@Inject
	@Named("org.pockito.xcp.repository.name")
	private String repository;

	@Inject
	@Named("org.pockito.xcp.repository.username")
	private String username;

	@Inject
	@Named("org.pockito.xcp.repository.password")
	private String password;

	private XcpPersistCommandCatalog cmd = null;
	private final List<XcpPersistCommand> commands = new ArrayList<XcpPersistCommand>();

	private boolean txRequested = false;
	private Object parent;
	private Object child;

	private boolean commandInProgress = false;

	private Object owner;

	private String rootPath;

	@Inject
	XcpRepoCmdImpl(DmsEntityManagerFactory emFactory) {
		this.emFactory = emFactory;
	}
	
	@Override
	public void connect() {
		connect(this.repository, this.username, this.password);
	}

	@Override
	public void connect(String repository, String username, String password) {
		checkNotNull(repository);
		this.repository = repository;
		this.username = username;
		this.password = password;
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(PropertyConstants.Repository, repository);
		props.put(PropertyConstants.Username, username);
		props.put(PropertyConstants.Password, password);
		this.em = this.emFactory.createDmsEntityManager(props);
	}

	private DmsEntityManager em() {
		return em;
	}

	private Transaction tx() {
		if (tx == null) {
			tx = em().getTransaction();
		}
		return tx;
	}

	@Override
	public XcpRepoCommand create(Object entity) {
		if (!isCmdInProgress()) {
			throw new IllegalStateException(Message.E_CMD_NO_STARTED.get());
		}
		commands.add(cmd().persistCmd(entity));
		return this;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return em().find(entityClass, primaryKey);
	}

	@Override
	public XcpRepoCommand update(Object entity) {
		if (!isCmdInProgress()) {
			throw new IllegalStateException(Message.E_CMD_NO_STARTED.get());
		}
		commands.add(cmd().persistCmd(entity));
		return this;
	}

	@Override
	public XcpRepoCommand remove(Object entity) {
		if (!isCmdInProgress()) {
			throw new IllegalStateException(Message.E_CMD_NO_STARTED.get());
		}
		commands.add(cmd().removeCmd(entity));
		return this;
	}

	@Override
	public <T> DmsTypedQuery<T> createNativeQuery(String qlString, Class<T> entityClass) {
		return em().createNativeQuery(qlString, entityClass);
	}

	@Override
	public DmsQuery createNativeQuery(String dqlString) {
		return em().createNativeQuery(dqlString);
	}

	@Override
	public <T, R> DmsTypedQuery<T> createChildRelativesQuery(Object parent, Class<R> relationClass,
			Class<T> childClass, String optionalDqlFilter) {
		return em().createChildRelativesQuery(parent, relationClass, childClass, optionalDqlFilter);
	}

	@Override
	public <T, R> DmsTypedQuery<T> createParentRelativesQuery(Object child, Class<R> relationClass,
			Class<T> parentClass, String optionalDqlFilter) {
		return em().createParentRelativesQuery(child, relationClass, parentClass, optionalDqlFilter);
	}

	@Override
	public XcpRepoCommand withinTransaction() {
		if (!isCmdInProgress()) {
			startNewCommand();
		}
		setTxRequested(true);
		return this;
	}

	@Override
	public void commit() {
		if (!isCmdInProgress()) {
			throw new IllegalStateException(Message.E_CMD_NO_STARTED.get());
		}
		executeCommand();
	}

	@Override
	public void rollback() {
		if (!isCmdInProgress()) {
			throw new IllegalStateException(Message.E_CMD_NO_STARTED.get());
		}
		if (isTxActive()) {
			this.tx().rollback();
		}
	}

	@Override
	public XcpRepoCommand withoutTransaction() {
		if (!isCmdInProgress()) {
			startNewCommand();
		}
		setTxRequested(false);
		return this;
	}

	@Override
	public void go() {
		if (!isCmdInProgress()) {
			throw new IllegalStateException(Message.E_CMD_NO_STARTED.get());
		}
		executeCommand();
	}

	@Override
	public void abort() {
		if (!isCmdInProgress()) {
			throw new IllegalStateException(Message.E_CMD_NO_STARTED.get());
		}
		setTxRequested(false);
		commands.clear();
	}

	@Override
	public XcpRepoCommand rollbackOn(Class<? extends Exception> e) {
		throw new NotYetImplemented();
	}

	@Override
	public Transaction getTransaction() {
		return this.tx;
	}

	@Override
	public XcpRepoCommand link(Object parent) {
		rememberParent(parent);
		return this;
	}

	@Override
	public XcpRepoCommand to(Object child) {
		if (this.parent == null) {
			throw new IllegalStateException(Message.E_NO_PARENT.get());
		}
		rememberChild(child);
		return this;
	}

	@Override
	public <T> XcpRepoCommand with(Class<T> relationType) {
		return with(relationType, null);

	}

	@Override
	public <T> XcpRepoCommand with(Class<T> relationType, Map<String, Object> extraAttributes) {
		if (this.parent == null) {
			throw new IllegalStateException(Message.E_NO_PARENT.get());
		}
		if (this.child == null) {
			throw new IllegalStateException(Message.E_NO_CHILD.get());
		}
		T instance;
		try {
			instance = relationType.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new XcpRepoException(Message.E_INSTANCIATION_FAILED.get(relationType.getName()));
		}
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
	public XcpRepoCommand with(Object relation) {
		if (this.parent == null) {
			throw new IllegalStateException(Message.E_NO_PARENT.get());
		}
		if (this.child == null) {
			throw new IllegalStateException(Message.E_NO_CHILD.get());
		}
		MetaData metaData = em().getMetaData(relation.getClass());
		if (metaData.getTypeCategory() != XcpTypeCategory.RELATION) {
			throw new IllegalArgumentException(Message.E_NOT_RELATION_TYPE.get(relation.getClass().getName()));
		}
		final PersistentProperty parentProp = metaData.getParentMethod();
		if (parentProp == null) {
			throw new XcpRepoException(Message.E_NO_PARENT_SETTER.get(relation.getClass().getName()));
		}
		final PersistentProperty childProp = metaData.getChildMethod();
		if (childProp == null) {
			throw new XcpRepoException(Message.E_NO_CHILD_SETTER.get(relation.getClass().getName()));
		}
		// set parent & child
		parentProp.setProperty(relation, this.parent);
		childProp.setProperty(relation, this.child);
		// queue the persist cmd for the relation
		commands.add(cmd().persistCmd(relation));
		return this;
	}

	@Override
	public XcpRepoCommand addAttachment(Object entity, String filename, String contentType) {
		commands.add(cmd().addAttachmentCmd(entity, filename, contentType));
		return this;
	}

	@Override
	public String getAttachment(Object entity, String folder, String filename) {
		return em().getAttachment(entity, folder, filename);
	}
	
	@Override
	public String getAttachment(Object entity, String folder, String filename, String contentType) {
		return em().getAttachment(entity, folder, filename, contentType);
	}

	@Override
	public DmsEntityManager getEntityManager() {
		return this.em;
	}

	@Override
	public void setOwner(Object owner) {
		this.owner = owner;
	}

	@Override
	public Object getOwner() {
		return this.owner;
	}

	@Override
	public boolean isOwner(Object owner) {
		return this.owner != null && this.owner == owner;
	}

	@Override
	public int size() {
		return commands.size();
	}

	@Override
	public XcpRepoCommand removeAttachment(Object entity) {
		throw new NotYetImplemented();
	}

	@Override
	public <T> DmsBeanQuery<T> createBeanQuery(Class<T> entityClass) {
		return em().createBeanQuery(entityClass);
	}

	private void executeCommand() {
		boolean txStarted = false;
		try {
			if (isTxRequested() && !isTxActive()) {
				tx().begin();
				txStarted = true;
			}
			for (XcpPersistCommand cmd : commands) {
				logger.trace("Executing command: {}", cmd.toString());
				cmd.execute();
			}
			if (txStarted) {
				tx().commit();
			}
		} catch (Exception e) {
			logger.trace("rolling back", e);
			// logger.trace("Commands: {}", commands.toString());
			if (txStarted) {
				tx().rollback();
			}
			throw e;
		} finally {
			this.commandInProgress = false;
			commands.clear();
		}
	}

	private boolean isTxActive() {
		return this.tx().isActive();
	}

	@Override
	public boolean isCmdInProgress() {
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

	@Override
	public String getRepository() {
		return repository;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public boolean isTxRequested() {
		return txRequested;
	}

	public void setTxRequested(boolean txRequested) {
		this.txRequested = txRequested;
	}

	private void rememberParent(Object parent) {
		this.parent = parent;
	}

	private void rememberChild(Object child) {
		this.child = child;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void setContentDownloaderRootPath(String path) {
		rootPath = path;
	}
	
	@Override
	public String getRootPath() {
		return rootPath;
	}

	@Override
	public void setFilenameBuilder(FilenameBuilder builder) {
		// TODO Auto-generated method stub
		
	}

}
