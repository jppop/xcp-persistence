package org.pockito.xcp.test.domain;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.Child;
import org.pockito.xcp.annotations.Parent;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;

@XcpEntity(namespace = "todo")
@XcpType(type = XcpTypeCategory.RELATION, name = "task_person")
public class TaskPerson extends PersistedObject {

//	@Attribute
//	String role;
//
	@Attribute(name = "order_no")
	int order;
	
	@Child
	Person person; 

	@Parent
	Task task;

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return String.format(
				"TaskPerson[id=%s, task=%s, person=%s, vstamp=%d, oder=%d]",
				getId(), 
				getTask(), getPerson(),
				getvStamp(), getOrder()
				);
	}

}
