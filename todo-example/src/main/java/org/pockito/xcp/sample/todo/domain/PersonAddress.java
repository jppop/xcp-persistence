package org.pockito.xcp.sample.todo.domain;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.Child;
import org.pockito.xcp.annotations.Parent;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.repository.PersistedObject;

@XcpEntity(namespace = "todo")
@XcpType(type = XcpTypeCategory.RELATION, name = "person_address")
public class PersonAddress extends PersistedObject {

	@Attribute(name ="address_type")
	String type;

	@Attribute(name = "order_no")
	int order;
	
	@Parent
	Person person; 

	@Child
	Address address;

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return String.format(
				"PersonAddress [id=%s, person=%s, address=%s, type=%s, vstamp=%d, oder=%d]",
				getId(), 
				getPerson(), getAddress(), getType(),
				getvStamp(), getOrder()
				);
	}

}
