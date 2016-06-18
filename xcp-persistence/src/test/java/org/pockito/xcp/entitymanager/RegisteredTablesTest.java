package org.pockito.xcp.entitymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.pockito.dctm.test.RepositoryRequiredTest;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsQuery.OrderDirection;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.message.Message;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

public class RegisteredTablesTest extends RepositoryRequiredTest {

	@Rule
	public TestName testName = new TestName();

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

		final String expectedSegName = findAnyExtent();
		assertNotNull(expectedSegName);

		Extent extent = em.find(Extent.class, expectedSegName);

		assertNotNull(extent);
		assertEquals(expectedSegName, extent.getSegmentName());
	}

	@Test
	public void testQuery() throws DfException {

		final String expectedSegName = findAnyExtent();
		assertNotNull(expectedSegName);

		DmsBeanQuery<Extent> query = em.createBeanQuery(Extent.class).setOrder("segmentName", OrderDirection.asc)
				.setMaxResults(1);
		List<Extent> extents = query.getResultList();
		assertNotNull(extents);
		assertEquals(1, extents.size());
		assertEquals(expectedSegName, extents.get(0).getSegmentName());

	}

	@Test
	public void testUnsupportedOp() throws DfException {

		final String expectedSegName = findAnyExtent();
		assertNotNull(expectedSegName);

		Extent extent = em.find(Extent.class, expectedSegName);

		assertNotNull(extent);
		assertEquals(expectedSegName, extent.getSegmentName());

		try {
			em.persist(extent);
			fail("should be unsupported");
		} catch (XcpPersistenceException e) {
			assertEquals(Message.E_NOT_PERSISTENT_OBJECT.get("dm_extents"), e.getMessage());
		}

		try {
			em.remove(extent);
			fail("should be unsupported");
		} catch (XcpPersistenceException e) {
			assertEquals(Message.E_REMOVE_FAILED.get(expectedSegName), e.getMessage());
			Exception cause = (Exception) e.getCause();
			assertNotNull(cause);
			assertEquals(Message.E_NOT_PERSISTENT_OBJECT.get("dm_extents"), cause.getMessage());
		}
	}

	private String findAnyExtent() throws DfException {
		String segmentName = null;
		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
		try {
			IDfQuery query = createQuery();
			query.setDQL("select segment_name from dm_extents order by 1 enable (RETURN_TOP  1)");
			IDfCollection results = query.execute(session, IDfQuery.DF_READ_QUERY);
			try {
				if (results.next()) {
					segmentName = results.getString("segment_name");
				}
			} finally {
				results.close();
			}
		} finally {
			getRepository().releaseSession(session);
		}
		return segmentName;
	}

}
