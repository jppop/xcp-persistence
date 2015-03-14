package org.pockito.dctm.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public enum RepositoryConfig {

	NAME("repository.name"),
	BROKER_HOST("repository.docbroker.host"),
	BROKER_PORT("repository.docbroker.port"),
	SUPERUSER_NAME("repository.su.username"),
	SUPERUSER_PWD("repository.su.password"),
	OPERATOR_NAME("repository.operator.username"),
	OPERATOR_PWD("repository.operator.password"), // no more used
	;

	private static Properties properties;
	static {
		properties = new Properties();
		try {
			String configFilename = System.getProperty("Repository.config");
			InputStream is;
			if (configFilename == null) {
				is = Repository.class.getResourceAsStream("Repository.properties");
				if (is == null) {
					is = Repository.class.getResourceAsStream("/Repository.properties");
				}
			} else {
				is = new FileInputStream(configFilename);
			}
			if (is != null) {
				properties.load(is);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when loading configuration file", e);
		}
	}

	private String key;

	RepositoryConfig(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return properties.getProperty(key);
	}
}
