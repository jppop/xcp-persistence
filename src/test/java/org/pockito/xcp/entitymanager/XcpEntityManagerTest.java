package org.pockito.xcp.entitymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.pockito.dctm.test.RepositoryRequiredTest;
import org.pockito.xcp.repository.DmsEntityManager;
import org.pockito.xcp.repository.DmsJoinTypedQuery;
import org.pockito.xcp.repository.DmsTypedQuery;
import org.pockito.xcp.repository.JoinType;
import org.pockito.xcp.test.domain.Document;
import org.pockito.xcp.test.domain.Person;
import org.pockito.xcp.test.domain.TaskPerson;
import org.pockito.xcp.test.domain.Task;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;

public class XcpEntityManagerTest extends RepositoryRequiredTest {

	@Rule
	public TestName name = new TestName();

	private static DmsEntityManager em;

	@BeforeClass
	public static void initResource() {
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(PropertyConstants.Repository, getRepository().getRepositoryName());
		props.put(PropertyConstants.Username, getRepository().getOperatorName());
		props.put(PropertyConstants.Password, getRepository().getOperatorPassword());
		XcpEntityManagerFactory dmsEmFactory = new XcpEntityManagerFactory(props);

		em = dmsEmFactory.createDmsEntityManager();
	}

	@Test
	public void testFind() throws DfException {

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			// create a document object
			String expectedName = "_#_" + name.getMethodName();
			IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
			dmDocument.setString("subject", "test purpose");
			dmDocument.setString("a_status", "draft");
			dmDocument.save();
			String docId = dmDocument.getObjectId().toString();

			Document document = em.find(Document.class, docId);
			assertNotNull(document);
			assertEquals(expectedName, document.getName());
			assertEquals("draft", document.getStatus());

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testCreate() throws DfException {

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			String expectedName = "_#_" + name.getMethodName();
			Document document = new Document();
			document.setName(expectedName);
			document.setStatus("draft");
			Calendar c = new GregorianCalendar();
			c.set(1965, 2, 28);
			document.setCreationDate(c.getTime());

			em.persist(document);
			
			Calendar today = new GregorianCalendar();

			// create a task object
			IDfSysObject dmDocument = (IDfSysObject) session.getObject(new DfId(document.getId()));
			addToDeleteList(dmDocument.getObjectId());
			assertEquals(expectedName, dmDocument.getObjectName());
			assertEquals("draft", dmDocument.getStatus());
			// should be overridden by the repository
			assertTrue(dmDocument.getCreationDate().getYear() == today.get(Calendar.YEAR));

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testFindList() throws DfException {

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			Calendar c = new GregorianCalendar();
			c.set(1965, 2, 28);

			DmsTypedQuery<Document> query
				= em.createNativeQuery("select r_object_id from dm_document where folder(:path)"
						+ "and r_creation_date > :creationDate", Document.class)
				 .setParameter("path", "/Templates")
				 .setParameter("creationDate", c.getTime())
				 .setMaxResults(5)
//				 .setHint("RETURN_RANGE", "11 20 'object_name'")
				 .setHint("ROW_BASED", "")
				 ;
			List<Document> docs = query.getResultList();
			if (docs != null) {
				for (Document document : docs) {
					System.out.println(document.toString());
				}
			}

		} finally {
			getRepository().releaseSession(session);
		}
	}
	
	@Test
	public void testQueryJoin() {
		
		// performers of specific task
//		select c.r_object_id, c.object_name, c.first_name, c.last_name, r.parent_id 
//		  from todo_person c, todo_task_person r
//		 where r.child_id = c.r_object_id
//		   and r.parent_id = '08c11cef80002f24'
//		;
		Task task = em.find(Task.class, "08c11cef80002f24");
		DmsJoinTypedQuery<TaskPerson, Person> q1 = em.createJoinQuery(TaskPerson.class, Person.class);
		q1.select().join(TaskPerson.class).on(JoinType.childId)
		 .setParameter(JoinType.parentId, task.getId())
		 ;
		List<Person> persons = q1.getRelatedResultList();
		for (Person person : persons) {
			System.out.println(person);
		}

			// tasks assigned to a performer
//			select p.r_object_id, p.object_name, p.priority, r.parent_id 
//			  from todo_task p, todo_task_person r
//			 where r.parent_id = p.r_object_id
//			   and r.child_id = '08c11cef800024a7'
//			   and p.priority = 'urgent'
//			;
		Person person = em.find(Person.class, "08c11cef800024a7");
		DmsJoinTypedQuery<TaskPerson, Task> q2 = em.createJoinQuery(TaskPerson.class, Task.class);
		q2.select().join(TaskPerson.class).on(JoinType.parentId)
		 .where("b.priority = :priority")
		 .setParameter(JoinType.childId, person.getId())
		 .setParameter("priority", "urgent")
		 ;
		List<Task> tasks = q2.getRelatedResultList();
		for (Task task2 : tasks) {
			System.out.println(task2);
		}
	}
}
