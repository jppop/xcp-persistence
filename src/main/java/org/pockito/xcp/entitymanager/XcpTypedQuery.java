package org.pockito.xcp.entitymanager;

import java.util.List;

import org.pockito.xcp.repository.DmsException;
import org.pockito.xcp.repository.DmsTypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XcpTypedQuery<T> extends AbstractTypedQuery<T> implements DmsTypedQuery<T> {

	static final Logger logger = LoggerFactory.getLogger(XcpTypedQuery.class);
	
	public XcpTypedQuery(XcpEntityManager em, String qlString) {
		this(em, qlString, null, false);
	}

	public XcpTypedQuery(XcpEntityManager em, String qlString, Class<T> entityClass) {
		this(em, qlString, entityClass, false);
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
				throw new DmsException("must provide an entity class");
			}
			logger.debug("XcpQuery: querying with a native DQL query");
			resultList = (List<T>) executeNativeQuery(getEntityClass(), getDqlString());
			logger.debug("XcpQuery: done");
		} else {
			throw new NotYetImplemented();
		}
		return resultList;
	}

}
