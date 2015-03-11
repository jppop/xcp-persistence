package org.pockito.xcp.repository;

import java.util.List;

public interface DmsQuery {

	@SuppressWarnings("rawtypes")
	List getResultList();
	
	int executeUpdate();

	DmsQuery setParameter(String name, Object value);

    DmsQuery setParameter(int position, Object value);
    
    DmsQuery setHint(String hintName, Object value);
    
    DmsQuery setMaxResults(int maxResult);
    
}
