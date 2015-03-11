package org.pockito.xcp.query;

public interface DqlCountSpecification<T> extends Specification<T> {

	void set(Class<T> entity, String whereClause);
}
