package org.pockito.xcp.test.domain;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.Child;
import org.pockito.xcp.annotations.Parent;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;

/**
 * A built-in relation type
 * @author jfrancon
 *
 */
@XcpEntity(namespace = "dm")
@XcpType(type = XcpTypeCategory.RELATION, name = "wf_email_template")
public class WfEmailTemplate extends PersistedObject {

	@Parent
	private Document wf; 

	@Child
	private Document template;

	@Attribute(name = "order_no")
	private int order;
	
	public Document getWf() {
		return wf;
	}

	public void setWf(Document wf) {
		this.wf = wf;
	}

	public Document getTemplate() {
		return template;
	}

	public void setTemplate(Document template) {
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
