package org.pockito.xcp.sample.todo.repository.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.pockito.xcp.repository.PersistedObject;
import org.pockito.xcp.repository.XcpGenericRepo;
import org.pockito.xcp.repository.guice.ModuleConfig;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.domain.Task;
import org.pockito.xcp.sample.todo.repository.PersonRepo;
import org.pockito.xcp.sample.todo.repository.TaskRepo;

import com.google.inject.Guice;
import com.google.inject.Injector;

public enum AppConfig {

	instance;
	
	public static final String PROPERTIES = "xcp-repository.properties";
	private static RepoProvider repoProvider = new RepoProvider();
	private static final Properties appProperties = new Properties();
	
	public PersonRepo getPersonRepo() {
		return (PersonRepo) repoProvider.getRepo(Person.class);
	}
	
	public TaskRepo getTaskRepo() {
		return (TaskRepo) repoProvider.getRepo(Task.class);
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
	
	static {
		Injector injector = Guice.createInjector(new GuiceModule());
		injector.injectMembers(repoProvider);
		loadProperties();
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
