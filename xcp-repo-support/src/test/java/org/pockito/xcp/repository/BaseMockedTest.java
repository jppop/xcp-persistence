package org.pockito.xcp.repository;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.entitymanager.api.DmsEntityManagerFactory;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.entitymanager.api.Transaction;
import org.pockito.xcp.repository.command.XcpRepoCmdFactory;
import org.pockito.xcp.repository.command.XcpRepoCmdImpl;
import org.pockito.xcp.repository.command.XcpRepoCommand;
import org.pockito.xcp.repository.test.domain.WfEmailTemplate;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.name.Names;

public class BaseMockedTest {

	protected static boolean mockXcpCmd = false;
	
	@Mock
	protected DmsEntityManagerFactory emFactory;
	@Mock
	protected XcpEntityManager em;
	@Mock
	protected Transaction txMock;

	@Mock
	XcpRepoCommand mockCmd;

	@Mock
	MetaData relationMetaDataMock;
	@Mock
	PersistentProperty parentProp;
	@Mock
	PersistentProperty childProp;

	public BaseMockedTest() {
		super();
	}

//	public static class XcpCommandProvider implements Provider<XcpRepoCommand> {
//
//		@Override
//		public XcpRepoCommand get() {
//			XcpRepoCommand mockCmd = mock(XcpRepoCommand.class);
//			when(mockCmd.withinTransaction()).thenReturn(mockCmd);
//			when(mockCmd.create(any())).thenReturn(mockCmd);
//			when(mockCmd.update(any())).thenReturn(mockCmd);
//			when(mockCmd.remove(any())).thenReturn(mockCmd);
//			when(mockCmd.link(any())).thenReturn(mockCmd);
//			when(mockCmd.to(any())).thenReturn(mockCmd);
//			when(mockCmd.with(any())).thenReturn(mockCmd);
//			return mockCmd;
//		}
//		
//	}
//	
	@Before
	public void initMock() {
		
		Guice.createInjector(new AbstractModule() {
	
			@Override
			protected void configure() {
				loadProperties(binder());
//				requestStaticInjection(XcpRepoCmdFactory.class);
				bind(DmsEntityManagerFactory.class).toInstance(emFactory);
				if (mockXcpCmd) {
					bind(XcpRepoCommand.class).annotatedWith(Names.named("XcpRepoCommand")).toInstance(mockCmd);
//					bind(XcpRepoCommand.class).toProvider(XcpCommandProvider.class);
				} else {
					bind(XcpRepoCommand.class).annotatedWith(Names.named("XcpRepoCommand")).to(XcpRepoCmdImpl.class);
				}
				requestInjection(XcpRepoCmdFactory.instance);

				when(emFactory.createDmsEntityManager(Matchers.<Map<String, Object>>any())).thenReturn(em);
				when(em.getTransaction()).thenReturn(txMock);

				// some stubs

				// stub cmd
				when(mockCmd.withinTransaction()).thenReturn(mockCmd);
				when(mockCmd.create(any())).thenReturn(mockCmd);
				when(mockCmd.update(any())).thenReturn(mockCmd);
				when(mockCmd.remove(any())).thenReturn(mockCmd);
				when(mockCmd.link(any())).thenReturn(mockCmd);
				when(mockCmd.to(any())).thenReturn(mockCmd);
				when(mockCmd.with(any())).thenReturn(mockCmd);
				when(mockCmd.getEntityManager()).thenReturn(em);
				
				// relation stubs
				when(em.getMetaData(WfEmailTemplate.class)).thenReturn(relationMetaDataMock);
				when(relationMetaDataMock.getTypeCategory()).thenReturn(XcpTypeCategory.RELATION);
				when(relationMetaDataMock.getParentMethod()).thenReturn(parentProp);
				when(relationMetaDataMock.getChildMethod()).thenReturn(childProp);
				
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