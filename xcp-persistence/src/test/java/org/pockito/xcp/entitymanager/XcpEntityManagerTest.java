package org.pockito.xcp.entitymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.pockito.dctm.test.RepositoryRequiredTest;
import org.pockito.xcp.entitymanager.api.DmsQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.Transaction;
import org.pockito.xcp.test.domain.Document;
import org.pockito.xcp.test.domain.WfEmailTemplate;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class XcpEntityManagerTest extends RepositoryRequiredTest {

	@Rule
	public TestName name = new TestName();

	private static XcpEntityManager em;

	@BeforeClass
	public static void initResource() {
		XcpEntityManagerFactory dmsEmFactory = new XcpEntityManagerFactory();

		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(PropertyConstants.Repository, getRepository().getRepositoryName());
		props.put(PropertyConstants.Username, getRepository().getOperatorName());
		props.put(PropertyConstants.Password, getRepository().getOperatorPassword());

		em = (XcpEntityManager) dmsEmFactory.createDmsEntityManager(props);
	}

	@Test
	public void testFind() throws DfException {

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
		try {

			String expectedName = "_#_" + name.getMethodName();
			Document document = new Document();
			document.setName(expectedName);
			document.setStatus("draft");
			Calendar c = new GregorianCalendar();
			c.set(1965, 2, 28);
			document.setCreationDate(c.getTime());

			em.persist(document);

			assertNotNull(document.getId());
			addToDeleteList(new DfId(document.getId()));

			Calendar today = new GregorianCalendar();

			// retrieve the document object
			IDfSysObject dmDocument = (IDfSysObject) session.getObject(new DfId(document.getId()));
			assertEquals(expectedName, dmDocument.getObjectName());
			assertEquals("draft", dmDocument.getStatus());
			// should be overridden by the repository
			assertTrue(dmDocument.getCreationDate().getYear() == today.get(Calendar.YEAR));

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testCreateWithinTransaction() throws DfException {

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
		try {

			String expectedName = "_#_" + name.getMethodName();
			Document document = new Document();
			document.setName(expectedName);
			document.setStatus("draft");

			Transaction tx = em.getTransaction();
			tx.begin();
			em.persist(document);

			assertNotNull(document.getId());

			// should not be persisted yet
			IDfPersistentObject dmsObj = session.getObjectByQualification("dm_sysobject where r_object_id = '"
					+ document.getId() + "'");
			assertNull(dmsObj);

			// commit now
			tx.commit();
			addToDeleteList(new DfId(document.getId()));

			// retrieve the document object
			dmsObj = session.getObjectByQualification("dm_sysobject where r_object_id = '" + document.getId() + "'");
			assertNotNull(dmsObj);

			// test rollback
			document = new Document();
			document.setName(expectedName);
			document.setStatus("draft");

			tx.begin();
			em.persist(document);
			assertNotNull(document.getId());
			tx.rollback();

			// should not be persisted yet
			dmsObj = session.getObjectByQualification("dm_sysobject where r_object_id = '" + document.getId() + "'");
			assertNull(dmsObj);

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testUpdate() throws DfException {

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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

			document.setStatus("approved");
			em.persist(document);

			assertEquals("approved", document.getStatus());

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testAddContent() throws DfException, IOException {

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
		try {

			// create a document object
			final String expectedName = "_#_" + name.getMethodName();
			final IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
			dmDocument.setString("subject", "test purpose");
			dmDocument.setString("a_status", "draft");
			dmDocument.save();
			String docId = dmDocument.getObjectId().toString();

			final Document document = em.find(Document.class, docId);
			assertNotNull(document);

			File tempFile = File.createTempFile(expectedName, ".expected");
			tempFile.deleteOnExit();
			PrintWriter writer = new PrintWriter(tempFile, "UTF-8");
			writer.print("sample");
			writer.close();

			em.addAttachment(document, tempFile.getAbsolutePath(), "text");

			assertTrue(document.getContentSize() > 0);
			assertEquals("text", document.getContentType());

			final File actualFile = File.createTempFile(expectedName, ".actual");
			actualFile.deleteOnExit();

			dmDocument.fetch(null);
			dmDocument.getFile(actualFile.getAbsolutePath());
			String actualContent = Files.toString(actualFile, Charsets.UTF_8);
			assertEquals("sample", actualContent);

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testGetContent() throws DfException, IOException {

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
		try {

			// create a document object
			final String expectedName = "_#_" + name.getMethodName();
			final IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
			dmDocument.setString("subject", "test purpose");
			dmDocument.setString("a_status", "draft");
			dmDocument.setContentType("text");
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write("sample".getBytes("UTF-8"));
			dmDocument.setContent(stream);
			dmDocument.save();
			String docId = dmDocument.getObjectId().toString();

			final Document document = em.find(Document.class, docId);
			assertNotNull(document);
			assertTrue(document.getContentSize() > 0);
			assertEquals("text", document.getContentType());

			File tempFile = File.createTempFile(expectedName, ".expected");
			tempFile.deleteOnExit();

			final String actualFilename = em.getAttachment(document, tempFile.getAbsolutePath());
			assertEquals(tempFile.getAbsolutePath(), actualFilename);

			String actualContent = Files.toString(tempFile, Charsets.UTF_8);
			assertEquals("sample", actualContent);

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testFindList() throws DfException {

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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
			List<WfEmailTemplate> wfEemailTemplates = query.getResultList();
			assertNotNull(wfEemailTemplates);
			assertEquals(2, wfEemailTemplates.size());

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testFindChildRelatives() throws DfException {

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
		try {

			// create the parent document object
			String expectedName = "_#_" + name.getMethodName();
			IDfSysObject dmWfDoc = createObject(session, "dm_document", expectedName);
			dmWfDoc.setString("subject", "test purpose -- parent");
			dmWfDoc.setString("a_status", "draft");
			dmWfDoc.save();
			String parentId = dmWfDoc.getObjectId().toString();

			final int childCount = 2;
			String childs[] = new String[childCount];
			for (int i = 0; i < childCount; i++) {
				expectedName = "_#_" + name.getMethodName();
				IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
				dmDocument.setString("subject", "test purpose -- child #" + Integer.toString(i));
				dmDocument.setString("a_status", "draft");
				dmDocument.save();
				childs[i] = dmDocument.getObjectId().getId();

				// link to the parent object
				dmDocument.addParentRelative("dm_wf_email_template", dmWfDoc.getObjectId(), null, true, "test purpose");
				dmDocument.save();
			}

			Document wf = em.find(Document.class, parentId);
			assertNotNull(wf);

			// find child relatives using a custom query
			DmsTypedQuery<Document> query = em.createNativeQuery(
					"select t.r_object_id from dm_relation r, dm_document t"
							+ " where r.relation_name = 'dm_wf_email_template' and r.parent_id = :wfId"
							+ " and r.child_id = t.r_object_id order by 1", Document.class)
					.setParameter("wfId", wf.getId());

			List<Document> emailTemplates = query.getResultList();

			assertNotNull(emailTemplates);
			assertEquals(2, emailTemplates.size());
			int i = 0;
			for (Document template : emailTemplates) {
				assertEquals(childs[i], template.getId());
				i++;
			}

			// find child relatives using an assisted query
			DmsTypedQuery<Document> assistedQuery = em.createChildRelativesQuery(wf, WfEmailTemplate.class,
					Document.class, null);

			emailTemplates = assistedQuery.getResultList();

			assertNotNull(emailTemplates);
			assertEquals(2, emailTemplates.size());
			i = 0;
			for (Document template : emailTemplates) {
				assertEquals(childs[i], template.getId());
				i++;
			}

		} finally {
			getRepository().releaseSession(session);
		}
	}

	@Test
	public void testRelate() throws DfException {

		IDfCollection childRelatives = null;

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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
			wfEmailTemplate.setOrder(2);
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
			
			// check extra relation attributes
			final IDfRelation relDmsObj = (IDfRelation) session.getObject(relId);
			assertEquals(2, relDmsObj.getInt("order_no"));

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

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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

			DmsQuery updateQuery = em.createNativeQuery("update dm_document objects set a_status = 'approved' "
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

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
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
