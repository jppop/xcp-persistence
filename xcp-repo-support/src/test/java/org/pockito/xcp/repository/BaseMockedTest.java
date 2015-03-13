package org.pockito.xcp.repository;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Before;
import org.mockito.Mock;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsEntityManagerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.name.Names;

public class BaseMockedTest {

	@Mock
	protected DmsEntityManagerFactory emFactory;
	@Mock
	protected DmsEntityManager em;

	public BaseMockedTest() {
		super();
	}

	@Before
	public void initMock() {
		
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
	
	}

	public DmsEntityManager em() {
		return em;
	}

}