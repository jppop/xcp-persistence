package org.pockito.xcp.repository.command;

import java.util.Map;

import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.Transaction;

public interface XcpRepoCommand {

	// basic CRUD operations
	XcpRepoCommand create(Object entity);
	<T> T find(Class<T> entityClass, Object primaryKey);
	XcpRepoCommand update(Object entity);
	XcpRepoCommand remove(Object entity);

    // query
    <T> DmsTypedQuery<T> createNativeQuery(String dqlString, Class<T> entityClass);
    DmsQuery createNativeQuery(String dqlString);
    
    // transaction
    XcpRepoCommand withoutTransaction();
    XcpRepoCommand withinTransaction();
//    XcpRepository within(Transaction transaction);
    XcpRepoCommand rollbackOn(Class<? extends Exception> e);
    void commit();
    void rollback();
    void go(); // commit if a transaction has been started
    void abort(); // rollback if a transaction has been started
    Transaction getTransaction();

    // relation
    XcpRepoCommand link(Object parent);
    XcpRepoCommand to(Object child);
    <T> XcpRepoCommand with(Class<T> relationType) throws Exception;
    <T> XcpRepoCommand with(Class<T> relationType, Map<String, Object> extraAttributes) throws Exception;
    XcpRepoCommand with(Object relation);
    
    // content related
    XcpRepoCommand addAttachment(Object entity, String filename, String contentType);
    String getAttachment(Object entity, String filename);

    // helper methods
    DmsEntityManager getEntityManager();
    int size();

    // no more used
    void setOwner(Object owner);
    Object getOwner();
    boolean isOwner(Object owner);
    
}
