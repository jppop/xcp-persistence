package org.pockito.xcp.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;

@RunWith(MockitoJUnitRunner.class)
public class XcpRepositoryFactoryTest extends BaseMockedTest {

	@Test
	public void testInjections() {
		XcpRepository repo = XcpRepositoryFactory.getInstance().create();
		assertNotNull(repo);
		assertEquals(repo.getClass(), XcpRepositoryImpl.class);

		DmsEntityManager emActual = repo.getEntityManager();
		assertEquals(em, emActual);

		InputStream stream = XcpRepositoryFactoryTest.class.getResourceAsStream("/repository.properties");
		Properties appProperties = new Properties();
		try {
			appProperties.load(stream);
		} catch (IOException e) {
			fail("failed to load repository.properties");
		}

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.name"),
				((XcpRepositoryImpl) repo).getRepository());

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.username"),
				((XcpRepositoryImpl) repo).getUsername());

		assertEquals(appProperties.getProperty("org.pockito.xcp.repository.password"),
				((XcpRepositoryImpl) repo).getPassword());

	}

}
