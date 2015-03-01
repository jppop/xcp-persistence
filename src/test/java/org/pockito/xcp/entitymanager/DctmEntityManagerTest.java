package org.pockito.xcp.entitymanager;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.pockito.dctm.test.RepositoryRequiredTest;
import org.pockito.xcp.repository.DmsEntityManager;
import org.pockito.xcp.test.domain.Document;
import org.pockito.xcp.test.domain.Task;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

public class DctmEntityManagerTest extends RepositoryRequiredTest {

	@Rule public TestName name = new TestName();
	
	@Test
	public void testFind() throws DfException {

		IDfSession session = getRepository().getManagedSessionForOperator(getRepository().getRepositoryName());
		try {

			// create a task object
			IDfSysObject dmDocument = createObject(session, "dm_document", "_#_" + name.getMethodName());
			dmDocument.setString("subject", "test prupose");
			dmDocument.save();
			String docId = dmDocument.getObjectId().toString();
			
			HashMap<String, Object> props = new HashMap<String, Object>();
			props.put(PropertyConstants.Repository, getRepository().getRepositoryName());
			props.put(PropertyConstants.Username, getRepository().getOperatorName());
			props.put(PropertyConstants.Password, getRepository().getOperatorPassword());
			DctmEntityManagerFactory dmsEmFactory = new DctmEntityManagerFactory(props);
			
			DmsEntityManager em = dmsEmFactory.createDmsEntityManager();
			
			Document document = em.find(Document.class, docId);
			assertNotNull(document);

		} finally {
			getRepository().releaseSession(session);
		}
	}

}
