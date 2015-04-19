package org.pockito.xcp.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.repository.command.XcpRepoCmdFactory;
import org.pockito.xcp.repository.command.XcpRepoCommand;
import org.pockito.xcp.repository.guice.ModuleConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * WARNING: the tests in this class cannot be run together.
 * 
 * @author jfrancon
 *
 */
public class XcpRepositoryFactoryTest {

	@Ignore("demo purpose")
	@Test
	public void injectWithGuice() {

		ModuleConfig.install("repository.properties");
		
		// get a command with injected repository
		XcpRepoCommand cmd = XcpRepoCmdFactory.instance.create();
		assertNotNull(cmd);

		DmsEntityManager emActual = cmd.getEntityManager();
		assertNotNull(emActual);
		assertEquals(XcpEntityManager.class, emActual.getClass());

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
		assertNotEquals(cmd, specificCmd);
		emActual = specificCmd.getEntityManager();
		assertNotNull(emActual);
		assertEquals(XcpEntityManager.class, emActual.getClass());

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

	@Ignore("demo purpose")
	@Test
	public void injectWithSpring() {
		InputStream stream = XcpRepositoryFactoryTest.class.getResourceAsStream("/repository.properties");
		Properties appProperties = new Properties();
		try {
			appProperties.load(stream);
		} catch (IOException e) {
			fail("failed to load repository.properties");
		}

		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "/META-INF/xcp-repo/ModuleConfig.xml" })) {
//			XcpRepoCommand xcpRepo = context.getBean(XcpRepoCommand.class);
//			assertNotNull(xcpRepo);
//			assertEquals(appProperties.getProperty("org.pockito.xcp.repository.name"), xcpRepo.getRepository());
//			assertEquals(appProperties.getProperty("org.pockito.xcp.repository.username"), xcpRepo.getUsername());
//			assertEquals(appProperties.getProperty("org.pockito.xcp.repository.password"), xcpRepo.getPassword());
	//
			// get a command with injected repository
			XcpRepoCommand cmd = XcpRepoCmdFactory.instance.create();
			assertNotNull(cmd);

			DmsEntityManager emActual = cmd.getEntityManager();
			assertNotNull(emActual);
			assertEquals(XcpEntityManager.class, emActual.getClass());
			
			XcpRepoCommand specificCmd = XcpRepoCmdFactory.instance.create("a repo", "an user", "incorrect");
			assertNotEquals(cmd, specificCmd);
			emActual = specificCmd.getEntityManager();
			assertNotNull(emActual);
			assertEquals(XcpEntityManager.class, emActual.getClass());

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
	
	@Ignore("demo purpose")
	@Test(expected = NullPointerException.class)
	public void noInjection() {
		XcpRepoCmdFactory.instance.create();
	}
}
