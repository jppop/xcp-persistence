package org.pockito.xcp.repository;

import java.util.Map;

import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.Transaction;

public interface XcpRepository {

	// basic CRUD operations
	XcpRepository create(Object entity);
	<T> T find(Class<T> entityClass, SystemId primaryKey);
	XcpRepository update(Object entity);
	XcpRepository remove(Object entity);

    // query
    <T> DmsTypedQuery<T> createNativeQuery(String qlString, Class<T> entityClass);
    DmsQuery createNativeQuery(String dqlString);
    
    // transaction
    XcpRepository withoutTransaction();
    XcpRepository withinTransaction();
//    XcpRepository within(Transaction transaction);
    XcpRepository rollbackOn(Class<? extends Exception> e);
    void commit();
    void rollback();
    void go(); // commit if a transaction has been started
    void abort(); // rollback if a transaction has been started
    Transaction getTransaction();

    // relation
    XcpRepository link(Object parent);
    XcpRepository to(Object child);
    <T> XcpRepository with(Class<T> relationType) throws Exception;
    <T> XcpRepository with(Class<T> relationType, Map<String, Object> extraAttributes) throws Exception;
    XcpRepository with(Object relation);
    
    // content related
    XcpRepository addAttachment(Object entity, String filename, String contentType);
    String getAttachment(Object entity, String filename);

    DmsEntityManager getEntityManager();
}
