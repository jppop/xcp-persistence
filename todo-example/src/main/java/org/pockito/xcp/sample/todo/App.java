package org.pockito.xcp.sample.todo;

import org.pockito.xcp.sample.todo.domain.Address;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.repository.PersonRepo;
import org.pockito.xcp.sample.todo.repository.config.AppConfig;

public class App {

	public static void main(String[] args) {
		new App().addSomePersons();
	}
	
	public void addSomePersons() {
		
		PersonRepo repo = AppConfig.instance.getPersonRepo();
		
		Person person = repo.findByName("James Bond");
		if (person != null) {
			// TODO: kill 007 and its addresses, then resurrect him. 
			return;
		}
		person = new Person();
		
		person.setFirstName("James");
		person.setLastName("Bond");
		
		
		Address jamesAddr = new Address();
		jamesAddr.setStreetLine1("MI6 Building");
		jamesAddr.setStreetLine2("PO Box 1300");
		jamesAddr.setCity("London");
		jamesAddr.setCountry("uk");
		jamesAddr.setZipcode("SE1 1BD");
		
		AppConfig.instance.getPersonRepo().add(person, jamesAddr);
	}

}
