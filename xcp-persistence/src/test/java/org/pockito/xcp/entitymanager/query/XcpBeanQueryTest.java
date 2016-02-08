package org.pockito.xcp.entitymanager.query;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.pockito.xcp.entitymanager.query.RightExpression.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.entitymanager.NotYetImplemented;
import org.pockito.xcp.entitymanager.PropertyConstants;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.XcpEntityManagerFactory;
import org.pockito.xcp.entitymanager.api.DctmDriver;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery.QueryType;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.test.domain.Document;
import org.pockito.xcp.test.domain.Person;
import org.pockito.xcp.test.domain.Task;
import org.pockito.xcp.test.domain.TaskPerson;

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

		String queryPart = extractFromPart(queryPerson.asDql());
		assertEquals("from todo_person where last_name = 'Doe'", queryPart);

		DmsBeanQuery<Task> queryTask = em.createBeanQuery(Task.class);

		Calendar c = new GregorianCalendar();
		c.set(1965, 2 - 1, 28, 9, 15, 25);
		queryTask.setParameter("priority", eq("medium"));
		queryTask.setParameter("creationDate", gt(c.getTime()));
		queryPart = extractFromPart(queryTask.asDql());
		assertEquals("from todo_task where priority = 'medium' "
				+ "and r_creation_date > DATE('1965/02/28 09:15:25', 'yyyy/mm/dd hh:mi:ss')", queryPart);

		DmsBeanQuery<Task> queryTask2 = em.createBeanQuery(Task.class);
		queryTask2.setParameter("priority", in("high", "urgent"));
		queryPart = extractFromPart(queryTask2.asDql());
		assertEquals("from todo_task where priority in ( 'high', 'urgent' )", queryPart);

	}

	@Test
	public void repatingAttributeIsPrecededByAny() {
		
		DmsBeanQuery<Document> query = em.createBeanQuery(Document.class);
		query.setParameter("keywords", eq("a keyword"));
		assertEquals("from dm_document where any keywords = 'a keyword'",
				extractFromPart(query.asDql()));
	}
	
	@Test
	public void select() {
		
		DmsBeanQuery<Document> query = em.createBeanQuery(Document.class);
		query.setParameter("keywords", eq("a keyword"));
		assertEquals("from dm_document where any keywords = 'a keyword'",
				extractFromPart(query.asDql()));
	}
	
	private String extractFromPart(String query) {
		int index = query.indexOf("from");
		if ( index < 0 ) {
			return query;
		} else {
			String queryPart = query.substring(query.indexOf("from"));
			return queryPart;
		}
	}

	@Test
	public void testDeletePerson() {

		DmsBeanQuery<Person> queryPerson = em.createBeanQuery(Person.class);

		// Kill the Does (all person named "Doe")
		queryPerson.setQueryType(QueryType.delete);
		queryPerson.setParameter("lastName", eq("Doe"));

		assertEquals("delete todo_person objects where last_name = 'Doe'", queryPerson.asDql());

		DmsBeanQuery<Task> queryTask = em.createBeanQuery(Task.class);
		queryTask.setQueryType(QueryType.delete);

		assertEquals("delete todo_task objects", queryTask.asDql());
		try {
			queryTask.executeUpdate();
			fail("should raise an exception");
		} catch (XcpPersistenceException e) {
			assertEquals("[XDP0017] E_CONFIRM_DELETE_ALL - Please confirm you really want to delete all objects",
					e.getMessage());
		}
		try {
			queryTask.getResultList();
			fail("should raise an exception");
		} catch (XcpPersistenceException e) {
			assertEquals("[XDP0016] E_NOT_SELECT_QUERY - Only select query can retrieve a list of entities",
					e.getMessage());
		}

		queryTask.executeUpdate(true); // should not raise an exception

		DmsBeanQuery<Person> updateQuery = em.createBeanQuery(Person.class);
		updateQuery.setQueryType(QueryType.update);
		try {
			updateQuery.executeUpdate();
			fail("should raise an exception");
		} catch (NotYetImplemented e) {
		}
	}

	@Test
	public void testRelationBeanQuery() {

		// get all TaskPerson relation objects where a give person is involved
		// as a supervisor
		DmsBeanQuery<TaskPerson> query = em.createBeanQuery(TaskPerson.class);

		// assume we have found a person using em.find()
		Person supervisor = new Person();
		supervisor.setId("supervisor id");

		query.setParameter("role", eq("supervisor"));
		query.setParameter("person", eq(supervisor.getId()));

		assertEquals("from todo_task_person" + " where role = 'supervisor' and child_id = 'supervisor id'",
				extractFromPart(query.asDql()));

	}

	@Test
	public void testQueryPersonWithIsAndLikeOperator() {

		DmsBeanQuery<Person> queryPerson = em.createBeanQuery(Person.class);

		// find the Does (all person named "Doe")
		queryPerson.setParameter("lastName", eq("Doe"));

		// find the person with null fisrtName
		queryPerson.setParameter("name", is("null"));

		// find the person with fisrtName starting by Jo
		queryPerson.setParameter("firstName", like("Jo%"));

		assertEquals(
				"from todo_person where last_name = 'Doe' and object_name is null and first_name like 'Jo%'",
				extractFromPart(queryPerson.asDql()));

		DmsBeanQuery<Task> queryTask = em.createBeanQuery(Task.class);

		Calendar c = new GregorianCalendar();
		c.set(1965, 2 - 1, 28, 9, 15, 25);
		queryTask.setParameter("priority", eq("medium"));
		queryTask.setParameter("creationDate", gt(c.getTime()));
		assertEquals("from todo_task where priority = 'medium' "
				+ "and r_creation_date > DATE('1965/02/28 09:15:25', 'yyyy/mm/dd hh:mi:ss')", extractFromPart(queryTask.asDql()));

		DmsBeanQuery<Task> queryTask2 = em.createBeanQuery(Task.class);
		queryTask2.setParameter("priority", in("high", "urgent"));
		assertEquals("from todo_task where priority in ( 'high', 'urgent' )", extractFromPart(queryTask2.asDql()));

	}

}
