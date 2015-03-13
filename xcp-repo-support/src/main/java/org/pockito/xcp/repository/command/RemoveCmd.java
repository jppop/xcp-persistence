package org.pockito.xcp.repository.command;

import org.pockito.xcp.entitymanager.api.DmsEntityManager;
import org.pockito.xcp.exception.XcpPersistenceException;

public class RemoveCmd implements XcpPersistCommand {

	private final DmsEntityManager em;
	private final Object entity;
	
	public RemoveCmd(final DmsEntityManager em, Object entity) {
		this.em = em;
		this.entity = entity;
	}
	
	@Override
	public void execute() throws XcpPersistenceException {
		this.em.remove(this.entity);
	}
	
	@Override
	public String toString() {
		return String.format("Remove command [object: %s]", entity.toString());
	}
}