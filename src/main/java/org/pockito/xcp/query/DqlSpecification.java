package org.pockito.xcp.query;

public interface DqlSpecification<T> extends Specification<T> {

	void set(Class<T> entity, String whereClause);
}
