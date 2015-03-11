package org.pockito.xcp.repository;

import java.util.List;

import org.pockito.xcp.query.BasicSpecification;
import org.pockito.xcp.query.DqlCountSpecification;
import org.pockito.xcp.query.DqlSpecification;
import org.pockito.xcp.query.FolderPath;

public interface XcpRepository {

	// basic CRUD operations
    void create(Object entity);
    void create(Object entity, FolderPath folder);
	<T> T find(Class<T> entityClass, SystemId primaryKey);
    void update(Object entity);
    void delete(Object entity);

    <T> List<T> query(DqlSpecification<T> spec);
    <T> int query(DqlCountSpecification<T> spec);
    <T> List<T> query(Class<T> entity, BasicSpecification query);
//    <P, C> List<C> findChildren(Object entity, Relation<P, C> relation);
//    <P, C> List<P> findParents(Object entity, Relation<P, C> relation);
    
    XcpRepository within(Transaction transaction);

    XcpRepository link(Object entity);
    XcpRepository to(Object entity);
//    <P, C> XcpRepository with(Relation<P, C> relation);
    
    XcpRepository copy(Object entity);
    XcpRepository move(Object entity);
    XcpRepository to(FolderPath folder);
    FolderPath locate(Object entity);
    List<FolderPath> locateAll(Object entity);

//    <P, C> void relate(Object parent, Object child, Relation<P, C> relation);
    void link(Object parent, FolderPath folder);
    void unlink(Object parent, FolderPath folder);
    
}
