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
	public List<Workflow> findByTemplate(EmailTemplate template);
	
	public void addTemplate(Workflow wf, EmailTemplate template, int order);

}
