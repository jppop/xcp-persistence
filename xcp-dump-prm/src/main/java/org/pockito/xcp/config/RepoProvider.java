package org.pockito.xcp.config;

import javax.inject.Inject;
import javax.inject.Named;

import org.pockito.xcp.config.domain.PersistedObject;
import org.pockito.xcp.config.domain.XcpParameter;
import org.pockito.xcp.config.domain.XcpParameterRepo;
import org.pockito.xcp.repository.XcpGenericRepo;

public class RepoProvider {

	@Inject
	@Named("XcpParameterRepo")
	private XcpParameterRepo paramRepo;
	
	public RepoProvider() {
	}
	
	public XcpGenericRepo<?> getRepo(Class<? extends PersistedObject> entityClass) {
		if (entityClass.isAssignableFrom(XcpParameter.class)) {
			return paramRepo;
		}
		return null;
	}
}
