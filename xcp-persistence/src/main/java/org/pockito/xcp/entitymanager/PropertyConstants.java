package org.pockito.xcp.entitymanager;

import org.pockito.xcp.repository.DmsEntityManagerFactory;

public interface PropertyConstants {

	public static final String DctmDriver = DmsEntityManagerFactory.class.getPackage().getName() + ".dctmDriver";
	public static final String Username = DmsEntityManagerFactory.class.getPackage().getName() + ".username";
	public static final String Password = DmsEntityManagerFactory.class.getPackage().getName() + ".password";
	public static final String Repository = DmsEntityManagerFactory.class.getPackage().getName() + ".repository";
	public static final String SessionLess = DmsEntityManagerFactory.class.getPackage().getName() + ".sessionLess";
	public static final String CacheManager = DmsEntityManagerFactory.class.getPackage().getName() + ".cacheManager";
}
