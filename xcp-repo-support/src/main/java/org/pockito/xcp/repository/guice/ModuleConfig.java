package org.pockito.xcp.repository.guice;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.pockito.xcp.entitymanager.XcpEntityManagerFactory;
import org.pockito.xcp.entitymanager.api.DmsEntityManagerFactory;
import org.pockito.xcp.repository.XcpRepository;
import org.pockito.xcp.repository.XcpRepositoryFactory;
import org.pockito.xcp.repository.XcpRepositoryImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class ModuleConfig extends AbstractModule implements com.google.inject.Module {

	@Override
	protected void configure() {
		loadProperties(binder());
		requestStaticInjection(XcpRepositoryFactory.class);
		bind(DmsEntityManagerFactory.class).to(XcpEntityManagerFactory.class).in(Scopes.SINGLETON);
		bind(XcpRepository.class).to(XcpRepositoryImpl.class);
	}

	public static void install() {
		Guice.createInjector(new ModuleConfig());
	}

	private void loadProperties(Binder binder) {
		Properties appProperties = new Properties();
		try {
			String configFilename = System.getProperty("Repository.config");
			InputStream is;
			if (configFilename == null) {
				is = ModuleConfig.class
						.getResourceAsStream("/xcp-repository.properties");
				if (is == null) {
					is = ModuleConfig.class
							.getResourceAsStream("xcp-repository.properties");
				}
			} else {
				is = new FileInputStream(configFilename);
			}
			appProperties.load(is);
			Names.bindProperties(binder, appProperties);
		} catch (IOException e) {
			binder.addError(e);
		}
	}

}
