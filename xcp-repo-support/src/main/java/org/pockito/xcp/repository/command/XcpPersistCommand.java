package org.pockito.xcp.repository.command;

import org.pockito.xcp.exception.XcpPersistenceException;

public interface XcpPersistCommand {
	void execute() throws XcpPersistenceException;
}