package org.pockito.xcp.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsEntityManagerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.name.Names;

@RunWith(MockitoJUnitRunner.class)
public class XcpRepositoryFactoryTest {

	@Mock
	DmsEntityManagerFactory emFactory;
	@Mock
	DmsEntityManager em;

	@Test
	public void testInjections() {
		Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				loadProperties(binder());
				requestStaticInjection(XcpRepositoryFactory.class);
				bind(DmsEntityManagerFactory.class).toInstance(emFactory);
				bind(XcpRepository.class).to(XcpRepositoryImpl.class);
				when(emFactory.createDmsEntityManager()).thenReturn(em);
			}

			private void loadProperties(Binder binder) {
				InputStream stream = XcpRepositoryFactoryTest.class.getResourceAsStream("/repository.properties");
				Properties appProperties = new Properties();
				try {
					appProperties.load(stream);
					Names.bindProperties(binder, appProperties);
				} catch (IOException e) {
					binder.addError(e);
				}
			}
		});

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
				((XcpRepositoryImpl) repo).getPasword());

	}

}
