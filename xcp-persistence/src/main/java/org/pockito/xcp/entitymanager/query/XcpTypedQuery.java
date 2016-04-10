package org.pockito.xcp.entitymanager.query;

import java.util.List;

import org.pockito.xcp.entitymanager.NotYetImplemented;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.api.DmsException;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XcpTypedQuery<T> extends AbstractTypedQuery<T> implements DmsTypedQuery<T> {

	static final Logger logger = LoggerFactory.getLogger(XcpTypedQuery.class);
	
	public XcpTypedQuery(XcpEntityManager em, String qlString) {
		this(em, qlString, false);
	}

	public XcpTypedQuery(XcpEntityManager em, String qlString, boolean nativeQuery) {
		super(em, qlString, nativeQuery);
	}

	public XcpTypedQuery(XcpEntityManager em, String qlString, Class<T> entityClass, boolean nativeQuery) {
		super(em, qlString, entityClass, nativeQuery);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getResultList() {
		final List<T> resultList;
		if (isNativeQuery()) {
			if (getEntityClass() == null) {
				throw new DmsException(Message.E_NO_ENTITY.get());
			}
			logger.debug("XcpQuery: querying with a native DQL query");
			resultList = (List<T>) executeNativeQuery(getEntityClass(), getQuery());
			logger.debug("XcpQuery: done");
		} else {
			throw new NotYetImplemented();
		}
		return resultList;
	}

	@Override
	public DmsTypedQuery<T> setOrder(String property, OrderDirection direction) {
		return (DmsTypedQuery<T>) super.setOrder(property, direction);
	}

}
