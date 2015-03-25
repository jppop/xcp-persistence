package org.pockito.xcp.sample.todo.domain;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.repository.PersistedObject;

@XcpEntity(namespace = "todo")
@XcpType(type = XcpTypeCategory.BUSINESS_OBJECT, name = "person")
public class Person extends PersistedObject {

	@Attribute(name = "first_name")
	private String firstName;

	@Attribute(name = "last_name")
	private String lastName;

	@Attribute(name = "object_name")
	private String name;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format(
				"Person[id=%s, First Name=%s, Last Name=%s]",
				getId(), 
				getFirstName(), getLastName()
				);
	}
}
