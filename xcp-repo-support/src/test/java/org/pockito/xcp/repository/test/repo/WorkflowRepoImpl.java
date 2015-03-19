package org.pockito.xcp.repository.test.repo;

import java.util.ArrayList;
import java.util.List;

import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.repository.XcpGenericRepoImpl;
import org.pockito.xcp.repository.test.domain.EmailTemplate;
import org.pockito.xcp.repository.test.domain.WfEmailTemplate;
import org.pockito.xcp.repository.test.domain.Workflow;

public class WorkflowRepoImpl extends XcpGenericRepoImpl<Workflow> implements WorkflowRepo {

	@Override
	public List<Workflow> findWfUsingTemplate(EmailTemplate template) {
		return findByChild(template, WfEmailTemplate.class, null);
	}

	public List<Workflow> findByTemplate2(EmailTemplate template) {
		DmsTypedQuery<Workflow> query = cmd().getEntityManager().createParentRelativesQuery(template,
				WfEmailTemplate.class, Workflow.class, null);
		return query.getResultList();
	}

	@Override
	public List<Workflow> findWfUsingTemplateWithOrder(EmailTemplate template, int order) {
		DmsTypedQuery<Workflow> query = cmd().getEntityManager().createParentRelativesQuery(template,
				WfEmailTemplate.class, Workflow.class, "order_no = :order")
				.setParameter("order", order);
		return query.getResultList();
	}

	public List<Workflow> findByTemplate(EmailTemplate template, int order) {
		DmsTypedQuery<WfEmailTemplate> query = cmd()
				.createNativeQuery(
						"select r_object_id from dm_relation where relation_name = 'dm_wf_email_template'"
								+ " and child_id = :templateId and order_no = :order", WfEmailTemplate.class)
				.setParameter("templateId", template.getId()).setParameter("order", order);

		List<WfEmailTemplate> emailTemplates = query.getResultList();
		List<Workflow> workflows = new ArrayList<Workflow>();
		for (WfEmailTemplate wfEmailTemplate : emailTemplates) {
			workflows.add(wfEmailTemplate.getWf());
		}
		return workflows;
	}

	@Override
	public void useTemplate(Workflow wf, EmailTemplate template, int order) {

		// create the relation object
		WfEmailTemplate relation = new WfEmailTemplate();
		relation.setOrder(order);

		// link the template
		// FIXME: should use command pattern!
		cmd().withinTransaction().link(wf).to(template).with(relation);
		commit();
	}

}
