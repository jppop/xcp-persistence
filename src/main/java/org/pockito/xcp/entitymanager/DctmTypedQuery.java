package org.pockito.xcp.entitymanager;

import java.util.ArrayList;
import java.util.List;

import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsException;
import org.pockito.xcp.repository.DmsTypedQuery;
import org.pockito.xcp.repository.Parameter;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.IDfId;

public class DctmTypedQuery<T> implements DmsTypedQuery<T> {

	private final DctmEntityManager em;
	private final DctmDriver dctmDriver;
	private final String dqlString;
	private final Class<T> entityClass;
	private final boolean nativeQuery;

	public DctmTypedQuery(DctmEntityManager em, String qlString) {
		this(em, qlString, null, false);
	}

	public DctmTypedQuery(DctmEntityManager em, String qlString, Class<T> entityClass) {
		this(em, qlString, entityClass, false);
	}

	public DctmTypedQuery(DctmEntityManager em, String qlString, Class<T> entityClass, boolean nativeQuery) {
		this.em = em;
		this.dqlString = qlString;
		this.dctmDriver = em.getDctmDriver();
		this.entityClass = entityClass;
		this.nativeQuery = nativeQuery;
	}

	@Override
	public List<T> getResultList() {
		final List<T> resultList;
		if (isNativeQuery()) {
			if (getEntityClass() == null) throw new DmsException("must provide an entity class");
			resultList = executeNativeQuery(getEntityClass());
		} else {
			resultList = null;
			throw new NotYetImplemented();
		}
		return resultList;
	}

	@Override
	public <P> DmsTypedQuery<T> setParameter(Parameter<P> param, P value) {
		throw new NotYetImplemented();
	}

	public DctmEntityManager em() {
		return em;
	}

	public DctmDriver dctmDriver() {
		return dctmDriver;
	}

	public String getDqlString() {
		return dqlString;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public boolean isNativeQuery() {
		return nativeQuery;
	}

	private List<T> executeNativeQuery(Class<T> entityClass) {
		final List<IDfId> rawResultList;
		final List<T> resultList = new ArrayList<T>();
		IDfSession session = dctmDriver().getSession();
		try {
			// TODO: Not optimized! Should use a query with the entity fields as columns
			rawResultList = dctmDriver().getObjectsByQuery(session, getDqlString());
			for (IDfId objectId : rawResultList) {
				T newInstance = em().find(entityClass, objectId.toString());
				resultList.add(newInstance);
			}
		} finally {
			dctmDriver().releaseSession(session);
		}
		return resultList;
	}
}
