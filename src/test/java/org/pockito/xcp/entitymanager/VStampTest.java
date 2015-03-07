package org.pockito.xcp.entitymanager;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.pockito.dctm.test.RepositoryRequiredTest;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;

public class VStampTest extends RepositoryRequiredTest {

	@Rule
	public TestName name = new TestName();

	@Test
	public void testVStamp() throws DfException {

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			// create a document object
			String expectedName = "_#_" + name.getMethodName();
			IDfSysObject dmDocument = createObject(session, "dm_document", expectedName);
			dmDocument.setString("subject", "test purpose");
			dmDocument.setString("a_status", "draft");
			dmDocument.save();
			IDfId docId = dmDocument.getObjectId();

			// vstamp should be initialized to 0
			assertEquals(1, dmDocument.getVStamp());
			// save the doc without any modification
			dmDocument.save();
			// should expected vstamp not incremented, but no. 
			assertEquals(2, dmDocument.getVStamp());
			
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
			// should expected vstamp always fetched from server, but no... 
			assertEquals(2, dmDocument.getVStamp());
			// ... explicitly fetch, instead
			dmDocument.fetch(null);
			assertEquals(3, dmDocument.getVStamp());
			
			
		} finally {
			getRepository().releaseSession(session);
		}
	}
}
