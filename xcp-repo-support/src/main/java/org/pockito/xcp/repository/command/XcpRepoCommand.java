package org.pockito.xcp.repository.command;

import java.util.Map;

import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
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
    void abort(); // rollback if a transaction has been started
    Transaction getTransaction();

    // relation
    XcpRepoCommand link(Object parent);
    XcpRepoCommand to(Object child);
    <T> XcpRepoCommand with(Class<T> relationType);
    <T> XcpRepoCommand with(Class<T> relationType, Map<String, Object> extraAttributes);
    XcpRepoCommand with(Object relation);
    
    // content related
    XcpRepoCommand addAttachment(Object entity, String filename, String contentType);
    String getAttachment(Object entity, String filename);
    XcpRepoCommand removeAttachment(Object entity);

    // helper methods
    DmsEntityManager getEntityManager();
    int size();

    // no more used
    void setOwner(Object owner);
    Object getOwner();
    boolean isOwner(Object owner);
    
}
