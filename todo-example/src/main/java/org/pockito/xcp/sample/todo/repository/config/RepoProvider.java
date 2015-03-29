package org.pockito.xcp.sample.todo.repository.config;

import javax.inject.Inject;

import org.pockito.xcp.repository.PersistedObject;
import org.pockito.xcp.repository.XcpGenericRepo;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.domain.Task;
import org.pockito.xcp.sample.todo.repository.PersonRepo;
import org.pockito.xcp.sample.todo.repository.TaskRepo;

public class RepoProvider {

	@Inject
	private PersonRepo personRepo;
	
	@Inject
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
