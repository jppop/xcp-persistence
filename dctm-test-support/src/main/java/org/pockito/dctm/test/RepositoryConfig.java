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

	public static final String REPOSITORY_PROPERTIES = "dctm-test-support.properties";

	public static final String OPT_REPOSITORY_CONFIG = "org.pockito.dctm.test.config";
	
	private static Properties properties;
	static {
		properties = new Properties();
		try {
			String configFilename = System.getProperty(OPT_REPOSITORY_CONFIG);
			InputStream is;
			if (configFilename == null) {
				is = Repository.class.getResourceAsStream(REPOSITORY_PROPERTIES);
				if (is == null) {
					is = Repository.class.getResourceAsStream("/" + REPOSITORY_PROPERTIES);
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
