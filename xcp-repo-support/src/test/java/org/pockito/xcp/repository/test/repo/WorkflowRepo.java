package org.pockito.xcp.repository.test.repo;

import java.util.List;

import org.pockito.xcp.repository.XcpGenericRepo;
import org.pockito.xcp.repository.test.domain.EmailTemplate;
import org.pockito.xcp.repository.test.domain.Workflow;

public interface WorkflowRepo extends XcpGenericRepo<Workflow> {
	
	/**
	 * Finds workflows using a given template.
	 * 
	 * @param template
	 * @return
	 */
	public List<Workflow> findWfUsingTemplate(EmailTemplate template);
	
	/**
	 * Associates a template to a workflow.
	 * 
	 * @param wf
	 * @param template
	 * @param order
	 */
	public void useTemplate(Workflow wf, EmailTemplate template, int order);

	/**
	 * Finds workflows using a given template in a specific order
	 * 
	 * @param template
	 * @param order
	 * @return
	 */
	List<Workflow> findWfUsingTemplateWithOrder(EmailTemplate template, int order);

}
