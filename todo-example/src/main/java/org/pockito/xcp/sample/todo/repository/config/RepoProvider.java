package org.pockito.xcp.sample.todo.repository.config;

import javax.inject.Inject;
import javax.inject.Named;

import org.pockito.xcp.repository.PersistedObject;
import org.pockito.xcp.repository.XcpGenericRepo;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.domain.Task;
import org.pockito.xcp.sample.todo.repository.PersonRepo;
import org.pockito.xcp.sample.todo.repository.TaskRepo;

public class RepoProvider {

	@Inject
	@Named("PersonRepo")
	private PersonRepo personRepo;
	
	@Inject
	@Named("TaskRepo")
	private TaskRepo taskRepo;
	
	public RepoProvider() {
	}
	
	public XcpGenericRepo<?> getRepo(Class<? extends PersistedObject> entityClass) {
		if (entityClass.isAssignableFrom(Person.class)) {
			return personRepo;
		} else if (entityClass.isAssignableFrom(Task.class)) {
			return taskRepo;
		}
		return null;
	}
}
