package org.pockito.xcp.repository.test.repo;

import java.util.List;

import org.pockito.xcp.repository.XcpGenericRepoImpl;
import org.pockito.xcp.repository.test.domain.EmailTemplate;
import org.pockito.xcp.repository.test.domain.Workflow;

public class EmailTemplateRepoImpl extends XcpGenericRepoImpl<EmailTemplate> implements EmailTemplateRepo {

	@Override
	public List<EmailTemplate> findByWorkflow(Workflow wf) {
		// TODO Auto-generated method stub
		return null;
	}

}
