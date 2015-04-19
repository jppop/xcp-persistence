package org.pockito.xcp.repository.guice;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.pockito.xcp.entitymanager.XcpEntityManagerFactory;
import org.pockito.xcp.entitymanager.api.DmsEntityManagerFactory;
import org.pockito.xcp.repository.command.XcpRepoCmdFactory;
import org.pockito.xcp.repository.command.XcpRepoCmdImpl;
import org.pockito.xcp.repository.command.XcpRepoCommand;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class ModuleConfig extends AbstractModule implements Module {

	public static final String PROPERTIES = "xcp-repository.properties";
	public static final String OPT_CONFIG = "org.pockito.xcp.repository.guice.config";

	private final String propFilename;
	
	public ModuleConfig() {
		this.propFilename = PROPERTIES;
	}
	
	public ModuleConfig(String propFileName) {
		this.propFilename = propFileName == null ? PROPERTIES : propFileName;
	}
	
	@Override
	protected void configure() {
		loadProperties(binder());
//		requestStaticInjection(XcpRepoCmdFactory.class);
		bind(DmsEntityManagerFactory.class).to(XcpEntityManagerFactory.class).in(Scopes.SINGLETON);
		bind(XcpRepoCommand.class).annotatedWith(Names.named("XcpRepoCommand")).to(XcpRepoCmdImpl.class);
		requestInjection(XcpRepoCmdFactory.instance);
	}

	public static void install() {
		Guice.createInjector(new ModuleConfig());
	}

	public static void install(String propFilename) {
		Guice.createInjector(new ModuleConfig(propFilename));
	}

	private void loadProperties(Binder binder) {
		Properties appProperties = new Properties();
		try {
			String configFilename = System.getProperty(OPT_CONFIG);
			InputStream is;
			if (configFilename == null) {
				is = ModuleConfig.class
						.getResourceAsStream("/" + propFilename);
				if (is == null) {
					is = ModuleConfig.class
							.getResourceAsStream(propFilename);
				}
			} else {
				is = new FileInputStream(configFilename);
			}
			if (is != null) {
				appProperties.load(is);
				Names.bindProperties(binder, appProperties);
			}
		} catch (IOException e) {
			binder.addError(e);
		}
	}

	public String getProppertyFilename() {
		return propFilename;
	}

}
