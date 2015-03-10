package org.pockito.xcp.entitymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.pockito.xcp.repository.DmsQuery;
import org.pockito.xcp.repository.DmsTypedQuery;
import org.pockito.xcp.test.domain.Document;
import org.pockito.xcp.test.domain.WfEmailTemplate;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;

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

			// retrieve the document object
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

			// create some document objects
			final int count = 10;
			for (int i = 0; i < count; i++) {
				String expectedName = "_#_" + name.getMethodName();
				IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
				dmDocument.setString("subject", "test purpose #" + Integer.toString(i));
				dmDocument.setString("a_status", "draft");
				dmDocument.link("/Temp");
				dmDocument.save();
			}

			// query the first 5 docs
			DmsTypedQuery<Document> query = em
					.createNativeQuery(
							"select r_object_id from dm_document where folder(:path)"
									+ " and subject like :subject and a_status = :status", Document.class)
					.setParameter("path", "/Temp").setParameter("subject", "test purpose #%")
					.setParameter("status", "draft").setMaxResults(5);
			List<Document> docs = query.getResultList();
			assertNotNull(docs);
			assertEquals(5, docs.size());

			// query the last 5 docs
			docs = null;
			query.setParameter("path", "/Temp").setParameter("subject", "test purpose #%")
					.setParameter("status", "draft").setHint("RETURN_RANGE", "6 10 'object_name'");
			docs = query.getResultList();
			assertNotNull(docs);
			assertEquals(5, docs.size());

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testFindRelationObject() throws DfException {

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			// create the parent document object
			String expectedName = "_#_" + name.getMethodName();
			IDfSysObject dmWfDoc = createObject(session, "dm_document", expectedName);
			dmWfDoc.setString("subject", "test purpose -- parent");
			dmWfDoc.setString("a_status", "draft");
			dmWfDoc.save();
			String parentId = dmWfDoc.getObjectId().toString();

			final int count = 2;
			for (int i = 0; i < count; i++) {
				expectedName = "_#_" + name.getMethodName();
				IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
				dmDocument.setString("subject", "test purpose -- child #" + Integer.toString(i));
				dmDocument.setString("a_status", "draft");
				dmDocument.save();

				// link to the parent object
				dmDocument.addParentRelative("dm_wf_email_template", dmWfDoc.getObjectId(), null, true, "test purpose");
				dmDocument.save();
			}

			Document wf = em.find(Document.class, parentId);
			assertNotNull(wf);

			DmsTypedQuery<WfEmailTemplate> query = em.createNativeQuery(
					"select r_object_id from dm_relation where relation_name = 'dm_wf_email_template'"
							+ " and parent_id = :wfId", WfEmailTemplate.class).setParameter("wfId", wf.getId());
			List<WfEmailTemplate> emailTemplates = query.getResultList();
			assertNotNull(emailTemplates);
			assertEquals(2, emailTemplates.size());

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testRelate() throws DfException {

		IDfCollection childRelatives = null;

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			String expectedName = "_#_" + name.getMethodName();
			Document parent = new Document();
			parent.setName(expectedName);
			parent.setSubject("test purpose -- parent");
			parent.setStatus("draft");
			em.persist(parent);
			addToDeleteList(new DfId(parent.getId()));

			Document template = new Document();
			template.setName(expectedName);
			template.setSubject("test purpose -- child");
			template.setStatus("draft");
			em.persist(template);
			addToDeleteList(new DfId(template.getId()));

			WfEmailTemplate wfEmailTemplate = new WfEmailTemplate();
			wfEmailTemplate.setWf(parent);
			wfEmailTemplate.setTemplate(template);
			em.persist(wfEmailTemplate);

			// retrieve the relation object using DFC
			IDfPersistentObject dmsParent = session.getObject(new DfId(parent.getId()));
			childRelatives = dmsParent.getChildRelatives("dm_wf_email_template");
			assertNotNull(childRelatives);
			// at least one child
			assertTrue(childRelatives.next());
			final IDfId relId = childRelatives.getId("r_object_id");
			// the relation object should the one we created
			assertEquals(wfEmailTemplate.getId(), relId.toString());
			// the child id should refer the child object we created
			final IDfId childId = childRelatives.getId("child_id");
			assertEquals(template.getId(), childId.toString());
			// no more child
			assertTrue(childRelatives.next() == false);

		} finally {
			if (childRelatives != null) {
				try {
					childRelatives.close();
				} catch (Exception ignore) {
				}
			}
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testParentFolder() throws DfException {

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
			assertEquals(docId, document.getId());
			String expectedFolderPath = "/" + getRepository().getOperatorName(); // its
																					// home
																					// cabinet
			assertEquals(expectedFolderPath, document.getParentFolder());

			// move the object
			document.setParentFolder("/Temp");
			em.persist(document);

			IDfFolder tempFolder = session.getFolderByPath("/Temp");
			dmDocument.fetch(null);
			assertEquals(dmDocument.getId("i_folder_id").toString(), tempFolder.getObjectId().toString());
			assertEquals(1, dmDocument.getValueCount("i_folder_id"));

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testRemove() throws DfException {

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
			em.remove(document);

			IDfPersistentObject dmsObj = session.getObjectByQualification("dm_document where r_object_id = '" + docId
					+ "'");
			assertNull(dmsObj);

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testExecuteUpdate() throws DfException {

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			final String expectedName = "_#_" + name.getMethodName();
			final int count = 5;
			for (int i = 0; i < count; i++) {
				IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
				dmDocument.setString("subject", "test purpose -- object #" + Integer.toString(i));
				dmDocument.setString("a_status", "draft");
				dmDocument.link("/Temp");
				dmDocument.save();
			}

			DmsQuery updateQuery = em
					.createNativeQuery(
							"update dm_document objects set a_status = 'approved' "
							+ "where folder('/Temp') and object_name = '" + expectedName + "'");
			int updateCount = updateQuery.executeUpdate();
			assertEquals(count, updateCount);

			DmsQuery deleteQuery = em
					.createNativeQuery("delete dm_document objects where folder('/Temp') and object_name = '"
							+ expectedName + "'");
			int deleteCount = deleteQuery.executeUpdate();
			assertEquals(count, deleteCount);

		} finally {
			getRepository().releaseSession(session);
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
			buffer.append("update dm_document object").append(" set subject = 'a new subject'")
					.append(" where r_object_id = '").append(docId.toString()).append("'");
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
