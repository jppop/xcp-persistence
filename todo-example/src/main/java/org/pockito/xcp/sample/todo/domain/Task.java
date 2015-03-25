package org.pockito.xcp.sample.todo.domain;

import java.util.Date;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.repository.PersistedObject;

@XcpEntity(namespace = "todo")
@XcpType(type = XcpTypeCategory.BUSINESS_OBJECT, name = "task")
public class Task extends PersistedObject {

	@Attribute(name = "subject")
	private String what;
	
	@Attribute
	private String priority; // priority is bound to the attribute 'priority'
	
	@Attribute(name = "due_date")
	private Date dueDate;
	
	@Attribute(name = "r_creation_date", readonly = true)
	private Date creationDate;

	@Attribute(name = "a_status")
	private String status;

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return String.format(
				"Task[id=%s, Subject=%s, priority=%s, creation date=%tc, due date=%tc]",
				getId(), getWhat(), getPriority(),
				creationDate, getDueDate()
				);
	}
}
