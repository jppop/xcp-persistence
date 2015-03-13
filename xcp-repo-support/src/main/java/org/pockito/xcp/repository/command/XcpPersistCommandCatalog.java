package org.pockito.xcp.repository.command;

import org.pockito.xcp.entitymanager.api.DmsEntityManager;

public class XcpPersistCommandCatalog {

	private final DmsEntityManager em;
	
	public XcpPersistCommandCatalog(DmsEntityManager em) {
		this.em = em;
	}
	
	DmsEntityManager em() {
		return em;
	}
	
    public XcpPersistCommand persistCmd(Object entity) {
		return new PersistCmd(em(), entity);
	}

	public XcpPersistCommand removeCmd(Object entity) {
		return new RemoveCmd(em, entity);
	}

	public XcpPersistCommand addAttachmentCmd(Object entity, String filename, String contentType) {
		return new AttachCmd(em, entity, filename, contentType);
	}
}
