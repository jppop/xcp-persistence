package org.pockito.xcp.repository;

import javax.inject.Inject;
import javax.inject.Provider;

public class XcpRepositoryFactory {

	@Inject
	private static Provider<XcpRepository> repoProvider;
	
	public XcpRepository create() {
		return repoProvider.get();
	}

	static class InstanceHolder {
		static XcpRepositoryFactory instance = new XcpRepositoryFactory();
	}

	public static XcpRepositoryFactory getInstance() {
		return InstanceHolder.instance;
	}
}
