package org.pockito.xcp.sample.todo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Collection;

import org.pockito.xcp.entitymanager.DctmDriverImpl;
import org.pockito.xcp.entitymanager.api.DctmDriver;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsException;
import org.pockito.xcp.sample.todo.domain.Address;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.olddomain.SerializedAddress;
import org.pockito.xcp.sample.todo.olddomain.SerializedPerson;
import org.pockito.xcp.sample.todo.repository.PersonRepo;
import org.pockito.xcp.sample.todo.repository.config.AppConfig;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class App {

	public static void main(String[] args) throws Exception {
		App app = new App();
		// create first an object (force DFC to initialize)
		System.out.println("My name is James, James Bond!");
		app.addJamesBond();
		if (args.length > 0) {
			try {
				final String filename = args[0];
				app.load(filename);
				deleteAll();
				app.rawLoad(filename);
				deleteAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void deleteAll() {

		PersonRepo repo = AppConfig.instance.getPersonRepo();

		System.out.println("Deleting person objects");
		DmsBeanQuery<?> query = repo.cmd().createBeanQuery(Person.class);
		query.setQueryType(DmsBeanQuery.QueryType.delete);
		int count = query.executeUpdate(true);
		System.out.printf("Deleted %d person(s)\n", count);

		query = repo.cmd().createBeanQuery(Address.class);
		System.out.println("Deleting address objects");
		query.setQueryType(DmsBeanQuery.QueryType.delete);
		count = query.executeUpdate(true);
		System.out.printf("Deleted %d address(es)\n", count);
	}

	public void addJamesBond() {

		PersonRepo repo = AppConfig.instance.getPersonRepo();

		Person person = repo.findByName("James Bond");
		if (person != null) {
			// kill 007 and its addresses, then resurrect him.
			repo.remove(person);
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

		repo.add(person, jamesAddr);
	}

	public void load(String filename) throws UnsupportedEncodingException, FileNotFoundException, IOException {

		PersonRepo repo = AppConfig.instance.getPersonRepo();

		try (Reader reader = new InputStreamReader(new FileInputStream(filename), "UTF-8")) {
			Gson gson = new GsonBuilder().create();
			Type collectionType = new TypeToken<Collection<SerializedPerson>>() {
			}.getType();
			Collection<SerializedPerson> persons = gson.fromJson(reader, collectionType);

			Stopwatch stopwatch = Stopwatch.createStarted();

			int count = 0;
			for (SerializedPerson serializedPerson : persons) {
				Person person = new Person();
				person.setFirstName(serializedPerson.getFirstname());
				person.setLastName(serializedPerson.getLastname());
				SerializedAddress tmpAddr = serializedPerson.getAddress();
				Address address = new Address();
				address.setStreetLine1(tmpAddr.getStreet());
				address.setZipcode(tmpAddr.getPostalcode());
				address.setCity(tmpAddr.getCity());
				address.setCountry(tmpAddr.getCountry());

				System.out.println("Creating " + serializedPerson.toString());
				repo.add(person, address);
				count++;
			}
			stopwatch.stop();
			System.out.printf("%d objects created in %s\n", count, stopwatch.toString());
		}
	}

	public void rawLoad(String filename) throws Exception {

		DctmDriver driver = new DctmDriverImpl();
		driver.getSessionManager(AppConfig.instance.repository(), AppConfig.instance.username(),
				AppConfig.instance.password());

		try (Reader reader = new InputStreamReader(new FileInputStream(filename), "UTF-8")) {
			Gson gson = new GsonBuilder().create();
			Type collectionType = new TypeToken<Collection<SerializedPerson>>() {
			}.getType();
			Collection<SerializedPerson> persons = gson.fromJson(reader, collectionType);

			Stopwatch stopwatch = Stopwatch.createStarted();

			int count = 0;
			for (SerializedPerson serializedPerson : persons) {

				System.out.println("Creating " + serializedPerson.toString());
				createPersonObject(driver, serializedPerson);

				count++;
			}
			stopwatch.stop();
			System.out.printf("%d objects created in %s\n", count, stopwatch.toString());
		}
	}

	private void createPersonObject(DctmDriver driver, SerializedPerson serializedPerson) throws Exception {
		IDfSession session = null;
		driver.getSessionManager().beginTransaction();
		try {
			session = driver.getSession();
			// create the person object
			IDfPersistentObject personObj = session.newObject("todo_person");
			personObj.setString("first_name", serializedPerson.getFirstname());
			personObj.setString("last_name", serializedPerson.getLastname());
			personObj.setString("object_name", serializedPerson.getFirstname() + " " + serializedPerson.getLastname());
			personObj.save();

			// create the address object
			SerializedAddress serializedAddress = serializedPerson.getAddress();
			IDfPersistentObject addrObj = session.newObject("todo_address");
			addrObj.setString("street_line_1", serializedAddress.getStreet());
			addrObj.setString("city", serializedAddress.getCity());
			addrObj.setString("zipcode", serializedAddress.getPostalcode());
			addrObj.setString("country", serializedAddress.getCountry());
			addrObj.save();

			// link the objects
			IDfRelation relationObj = personObj.addChildRelative("todo_person_address", addrObj.getObjectId(), null,
					false, null);
			relationObj.setString("address_type", "default");
			relationObj.save();

			// commit the transaction
			driver.getSessionManager().commitTransaction();
			
		} catch (DmsException | DfException e) {
			driver.getSessionManager().abortTransaction();
			throw e;
		} finally {
			if (session != null) {
				driver.releaseSession(session);
			}
		}
	}
}
