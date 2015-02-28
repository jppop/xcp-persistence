package org.pockito.xcp.query;

public interface DqlSpecification<T> extends Specification<T> {

	public void set(Class<T> entity, String whereClause);
}
