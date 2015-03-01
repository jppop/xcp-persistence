package org.pockito.xcp.entitymanager;

import org.pockito.xcp.repository.DmsEntityManagerFactory;

public interface PropertyConstants {

	public static final String DriverClass = DmsEntityManagerFactory.class.getPackage().getName() + ".DctmDriver";
	public static final String Username = DmsEntityManagerFactory.class.getPackage().getName() + ".username";
	public static final String Password = DmsEntityManagerFactory.class.getPackage().getName() + ".password";
	public static final String Repository = DmsEntityManagerFactory.class.getPackage().getName() + ".repository";
}
