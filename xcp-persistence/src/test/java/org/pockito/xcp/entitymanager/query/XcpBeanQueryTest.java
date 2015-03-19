package org.pockito.xcp.entitymanager.query;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pockito.xcp.entitymanager.PropertyConstants;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.XcpEntityManagerFactory;
import org.pockito.xcp.test.domain.Person;

public class XcpBeanQueryTest {

	private static XcpEntityManager em;

	@BeforeClass
	public static void initResource() {
		XcpEntityManagerFactory dmsEmFactory = new XcpEntityManagerFactory();

		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(PropertyConstants.Repository, "repo");
		props.put(PropertyConstants.Username, "user");
		props.put(PropertyConstants.Password, "incorrect");

		em = (XcpEntityManager) dmsEmFactory.createDmsEntityManager(props);
	}

	@Test
	public void test() {
		
		XcpBeanQuery<Person> query = (XcpBeanQuery<Person>) em.createBeanQuery(Person.class);
		
		// find the Does (all person named "Doe")
		query.bean().setLastName("Doe");
		
		assertEquals("select r_object_id from todo_person where last_name = 'Doe'", query.asDql());
		
//		List<Person> persons = query.getResultList();
	}

}
