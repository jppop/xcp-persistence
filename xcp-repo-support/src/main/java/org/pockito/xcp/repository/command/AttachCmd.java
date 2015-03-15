package org.pockito.xcp.repository.command;

import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.exception.XcpPersistenceException;

public class AttachCmd implements XcpPersistCommand {

	private final DmsEntityManager em;
	private final Object entity;
	private final String filename;
	private final String contentType;
	
	public AttachCmd(DmsEntityManager em, Object entity, String filename, String contentType) {
		super();
		this.em = em;
		this.entity = entity;
		this.filename = filename;
		this.contentType = contentType;
	}

	@Override
	public void execute() throws XcpPersistenceException {
		this.em.addAttachment(entity, filename, contentType);
	}

}
