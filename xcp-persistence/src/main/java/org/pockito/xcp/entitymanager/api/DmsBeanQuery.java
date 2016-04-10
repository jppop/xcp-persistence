package org.pockito.xcp.entitymanager.api;

import java.util.List;

import org.pockito.xcp.entitymanager.query.RightExpression;

public interface DmsBeanQuery<T> extends DmsTypedQuery<T> {

	enum QueryType {
		select, update, delete
		;
	}
	List<T> getResultList();

	<B> DmsBeanQuery<T> setParameter(String fieldName, RightExpression<B> op);
	
	void setQueryType(QueryType type);
	
	int executeUpdate(boolean yesIReallyWantDeleteAll);
	
	DmsBeanQuery<T> setHint(String hintName, Object value);
    
	DmsBeanQuery<T> setMaxResults(int maxResult);

	DmsBeanQuery<T> setOrder(String fieldName, OrderDirection direction);
}
