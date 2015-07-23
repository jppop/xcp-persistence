package org.pockito.xcp.repository.command;

import java.util.Map;

import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.Transaction;

/**
 * The gateway between a repository class and the entity manager.
 * 
 * @author jfrancon
 *
 */
public interface XcpRepoCommand {

	public interface FilenameBuilder {
		String buildFilename(Class<?> entity);
	}
	
	// basic CRUD operations
	XcpRepoCommand create(Object entity);
	<T> T find(Class<T> entityClass, Object primaryKey);
	XcpRepoCommand update(Object entity);
	XcpRepoCommand remove(Object entity);

    // query
    <T> DmsTypedQuery<T> createNativeQuery(String dqlString, Class<T> entityClass);
    DmsQuery createNativeQuery(String dqlString);
	<T, R> DmsTypedQuery<T> createChildRelativesQuery(Object parent, Class<R> relationClass, Class<T> childClass, String optionalDqlFilter);
	<T, R> DmsTypedQuery<T> createParentRelativesQuery(Object child, Class<R> relationClass, Class<T> parentClass, String optionalDqlFilter);
	<T> DmsBeanQuery<T> createBeanQuery(Class<T> entityClass);
    
    // transaction
    XcpRepoCommand withoutTransaction();
    XcpRepoCommand withinTransaction();
//    XcpRepository within(Transaction transaction);
    XcpRepoCommand rollbackOn(Class<? extends Exception> e);
    void commit();
    void rollback();
    void go(); // commit if a transaction has been started
    void abort(); // roll back if a transaction has been started
    Transaction getTransaction();

    // relation
    XcpRepoCommand link(Object parent);
    XcpRepoCommand to(Object child);
    <T> XcpRepoCommand with(Class<T> relationType);
    <T> XcpRepoCommand with(Class<T> relationType, Map<String, Object> extraAttributes);
    XcpRepoCommand with(Object relation);
    
    // content related
    XcpRepoCommand addAttachment(Object entity, String filename, String contentType);
    String getAttachment(Object entity, String folder, String filename);
    String getAttachment(Object entity, String folder, String filename, String contentType);
    XcpRepoCommand removeAttachment(Object entity);
    void setContentDownloaderRootPath(String path);
    String getRootPath();
    void setFilenameBuilder(FilenameBuilder builder);

    // connect the Command to the repository (through an entity manager)
	void connect(String repository, String username, String password);
	void connect();
	String getPassword();
	String getUsername();
	String getRepository();

	// helper methods
    DmsEntityManager getEntityManager();
    int size();

    void setOwner(Object owner);
    Object getOwner();
    boolean isOwner(Object owner);
	boolean isCmdInProgress();
    
}
