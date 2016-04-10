package org.pockito.xcp.config.domain;

import java.util.List;

import org.pockito.xcp.repository.XcpGenericRepo;

public interface XcpParameterRepo extends XcpGenericRepo<XcpParameter> {

	List<XcpParameter> findByNamespaces(String[] namespaces);

	XcpParameter findByName(String namespace, String configName);
}
