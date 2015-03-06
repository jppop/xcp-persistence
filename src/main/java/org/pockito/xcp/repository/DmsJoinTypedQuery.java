package org.pockito.xcp.repository;

import java.util.List;

public interface DmsJoinTypedQuery<T, B> extends DmsTypedQuery<T> {

	DmsJoinTypedQuery<T, B> select();
	DmsJoinTypedQuery<T, B> join(Class<T> relation);
	DmsJoinTypedQuery<T, B> on(JoinType joinType);
	DmsJoinTypedQuery<T, B> where(String qualification);
	DmsJoinTypedQuery<T, B> setParameter(JoinType name, Object value);
	List<B> getJoinedResultList();
}