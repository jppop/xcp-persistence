package org.pockito.xcp.config.repository;

import static org.pockito.xcp.entitymanager.query.RightExpression.*;

import java.util.List;

import org.pockito.xcp.config.domain.XcpParameter;
import org.pockito.xcp.config.domain.XcpParameterRepo;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsQuery.OrderDirection;
import org.pockito.xcp.repository.XcpGenericRepoImpl;

public class XcpParameterRepoImpl extends XcpGenericRepoImpl<XcpParameter> implements XcpParameterRepo {

	@Override
	public List<XcpParameter> findByNamespaces(String[] namespaces, String[] typeFilter) {
		
		DmsBeanQuery<XcpParameter> queryPrm = cmd().createBeanQuery(XcpParameter.class);

		queryPrm.setParameter("namespace", in(namespaces));
		if (typeFilter != null) {
			queryPrm.setParameter("configType", in(typeFilter));
		}
		queryPrm.setOrder("namespace", OrderDirection.asc).setOrder("configName", OrderDirection.asc);
		return queryPrm.getResultList();
	}

	@Override
	public XcpParameter findByName(String namespace, String configName) {
		DmsBeanQuery<XcpParameter> queryPrm = cmd().createBeanQuery(XcpParameter.class);

		queryPrm.setParameter("namespace", eq(namespace)).setParameter("configName", eq(configName)).setMaxResults(1);
		List<XcpParameter> prms = queryPrm.getResultList();
		if ( prms.size() == 1) {
			return prms.get(0);
		}
		return null;
	}

}
