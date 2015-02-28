package org.pockito.xcp.repository;

import java.util.List;

import org.pockito.xcp.query.BasicSpecification;
import org.pockito.xcp.query.DqlCountSpecification;
import org.pockito.xcp.query.DqlSpecification;
import org.pockito.xcp.query.FolderPath;

public interface XcpRepository {

	// basic CRUD operations
    public void create(Object entity);
    public void create(Object entity, FolderPath folder);
	public <T> T find(Class<T> entityClass, SystemId primaryKey);
    public void update(Object entity);
    public void delete(Object entity);

    public <T> List<T> query(DqlSpecification<T> spec);
    public <T> int query(DqlCountSpecification<T> spec);
    public <T> List<T> query(Class<T> entity, BasicSpecification query);
    public <P, C> List<C> findChildren(Object entity, Relation<P, C> relation);
    public <P, C> List<P> findParents(Object entity, Relation<P, C> relation);
    
    public XcpRepository within(Transaction transaction);

    public XcpRepository link(Object entity);
    public XcpRepository to(Object entity);
    public <P, C> XcpRepository with(Relation<P, C> relation);
    
    public XcpRepository copy(Object entity);
    public XcpRepository move(Object entity);
    public XcpRepository to(FolderPath folder);
    public FolderPath locate(Object entity);
    public List<FolderPath> locateAll(Object entity);

    public <P, C> void relate(Object parent, Object child, Relation<P, C> relation);
    public void link(Object parent, FolderPath folder);
    public void unlink(Object parent, FolderPath folder);
    
}
