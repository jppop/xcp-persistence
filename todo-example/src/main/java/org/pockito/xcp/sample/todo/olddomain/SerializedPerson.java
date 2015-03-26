package org.pockito.xcp.sample.todo.olddomain;


public class SerializedPerson {

	private String id;
	
	private String firstname;

	private String lastname;

	private String name;
	
	private String email;
	
	private SerializedAddress address;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public SerializedAddress getAddress() {
		return address;
	}

	public void setAddress(SerializedAddress address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return String.format(
				"SerializedPerson [Id=%s, First Name=%s, Last Name=%s, email=%s, address=%s]",
				getId(), getFirstname(), getLastname(), getEmail(), getAddress().toString()
				);
	}

}
