package org.pockito.xcp.entitymanager.query;

import java.util.List;

import org.pockito.xcp.entitymanager.NotYetImplemented;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.api.DmsException;
import org.pockito.xcp.entitymanager.api.DmsQuery;

import com.documentum.fc.client.IDfSession;

public class XcpQuery extends AbstractQuery implements DmsQuery {

	public XcpQuery(XcpEntityManager em, String dqlString) {
		super(em, dqlString, true);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getResultList() throws DmsException {
		final List resultList;
		IDfSession session = dctmDriver().getSession();
		try {
			resultList = dctmDriver().getObjectsByQuery(session, getQuery());
		} finally {
			dctmDriver().releaseSession(session);
		}
		return resultList;
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
	public DmsQuery setMaxResults(int maxResult) {
		throw new NotYetImplemented();
	}

}
