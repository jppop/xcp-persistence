package org.pockito.xcp.sample.todo.repository.impl;

import java.util.List;

import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.repository.XcpGenericRepoImpl;
import org.pockito.xcp.sample.todo.domain.Address;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.domain.PersonAddress;
import org.pockito.xcp.sample.todo.repository.PersonRepo;

public class PersonRepoImpl extends XcpGenericRepoImpl<Person> implements PersonRepo {

	@Override
	public void add(Person person, Address address) {
		
		PersonAddress personAddress = new PersonAddress();
		personAddress.setType("default");
		personAddress.setOrder(-1);
		
		cmd().withinTransaction()
			.create(person)
			.create(address)
			.link(person).to(address).with(personAddress);

		commit();
	}

	@Override
	public void addAddress(Person person, Address address, String type) {
		
		PersonAddress personAddress = new PersonAddress();
		personAddress.setType(type);
		personAddress.setOrder(0);
		
		cmd().withinTransaction()
			.create(address)
			.link(person).to(address).with(personAddress);

		commit();
	}

	public List<Address> findPersonAddresses(Person person) {
		final List<Address> addresses;
		DmsTypedQuery<Address> query = cmd().createChildRelativesQuery(person, PersonAddress.class, Address.class,
				null);
		addresses = query.getResultList();
		return addresses;
	}
	
	@Override
	public void add(Person object) {
		throw new RuntimeException("Must provide a default address");
	}
}
