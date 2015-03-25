package org.pockito.xcp.sample.todo.repository;

import java.util.List;

import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.repository.XcpGenericRepo;
import org.pockito.xcp.sample.todo.domain.Address;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.repository.impl.NotUniqueException;

public interface PersonRepo extends XcpGenericRepo<Person> {
	
	/**
	 * Adds a new person with its default address.
	 * 
	 * @param person
	 * @param address
	 */
	void add(Person person, Address address);

	/**
	 * Adds an address to an existing person.
	 * 
	 * @param person
	 * @param address
	 * @param type
	 */
	public void addAddress(Person person, Address address, String type);

	public abstract Person findByName(String name);

}
