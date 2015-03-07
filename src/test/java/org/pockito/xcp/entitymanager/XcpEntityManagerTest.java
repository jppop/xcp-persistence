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
import org.pockito.xcp.repository.DmsTypedQuery;
import org.pockito.xcp.test.domain.Document;
import org.pockito.xcp.test.domain.Person;
import org.pockito.xcp.test.domain.TaskPerson;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfAttr;

public class XcpEntityManagerTest extends RepositoryRequiredTest {

	@Rule
	public TestName name = new TestName();

	private static XcpEntityManager em;

	@BeforeClass
	public static void initResource() {
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(PropertyConstants.Repository, getRepository().getRepositoryName());
		props.put(PropertyConstants.Username, getRepository().getOperatorName());
		props.put(PropertyConstants.Password, getRepository().getOperatorPassword());
		XcpEntityManagerFactory dmsEmFactory = new XcpEntityManagerFactory(props);

		em = (XcpEntityManager) dmsEmFactory.createDmsEntityManager();
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

			DmsTypedQuery<Document> query = em
					.createNativeQuery(
							"select r_object_id from dm_document where folder(:path)"
									+ "and r_creation_date > :creationDate", Document.class)
					.setParameter("path", "/Templates").setParameter("creationDate", c.getTime()).setMaxResults(5)
					// .setHint("RETURN_RANGE", "11 20 'object_name'")
					.setHint("ROW_BASED", "");
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
	public void testFindRelationObject() {
		Person person = em.find(Person.class, "08c11cef800024a7");
		DmsTypedQuery<TaskPerson> query = em.createNativeQuery(
				"select r_object_id from todo_task_person where child_id = :personId", TaskPerson.class)
				.setParameter("personId", person.getId())
				;
		List<TaskPerson> tasksPerson = query.getResultList();
		for (TaskPerson taskPerson : tasksPerson) {
			System.out.println(taskPerson);
		}
	}

	@Test
	public void testCache() throws DfException {

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			// create a document object
			String expectedName = "_#_" + name.getMethodName();
			IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
			dmDocument.setString("subject", "test purpose");
			dmDocument.setString("a_status", "draft");
			dmDocument.save();
			String docId = dmDocument.getObjectId().toString();

			Document document1 = em.find(Document.class, docId);
			assertNotNull(document1);
			assertNotNull(em.sessionCache().get(docId));

			int vtamp = document1.getvStamp();

			Document document2 = em.find(Document.class, docId);
			assertNotNull(document2);
			assertEquals(document1, document2);
			assertEquals(vtamp, document2.getvStamp());

			// update the object from server (using DQL)
			StringBuffer buffer = new StringBuffer();
			buffer.append("update dm_document object")
				.append(" set subject = 'a new subject'")
				.append(" where r_object_id = '").append(docId.toString()).append("'")
				;
			IDfQuery queryExecutor = new DfQuery();
			queryExecutor.setDQL(buffer.toString());
			IDfCollection col = null;
			int count = -1;
			try {
				col = queryExecutor.execute(session, IDfQuery.DF_QUERY);
				if (col.next()) {
					IDfAttr attr = col.getAttr(0);
					count = col.getInt(attr.getName());
				}
			} finally {
				try {
					if (col != null) {
						col.close();
					}
				} catch (DfException ignore) {
				}
			}
			assertEquals(1, count);
			Document document3 = em.find(Document.class, docId);
			assertTrue(document2 != document3);
			assertEquals(vtamp + 1, document3.getvStamp());
			
		} finally {
			getRepository().releaseSession(session);
		}
	}

}
