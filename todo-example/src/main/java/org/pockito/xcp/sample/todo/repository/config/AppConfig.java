package org.pockito.xcp.sample.todo.repository.config;

import org.pockito.xcp.repository.PersistedObject;
import org.pockito.xcp.repository.XcpGenericRepo;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.domain.Task;
import org.pockito.xcp.sample.todo.repository.PersonRepo;
import org.pockito.xcp.sample.todo.repository.TaskRepo;

import com.google.inject.Guice;
import com.google.inject.Injector;

public enum AppConfig {

	instance;
	
	private static RepoProvider repoProvider = new RepoProvider();
	
	public PersonRepo getPersonRepo() {
		return (PersonRepo) repoProvider.getRepo(Person.class);
	}
	
	public TaskRepo getTaskRepo() {
		return (TaskRepo) repoProvider.getRepo(Task.class);
	}
	
	public XcpGenericRepo<?> getRepo(Class<? extends PersistedObject> entityClass) {
		return repoProvider.getRepo(entityClass);
	}
	
	static {
		Injector injector = Guice.createInjector(new GuiceModule());
		injector.injectMembers(repoProvider);
	}
}
