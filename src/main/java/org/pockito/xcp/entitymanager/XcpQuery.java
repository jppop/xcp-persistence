package org.pockito.xcp.entitymanager;

import java.util.List;

import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsException;
import org.pockito.xcp.repository.DmsQuery;

import com.documentum.fc.client.IDfSession;

public class XcpQuery implements DmsQuery {

	// TODO should be in a super class shared with DctmTypedQuery
	private final XcpEntityManager em;
	private final DctmDriver dctmDriver;
	private final String dqlString;

	public XcpQuery(XcpEntityManager em, String dqlString) {
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

	public XcpEntityManager entityManager() {
		return em;
	}

	public DctmDriver dctmDriver() {
		return dctmDriver;
	}

	public String getDqlString() {
		return dqlString;
	}

	@Override
	public DmsQuery setParameter(String name, Object value) {
		throw new NotYetImplemented();
	}

	@Override
	public DmsQuery setParameter(int position, Object value) {
		throw new NotYetImplemented();
	}

	@Override
	public DmsQuery setHint(String hintName, Object value) {
		throw new NotYetImplemented();
	}

	@Override
	public int setMaxResults(int maxResult) {
		throw new NotYetImplemented();
	}

}
