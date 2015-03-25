package org.pockito.xcp.sample.todo.repository.config;

import org.pockito.xcp.repository.guice.ModuleConfig;
import org.pockito.xcp.sample.todo.repository.PersonRepo;
import org.pockito.xcp.sample.todo.repository.TaskRepo;
import org.pockito.xcp.sample.todo.repository.impl.PersonRepoImpl;
import org.pockito.xcp.sample.todo.repository.impl.TaskRepoImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class GuiceModule extends AbstractModule implements Module {

	@Override
	protected void configure() {
		install(new ModuleConfig());
		bind(PersonRepo.class).to(PersonRepoImpl.class).in(Scopes.SINGLETON);
		bind(TaskRepo.class).to(TaskRepoImpl.class).in(Scopes.SINGLETON);
	}

}
