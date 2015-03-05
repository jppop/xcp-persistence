package org.pockito.xcp.test.domain;

import java.util.Collection;

import org.pockito.xcp.annotations.Child;
import org.pockito.xcp.annotations.Parent;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;

@XcpEntity(namespace = "todo")
@XcpType(type = XcpTypeCategory.RELATION, name = "task_person")
public class TaskPerson {

//	@Attribute
//	String role;
//
	@Parent
	Collection<Person> persons; 

	@Child
	Collection<Task> tasks;

	public Collection<Person> getPersons() {
		return persons;
	}

	public void setPersons(Collection<Person> persons) {
		this.persons = persons;
	}

	public Collection<Task> getTasks() {
		return tasks;
	}

	public void setTasks(Collection<Task> tasks) {
		this.tasks = tasks;
	} 
}
