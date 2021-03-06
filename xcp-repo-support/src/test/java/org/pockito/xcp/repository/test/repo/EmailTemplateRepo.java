package org.pockito.xcp.repository.test.repo;

import java.util.List;

import org.pockito.xcp.repository.XcpGenericRepo;
import org.pockito.xcp.repository.test.domain.EmailTemplate;
import org.pockito.xcp.repository.test.domain.Workflow;

public interface EmailTemplateRepo extends XcpGenericRepo<EmailTemplate> {
	
	/**
	 * Finds the templates used by a given workflow.
	 * 
	 * @param wf
	 * @return
	 */
	List<EmailTemplate> findWfTemplates(Workflow wf);
	
	/**
	 * Finds the default template of a workflow.
	 * 
	 * @param wf
	 * @return
	 */
	EmailTemplate findWfDefaultTemplate(Workflow wf);

}
