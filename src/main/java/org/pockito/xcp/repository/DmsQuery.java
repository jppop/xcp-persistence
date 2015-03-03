package org.pockito.xcp.repository;

import java.util.List;

public interface DmsQuery {

	@SuppressWarnings("rawtypes")
	List getResultList();
	
	<P> DmsQuery setParameter(Parameter<P> param, P value);
}
