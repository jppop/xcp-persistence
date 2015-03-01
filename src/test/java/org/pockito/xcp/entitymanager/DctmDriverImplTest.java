package org.pockito.xcp.entitymanager;

import org.junit.Ignore;
import org.junit.Test;
import org.pockito.xcp.entitymanager.DctmDriverImpl;
import org.pockito.xcp.repository.DctmDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.fc.client.IDfSession;

public class DctmDriverImplTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DctmDriverImplTest.class);
	
	@Ignore
	public final void test() {
		
		LOGGER.info("Hello");
		
		LOGGER.warn("Watch for the step!");
		
		final Logger audit = LoggerFactory.getLogger("org.pockito.xcp.audit");
		audit.info("Task added to the repository");
//		DctmDriver driver = new DctmDriverImpl();
//		driver.setCredendatials("devbox", "dmadmin", "dmadmin");
//		IDfSession session = driver.getSession();
//		
//		driver.releaseSession(session);
		
	}

}
