package org.pockito.xcp.entitymanager.query;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.pockito.xcp.entitymanager.query.RightExpression.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.entitymanager.PropertyConstants;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.XcpEntityManagerFactory;
import org.pockito.xcp.entitymanager.api.DctmDriver;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.test.domain.Person;
import org.pockito.xcp.test.domain.Task;

@RunWith(MockitoJUnitRunner.class)
public class XcpBeanQueryTest {

	protected static DctmDriver dctmDriver;

	private static XcpEntityManager em;

	@BeforeClass
	public static void initResource() {

		dctmDriver = mock(DctmDriver.class);
		XcpEntityManagerFactory dmsEmFactory = new XcpEntityManagerFactory();

		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(PropertyConstants.DctmDriver, dctmDriver);
		props.put(PropertyConstants.Repository, "repo");
		props.put(PropertyConstants.Username, "user");
		props.put(PropertyConstants.Password, "incorrect");

		em = (XcpEntityManager) dmsEmFactory.createDmsEntityManager(props);
	}

	@Test
	public void testQueryPerson() {

		DmsBeanQuery<Person> queryPerson = em.createBeanQuery(Person.class);

		// find the Does (all person named "Doe")
		queryPerson.setParameter("lastName", eq("Doe"));

		assertEquals("select r_object_id from todo_person where last_name = 'Doe'", queryPerson.asDql());

		DmsBeanQuery<Task> queryTask = em.createBeanQuery(Task.class);

		Calendar c = new GregorianCalendar();
		c.set(1965, 2 - 1, 28, 9, 15, 25);
		queryTask.setParameter("priority", eq("medium"));
		queryTask.setParameter("creationDate", gt(c.getTime()));
		assertEquals(
				"select r_object_id from todo_task where priority = 'medium' "
				+ "and r_creation_date > DATE('1965/02/28 09:15:25', 'yyyy/mm/dd hh:mi:ss')",
				queryTask.asDql());

		DmsBeanQuery<Task> queryTask2 = em.createBeanQuery(Task.class);
		queryTask2.setParameter("priority", in("high", "urgent"));
		assertEquals(
				"select r_object_id from todo_task where priority in ( 'high', 'urgent' )",
				queryTask2.asDql());

	}

}
