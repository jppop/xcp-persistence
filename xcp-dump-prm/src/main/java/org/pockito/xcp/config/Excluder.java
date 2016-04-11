package org.pockito.xcp.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Excluder {

	private String filename;
	private boolean inited = false;
	private List<String> excludeTypes = new ArrayList<String>();
	private List<String> excludeNames = new ArrayList<String>();

	public Excluder(String filename) {
		super();
		this.filename = filename;
	}

	public boolean isTypeExluded(String type) throws IOException {
		boolean exluded = false;
		if (!this.inited) {
			init();
		}
		exluded = this.excludeTypes.contains(type);
		return exluded;
	}
	
	public boolean isNameExluded(String name) throws IOException {
		boolean exluded = false;
		if (!this.inited) {
			init();
		}
		exluded = this.excludeNames.contains(name);
		return exluded;
	}
	
	private void init() throws IOException {
		Properties excludeList = loadExcludeList(this.filename);
		Set<Object> keys = excludeList.keySet();
		for (Object k : keys) {
			String key = (String) k;
			String value = excludeList.getProperty(key);
			if ("type".equalsIgnoreCase(key)) {
				excludeTypes.add(value);
			} else if ("name".equalsIgnoreCase(key)) {
				excludeNames.add(value);
			}
		}
		this.inited = true;
	}
	
	private Properties loadExcludeList(String filename) throws IOException {
		Properties exludeList = new Properties();
		InputStream is = new FileInputStream(filename);
		exludeList.load(is);
		return exludeList;
	}
}
