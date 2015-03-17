package org.pockito.xcp.repository.test.repo;

import java.util.List;

import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.repository.XcpGenericRepoImpl;
import org.pockito.xcp.repository.test.domain.EmailTemplate;
import org.pockito.xcp.repository.test.domain.WfEmailTemplate;
import org.pockito.xcp.repository.test.domain.Workflow;

public class EmailTemplateRepoImpl extends XcpGenericRepoImpl<EmailTemplate> implements EmailTemplateRepo {

	@Override
	public List<EmailTemplate> findWfTemplates(Workflow wf) {
		DmsTypedQuery<EmailTemplate> query = cmd().getEntityManager().createChildRelativesQuery(wf,
				WfEmailTemplate.class, EmailTemplate.class, null);
		return query.getResultList();
	}

	@Override
	public EmailTemplate findWfDefaultTemplate(Workflow wf) {
		EmailTemplate template = null;
		DmsTypedQuery<EmailTemplate> query = cmd().getEntityManager().createChildRelativesQuery(wf,
				WfEmailTemplate.class, EmailTemplate.class, "order_no = -1");
		List<EmailTemplate> templates = query.getResultList();
		if (templates.size() == 1) {
			template = templates.get(0);
		}
		return template;
	}

}
