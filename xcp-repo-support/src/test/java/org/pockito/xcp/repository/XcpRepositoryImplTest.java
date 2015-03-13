package org.pockito.xcp.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.repository.test.domain.Document;
import org.pockito.xcp.repository.test.domain.WfEmailTemplate;

@RunWith(MockitoJUnitRunner.class)
public class XcpRepositoryImplTest {

	@Test
	public void test() throws Exception {
		fail("Not yet implemented");
		
		XcpRepository repo = XcpRepositoryFactory.getInstance().create();
		
		Document parent = new Document();
		Document child = new Document();
		repo.withinTransaction()
		    .create(parent)
		    .create(child)
		    .link(parent).to(child).with(WfEmailTemplate.class)
		    .commit();
	}

}
