package org.pockito.xcp.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.config.domain.XcpParameter;
import org.pockito.xcp.config.domain.XcpParameterRepo;
import org.pockito.xcp.repository.command.XcpRepoCommand;

@RunWith(MockitoJUnitRunner.class)
public class XcpConfigToolTest {

	@Mock
	XcpParameterRepo repo;
	
	@Mock
	XcpRepoCommand xcpCmd;
	
	@Before
	public void setup() {
		when(repo.createSharedCmd(anyString(), anyString(), anyString())).thenReturn(xcpCmd);
	}
	
	@Test
	public void testExportIsOk() throws JAXBException {
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<parameters>\n"
				+ "    <parameter>\n"
				+ "        <namespace>nm1</namespace>\n"
				+ "        <config-name>c-name-#01</config-name>\n"
				+ "        <config-type>c-type-#01</config-type>\n"
				+ "        <properties>\n"
				+ "            <property name=\"p-name-#01.01\">\n"
				+ "                <value>p-value-#01.01</value>\n"
				+ "            </property>\n"
				+ "            <property name=\"p-name-#01.02\">\n"
				+ "                <value>p-value-#01.02</value>\n"
				+ "            </property>\n"
				+ "        </properties>\n"
				+ "    </parameter>\n"
				+ "    <parameter>\n"
				+ "        <namespace>nm2</namespace>\n"
				+ "        <config-name>c-name-#02</config-name>\n"
				+ "        <config-type>c-type-#02</config-type>\n"
				+ "        <properties>\n"
				+ "            <property name=\"p-name-#02.01\">\n"
				+ "                <value>p-value-#02.01</value>\n"
				+ "            </property>\n"
				+ "        </properties>\n"
				+ "    </parameter>\n"
				+ "</parameters>\n"
				;
		
		List<XcpParameter> prms = new ArrayList<XcpParameter>();
		
		XcpParameter prm;
		prm = new XcpParameter();
		prm.setNamespace("nm1");
		prm.setConfigName("c-name-#01");
		prm.setConfigType("c-type-#01");
		prm.setPropertyName(Arrays.asList("p-name-#01.01", "p-name-#01.02"));
		prm.setPropertyValue(Arrays.asList("p-value-#01.01", "p-value-#01.02"));
		prms.add(prm);
		prm = new XcpParameter();
		prm.setNamespace("nm2");
		prm.setConfigName("c-name-#02");
		prm.setConfigType("c-type-#02");
		prm.setPropertyName(Arrays.asList("p-name-#02.01"));
		prm.setPropertyValue(Arrays.asList("p-value-#02.01"));
		prms.add(prm);
		
		when(repo.findByNamespaces(aryEq(new String[] { "nm1", "nm2"}))).thenReturn(prms);
		
		XcpConfigTool configTool = new XcpConfigTool(repo);
		
		OutputStream output = new OutputStream()
	    {
	        private StringBuilder string = new StringBuilder();
	        @Override
	        public void write(int b) throws IOException {
	            this.string.append((char) b );
	        }

	        public String toString(){
	            return this.string.toString();
	        }
	    };
	    
	    configTool.exportConfig("repo", "username", "password", new String[] { "nm1", "nm2"}, output);
	    
	    assertEquals(expected, output.toString());
	}

	@Test
	public void testNoPrms() throws JAXBException {
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<parameters/>\n"
				;
		
		List<XcpParameter> prms = new ArrayList<XcpParameter>();
		
		when(repo.findByNamespaces(Mockito.any(String[].class))).thenReturn(prms);
		
		XcpConfigTool configTool = new XcpConfigTool(repo);
		
		OutputStream output = new OutputStream()
	    {
	        private StringBuilder string = new StringBuilder();
	        @Override
	        public void write(int b) throws IOException {
	            this.string.append((char) b );
	        }

	        public String toString(){
	            return this.string.toString();
	        }
	    };
	    
	    configTool.exportConfig("repo", "username", "password", new String[] { "nm1", "nm2"}, output);
	    
	    assertEquals(expected, output.toString());
	}
	
	@Test
	public void testUpdate() throws JAXBException {
		
		String input = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<parameters>\n"
				+ "    <parameter>\n"
				+ "        <namespace>nm1</namespace>\n"
				+ "        <config-name>c-name-#01</config-name>\n"
				+ "        <config-type>c-type-#01</config-type>\n"
				+ "        <properties>\n"
				+ "            <property name=\"p-name-#01.01\">\n"
				+ "                <value>p-value-#01.01(+)</value>\n"
				+ "            </property>\n"
				+ "        </properties>\n"
				+ "    </parameter>\n"
				+ "    <parameter>\n"
				+ "        <namespace>nm2</namespace>\n"
				+ "        <config-name>c-name-#02</config-name>\n"
				+ "        <config-type>c-type-#02</config-type>\n"
				+ "        <properties>\n"
				+ "            <property name=\"p-name-#02.01\">\n"
				+ "                <value>p-value-#02.01</value>\n"
				+ "            </property>\n"
				+ "        </properties>\n"
				+ "    </parameter>\n"
				+ "</parameters>\n"
				;
		
		XcpParameter prm;
		prm = new XcpParameter();
		prm.setNamespace("nm1");
		prm.setConfigName("c-name-#01");
		prm.setConfigType("c-type-#01");
		prm.setPropertyName(Arrays.asList("p-name-#01.01(-)", "p-name-#01.02(-)"));
		prm.setPropertyValue(Arrays.asList("p-value-#01.01(-)", "p-value-#01.02(-)"));
		
		when(repo.findByName(eq(prm.getNamespace()), eq(prm.getConfigName()))).thenReturn(prm);
		when(repo.findByName(eq("nm2"), anyString())).thenReturn(null);
		
		InputStream is = new ByteArrayInputStream(input.getBytes());

		XcpConfigTool configTool = new XcpConfigTool(repo);
	    Collection<XcpParameter> paramList = configTool.importConfig("repo", "username", "password", is, false);
	    assertNotNull(paramList);
	    assertEquals(1, paramList.size());
	    prm = paramList.iterator().next();
	    assertEquals("[p-name-#01.01]", prm.getPropertyName().toString());
	    assertEquals("[p-value-#01.01(+)]", prm.getPropertyValue().toString());
	}

}
