package org.pockito.xcp.repository.command;

import javax.inject.Inject;
import javax.inject.Provider;

public enum XcpRepoCmdFactory {

	instance;
	
	@Inject
	private static Provider<XcpRepoCommand> repoProvider;
	private XcpRepoCommand sharedCmd = null;
	
	public XcpRepoCommand create() {
		return repoProvider.get();
	}

	public static XcpRepoCmdFactory getInstance() {
		return instance;
	}
	
	public void registerSharedCmd(final XcpRepoCommand cmd) {
		this.sharedCmd  = cmd;
	}
	
	public XcpRepoCommand getSharedCmd() {
		return this.sharedCmd;
	}
}
