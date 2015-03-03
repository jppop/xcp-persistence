package org.pockito.xcp.repository;

import java.util.List;

public interface DmsTypedQuery<T> extends DmsQuery {

	List<T> getResultList();
	
	<P> DmsTypedQuery<T> setParameter(Parameter<P> param, P value);
}
