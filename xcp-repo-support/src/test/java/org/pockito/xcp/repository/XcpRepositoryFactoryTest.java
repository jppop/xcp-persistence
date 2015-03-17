package org.pockito.xcp.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.repository.command.XcpRepoCmdFactory;
import org.pockito.xcp.repository.command.XcpRepoCmdImpl;
import org.pockito.xcp.repository.command.XcpRepoCommand;

@RunWith(MockitoJUnitRunner.class)
public class XcpRepositoryFactoryTest extends BaseMockedTest {

	@BeforeClass
	public static void init() {
		XcpGenericRepoImplTest.mockXcpCmd = false;
	}
	
	@Test
	public void testInjections() {
		XcpRepoCommand cmd = XcpRepoCmdFactory.getInstance().create();
		assertNotNull(cmd);

//		cmd.withinTransaction().rollback(); // force the entity manager creation
		DmsEntityManager emActual = cmd.getEntityManager();
		assertEquals(em, emActual);

		InputStream stream = XcpRepositoryFactoryTest.class.getResourceAsStream("/repository.properties");
		Properties appProperties = new Properties();
		try {
			appProperties.load(stream);
		} catch (IOException e) {
			fail("failed to load repository.properties");
		}

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.name"),
				((XcpRepoCmdImpl) cmd).getRepository());

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.username"),
				((XcpRepoCmdImpl) cmd).getUsername());

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.password"),
				((XcpRepoCmdImpl) cmd).getPassword());

	}

}
