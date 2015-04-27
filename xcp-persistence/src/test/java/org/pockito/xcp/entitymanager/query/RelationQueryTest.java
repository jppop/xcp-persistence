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
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery.QueryType;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.test.domain.Document;
import org.pockito.xcp.test.domain.Person;
import org.pockito.xcp.test.domain.Task;
import org.pockito.xcp.test.domain.TaskPerson;
import org.pockito.xcp.test.domain.WfEmailTemplate;

@RunWith(MockitoJUnitRunner.class)
public class RelationQueryTest {

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
	public void testRelationQuery() {
		
		// assume we have found a person using em.find()
		Person person = new Person();
		person.setId("person id");

		// get all tasks for a given person
		DmsTypedQuery<Task> assistedQuery = em.createParentRelativesQuery(person, TaskPerson.class,
				Task.class, null);
		
		assertEquals("select p.r_object_id from todo_task_person r, todo_task p"
				+ " where r.relation_name = 'todo_task_person' and r.child_id = 'person id'"
				+ " and r.parent_id = p.r_object_id", assistedQuery.asDql());

	}

}
