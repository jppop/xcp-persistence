package org.pockito.xcp.config.domain;


import static org.junit.Assert.assertEquals;
import static org.pockito.xcp.entitymanager.query.RightExpression.eq;

import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.pockito.dctm.test.RepositoryRequiredTest;
import org.pockito.xcp.entitymanager.PropertyConstants;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.XcpEntityManagerFactory;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsQuery.OrderDirection;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

public class XcpParameterTest extends RepositoryRequiredTest {

	private static final String XCPAPP_NAMESPACE = "ns!";

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
	public void testFindPrm() throws DfException {

		IDfSession session = getRepository().getSessionForOperator(getRepository().getRepositoryName());
		try {

			IDfSysObject dmAppPrm;
			
			// create a document object
			String expectedName = "_#_" + name.getMethodName();
			dmAppPrm = createObject(session, "dmc_xcp_app_config", expectedName);
			dmAppPrm.setString("subject", "test purpose");
			dmAppPrm.setString("namespace", XCPAPP_NAMESPACE);
			dmAppPrm.setString("config_name", "c-name-#01");
			dmAppPrm.setString("config_type", "c-type-#01");
			dmAppPrm.setRepeatingString("property_name", 0, "p-name-one");
			dmAppPrm.setRepeatingString("property_value", 0, "p-value-one");
			dmAppPrm.save();

			dmAppPrm = createObject(session, "dmc_xcp_app_config", expectedName);
			dmAppPrm.setString("subject", "test purpose");
			dmAppPrm.setString("namespace", XCPAPP_NAMESPACE);
			dmAppPrm.setString("config_name", "c-name-#02");
			dmAppPrm.setString("config_type", "c-type-#02");
			dmAppPrm.setRepeatingString("property_name", 0, "p-name-one");
			dmAppPrm.setRepeatingString("property_value", 0, "p-value-one");
			dmAppPrm.save();

		} finally {
			getRepository().releaseSession(session);
		}

		DmsBeanQuery<XcpParameter> queryPrm = em.createBeanQuery(XcpParameter.class);

		queryPrm.setParameter("namespace", eq(XCPAPP_NAMESPACE));
		queryPrm.setOrder("namespace", OrderDirection.asc).setOrder("configName", OrderDirection.asc);
		
		List<XcpParameter> prmList = queryPrm.getResultList();
		assertEquals(2, prmList.size());
		
		XcpParameter prm; String[] names; String[] values;
		prm = prmList.get(0);
		assertEquals(XCPAPP_NAMESPACE, prm.getNamespace());
		assertEquals("c-name-#01", prm.getConfigName());
		assertEquals("c-type-#01", prm.getConfigType());
		assertEquals(1,  prm.getPropertyName().size());
		names = new String[prm.getPropertyName().size()];
		prm.getPropertyName().toArray(names);
		assertEquals("p-name-one", names[0]);
		values = new String[prm.getPropertyValue().size()];
		prm.getPropertyValue().toArray(values);
		assertEquals("p-value-one", values[0]);
		
		prm = prmList.get(1);
		assertEquals(XCPAPP_NAMESPACE, prm.getNamespace());
		assertEquals("c-name-#02", prm.getConfigName());
		assertEquals("c-type-#02", prm.getConfigType());
		assertEquals(1,  prm.getPropertyName().size());
		names = new String[prm.getPropertyName().size()];
		prm.getPropertyName().toArray(names);
		assertEquals("p-name-one", names[0]);
		values = new String[prm.getPropertyValue().size()];
		prm.getPropertyValue().toArray(values);
		assertEquals("p-value-one", values[0]);
		
	}

}
