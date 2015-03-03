package org.pockito.xcp.entitymanager;

import java.util.List;

import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsException;
import org.pockito.xcp.repository.DmsQuery;
import org.pockito.xcp.repository.Parameter;

import com.documentum.fc.client.IDfSession;

public class DctmQuery implements DmsQuery {

	// TODO should be in a super class shared with DctmTypedQuery
	private final DctmEntityManager em;
	private final DctmDriver dctmDriver;
	private final String dqlString;

	public DctmQuery(DctmEntityManager em, String dqlString) {
		this.em = em;
		this.dqlString = dqlString;
		this.dctmDriver = em.getDctmDriver();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getResultList() throws DmsException {
		final List resultList;
		IDfSession session = dctmDriver().getSession();
		try {
			resultList = dctmDriver().getObjectsByQuery(session, getDqlString());
		} finally {
			dctmDriver().releaseSession(session);
		}
		return resultList;
	}

	@Override
	public <P> DmsQuery setParameter(Parameter<P> param, P value) {
		throw new NotYetImplemented();
	}

	public DctmEntityManager entityManager() {
		return em;
	}

	public DctmDriver dctmDriver() {
		return dctmDriver;
	}

	public String getDqlString() {
		return dqlString;
	}

}
