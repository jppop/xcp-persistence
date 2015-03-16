package org.pockito.xcp.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
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
import org.pockito.xcp.repository.test.domain.Workflow;
import org.pockito.xcp.repository.test.repo.EmailTemplateRepoImpl;
import org.pockito.xcp.repository.test.repo.WorkflowRepoImpl;

@RunWith(MockitoJUnitRunner.class)
public class XcpGenericRepoImplTest extends BaseMockedTest {

	@BeforeClass
	public static void init() {
		XcpGenericRepoImplTest.mockXcpCmd = true;
	}
	
	@Test
	public void testCreateWithSharedCmd() {
		
		// create the repositories (in real life, should be injected)
		WorkflowRepoImpl wfRepo = new WorkflowRepoImpl();
		EmailTemplateRepoImpl templateRepo = new EmailTemplateRepoImpl();
		
		// create a shared command
		XcpRepoCommand sharedCmd = wfRepo.createSharedCmd();
		assertEquals(sharedCmd, XcpRepoCmdFactory.instance.getSharedCmd());
		
		// add a workflow
		Workflow wf = new Workflow();
		wf.setName("sample wf");
		wfRepo.add(wf);
		
		final int count = 2;
		EmailTemplate someTemplates[] = new EmailTemplate[count];
		
		// add some templates
		for (int i = 0; i < count; i++) {
			EmailTemplate template = new EmailTemplate();
			template.setName("sample template #" + Integer.toString(i));
			templateRepo.add(template);

			wfRepo.addTemplate(wf, template, i);

			someTemplates[i] = template;
		}
		
		InOrder order = inOrder(sharedCmd);
		order.verify(sharedCmd).setOwner(wfRepo);;
		order.verify(sharedCmd).withinTransaction();
		order.verify(sharedCmd).create(wf);
		for (int i= 0; i < count; i++) {
			order.verify(sharedCmd).withinTransaction();
			order.verify(sharedCmd).create(someTemplates[i]);
			order.verify(sharedCmd).withinTransaction();
			order.verify(sharedCmd).link(wf);
			order.verify(sharedCmd).to(someTemplates[i]);
			order.verify(sharedCmd).with(any(WfEmailTemplate.class));
		}
		
		// verify no commit calls has been done
		verify(sharedCmd, never()).commit();
		
		// now commit the shared command
		wfRepo.commitSharedCmd();
		order.verify(sharedCmd).commit();

		// check internal command has been reseted
		assertEquals(0, wfRepo.getCmd().size());
		assertNull(XcpRepoCmdFactory.instance.getSharedCmd());
		
	}

	@Test
	public void testCreateWithPrivateCmd() {
		
		// create the repositories (in real life, should be injected)
		WorkflowRepoImpl wfRepo = new WorkflowRepoImpl();
		EmailTemplateRepoImpl templateRepo = new EmailTemplateRepoImpl();
		
		// add a workflow
		Workflow wf = new Workflow();
		wf.setName("sample wf");
		wfRepo.add(wf);
		
		final int count = 2;
		EmailTemplate someTemplates[] = new EmailTemplate[count];
		
		// add some templates
		for (int i = 0; i < count; i++) {
			EmailTemplate template = new EmailTemplate();
			template.setName("sample template #" + Integer.toString(i));
			templateRepo.add(template);

			wfRepo.addTemplate(wf, template, i);

			someTemplates[i] = template;
		}
		
		XcpRepoCommand wfCmd = wfRepo.getCmd();
		assertNotNull(wfCmd);
		XcpRepoCommand templateCmd = templateRepo.getCmd();
		assertNotNull(templateCmd);
		
		InOrder order = inOrder(wfCmd, templateCmd);
		order.verify(wfCmd).withinTransaction();
		order.verify(wfCmd).create(wf);
		order.verify(wfCmd).commit();
		for (int i= 0; i < count; i++) {
			order.verify(templateCmd).withinTransaction();
			order.verify(templateCmd).create(someTemplates[i]);
			order.verify(templateCmd).commit();
			order.verify(wfCmd).withinTransaction();
			order.verify(wfCmd).link(wf);
			order.verify(wfCmd).to(someTemplates[i]);
			order.verify(wfCmd).with(any(WfEmailTemplate.class));
		}
		
	}

}
