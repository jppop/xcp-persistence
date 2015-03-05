package org.pockito.xcp.repository;

import java.util.List;

public interface DmsTypedQuery<T> {

	List<T> getResultList();
	
	DmsTypedQuery<T> setParameter(String name, Object value);

	DmsTypedQuery<T> setParameter(int position, Object value);
    
	DmsTypedQuery<T> setHint(String hintName, Object value);
    
	DmsTypedQuery<T> setMaxResults(int maxResult);
}
