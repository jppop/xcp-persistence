package org.pockito.xcp.repository;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.repository.command.XcpRepoCmdFactory;
import org.pockito.xcp.repository.command.XcpRepoCommand;
import org.pockito.xcp.repository.test.domain.EmailTemplate;
import org.pockito.xcp.repository.test.domain.WfEmailTemplate;

@RunWith(MockitoJUnitRunner.class)
public class XcpRepositoryImplTest extends BaseMockedTest {

	@BeforeClass
	public static void init() {
		XcpGenericRepoImplTest.mockXcpCmd = false;
	}
	
	@Test
	public void testCreateRelation() throws Exception {
		
		XcpRepoCommand cmd = XcpRepoCmdFactory.instance.create();
		
		EmailTemplate parent = new EmailTemplate();
		parent.setName("parent");
		EmailTemplate child = new EmailTemplate();
		child.setName("child");
		
		cmd.withinTransaction()
		    .create(parent)
		    .create(child)
		    .link(parent).to(child).with(WfEmailTemplate.class)
		    .commit();
		
		verify(parentProp).setProperty(any(WfEmailTemplate.class), eq(parent));
		verify(childProp).setProperty(any(WfEmailTemplate.class), eq(child));
		
		InOrder order = inOrder(txMock, em);
		order.verify(txMock).begin();
		order.verify(em).persist(parent);
		order.verify(em).persist(child);
		order.verify(em).persist(any(WfEmailTemplate.class));
		order.verify(txMock).commit();;
	}

}
