package org.pockito.xcp.entitymanager.api;

import java.util.List;

public interface DmsBeanQuery<T> extends DmsTypedQuery<T> {

	T bean();
	
	List<T> getResultList();
	
}
