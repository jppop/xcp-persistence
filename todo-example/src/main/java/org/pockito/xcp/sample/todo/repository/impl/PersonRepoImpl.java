package org.pockito.xcp.sample.todo.repository.impl;

import java.util.List;

import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.repository.XcpGenericRepoImpl;
import org.pockito.xcp.sample.todo.domain.Address;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.domain.PersonAddress;
import org.pockito.xcp.sample.todo.repository.PersonRepo;

import static org.pockito.xcp.entitymanager.query.RightExpression.*;

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

	@Override
	public void remove(Person person) {
		
		cmd().withinTransaction();
		
		// remove first the person addresses
		List<Address> addresses = findPersonAddresses(person);
		for (Address address : addresses) {
			cmd().remove(address);
		}
		super.remove(person);
	}
	
	@Override
	public Person findByName(String name) {
	
		DmsBeanQuery<Person> query = cmd().createBeanQuery(Person.class);
		query.setParameter("name", eq(name));
		
		List<Person> persons = query.getResultList();
		if (persons.isEmpty()) {
			return null;
		}
		if (persons.size() > 1) {
			throw new NotUniqueException();
		}
		return persons.get(0);
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
