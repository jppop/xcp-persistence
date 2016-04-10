package org.pockito.xcp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.pockito.xcp.config.domain.PersistedObject;
import org.pockito.xcp.config.domain.XcpParameter;
import org.pockito.xcp.config.domain.XcpParameterRepo;
import org.pockito.xcp.repository.XcpGenericRepo;
import org.pockito.xcp.repository.guice.ModuleConfig;

import com.google.inject.Guice;
import com.google.inject.Injector;

public enum AppConfig {

	instance;
	
	public static final String PROPERTIES = "xcp-repository.properties";
	private static RepoProvider repoProvider = null;
	private static final Properties appProperties = new Properties();
	
	public XcpParameterRepo getXcpParameterRepo() {
		return (XcpParameterRepo) repoProvider.getRepo(XcpParameter.class);
	}
	
	public XcpGenericRepo<?> getRepo(Class<? extends PersistedObject> entityClass) {
		return repoProvider.getRepo(entityClass);
	}
	
	public String repository() {
		return appProperties.getProperty("org.pockito.xcp.repository.name");
	}
	
	public String username() {
		return appProperties.getProperty("org.pockito.xcp.repository.username");
	}
	
	public String password() {
		return appProperties.getProperty("org.pockito.xcp.repository.password");
	}
	
	public static String injector() {
		return appProperties.getProperty("injector", "guice");
	}
	
	static {
		loadProperties();
		Injector injector = Guice.createInjector(new GuiceModule());
		repoProvider = new RepoProvider();
		injector.injectMembers(repoProvider);
	}

	private static void loadProperties() {
		try {
			InputStream is;
			is = ModuleConfig.class
					.getResourceAsStream("/" + PROPERTIES);
			if (is == null) {
				is = ModuleConfig.class
						.getResourceAsStream(PROPERTIES);
			}
			if (is != null) {
				appProperties.load(is);
			}
		} catch (IOException e) {
		}
	}
}
