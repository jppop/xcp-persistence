package org.pockito.xcp.config;

import org.pockito.xcp.config.domain.XcpParameterRepo;
import org.pockito.xcp.config.repository.XcpParameterRepoImpl;
import org.pockito.xcp.repository.guice.ModuleConfig;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class GuiceModule extends AbstractModule implements Module {

	@Override
	protected void configure() {
		install(new ModuleConfig());
		bind(XcpParameterRepo.class).annotatedWith(Names.named("XcpParameterRepo"))
			.to(XcpParameterRepoImpl.class).in(Scopes.SINGLETON);
	}

}
