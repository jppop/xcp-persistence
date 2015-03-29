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
import org.pockito.xcp.repository.command.XcpRepoCommand;

@RunWith(MockitoJUnitRunner.class)
public class XcpRepositoryFactoryTest extends BaseMockedTest {

	@BeforeClass
	public static void init() {
		XcpGenericRepoImplTest.mockXcpCmd = false;
	}

	@Test
	public void testInjections() {

		// get a command with injected repository
		XcpRepoCommand cmd = XcpRepoCmdFactory.instance.create();
		assertNotNull(cmd);

		DmsEntityManager emActual = cmd.getEntityManager();
		assertEquals(em, emActual);

		InputStream stream = XcpRepositoryFactoryTest.class.getResourceAsStream("/repository.properties");
		Properties appProperties = new Properties();
		try {
			appProperties.load(stream);
		} catch (IOException e) {
			fail("failed to load repository.properties");
		}

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.name"), cmd.getRepository());

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.username"), cmd.getUsername());

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.password"), cmd.getPassword());

		XcpRepoCommand specificCmd = XcpRepoCmdFactory.instance.create("a repo", "an user", "incorrect");
		emActual = cmd.getEntityManager();
		assertEquals(em, emActual);

		assertEquals("a repo", specificCmd.getRepository());

		assertEquals("an user", specificCmd.getUsername());

		assertEquals("incorrect", specificCmd.getPassword());

		// get again a command with injected repository
		XcpRepoCommand cmdAgain = XcpRepoCmdFactory.instance.create();
		assertNotNull(cmdAgain);

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.name"), cmdAgain.getRepository());

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.username"), cmdAgain.getUsername());

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.password"), cmdAgain.getPassword());
	}

}
