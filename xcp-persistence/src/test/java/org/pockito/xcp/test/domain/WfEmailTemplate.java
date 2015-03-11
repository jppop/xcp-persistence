package org.pockito.xcp.test.domain;

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
	Document wf; 

	@Child
	Document template;

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

	@Override
	public String toString() {
		return String.format(
				"WfEmailTemplate[id=%s, wf=%s, template=%s, vstamp=%d]",
				getId(), 
				getWf(), getTemplate(),
				getvStamp()
				);
	}

}
