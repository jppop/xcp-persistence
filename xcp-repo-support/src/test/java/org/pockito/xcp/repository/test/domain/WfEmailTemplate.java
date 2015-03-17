package org.pockito.xcp.repository.test.domain;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.Child;
import org.pockito.xcp.annotations.Parent;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.repository.PersistedObject;

/**
 * A built-in relation type
 * @author jfrancon
 *
 */
@XcpEntity(namespace = "dm")
@XcpType(type = XcpTypeCategory.RELATION, name = "wf_email_template")
public class WfEmailTemplate extends PersistedObject {

	@Parent
	Workflow wf; 

	@Child
	EmailTemplate template;
	
	@Attribute(name = "order_no")
	private int order;

	public Workflow getWf() {
		return wf;
	}

	public void setWf(Workflow wf) {
		this.wf = wf;
	}

	public EmailTemplate getTemplate() {
		return template;
	}

	public void setTemplate(EmailTemplate template) {
		this.template = template;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return String.format(
				"WfEmailTemplate[id=%s, wf=%s, template=%s, order=%d, vstamp=%d]",
				getId(), 
				getWf(), getTemplate(), getOrder(),
				getvStamp()
				);
	}

}
