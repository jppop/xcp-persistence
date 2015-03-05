package org.pockito.xcp.entitymanager;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DctmDriverImplTest {

	private static final Logger logger = LoggerFactory.getLogger(DctmDriverImplTest.class);
	
	@Test
	public final void test() {
		
		logger.info("Hello");
		
		logger.warn("Watch for the step!");
		
		logger.debug("debug message!");

		logger.trace("trace message!");

		final Logger audit = LoggerFactory.getLogger("org.pockito.xcp.audit");
		audit.info("Task added to the repository");
//		DctmDriver driver = new DctmDriverImpl();
//		driver.setCredendatials("devbox", "dmadmin", "dmadmin");
//		IDfSession session = driver.getSession();
//		
//		driver.releaseSession(session);
		
	}

}
