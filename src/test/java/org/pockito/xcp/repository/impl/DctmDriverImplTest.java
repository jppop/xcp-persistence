package org.pockito.xcp.repository.impl;

import org.junit.Test;
import org.pockito.xcp.repository.DctmDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.fc.client.IDfSession;

public class DctmDriverImplTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DctmDriverImplTest.class);
	@Test
	public final void test() {
		
		LOGGER.info("Hello");
		DctmDriver driver = new DctmDriverImpl();
		driver.setCredendatials("devbox", "dmadmin", "dmadmin");
		IDfSession session = driver.getSession();
		
		driver.releaseSession(session);
		
	}

}
