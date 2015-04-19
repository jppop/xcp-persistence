package org.pockito.xcp.repository.command;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public enum XcpRepoCmdFactory {

	instance;
	
	@Inject
	@Named("XcpRepoCommand")
	private Provider<XcpRepoCommand> repoProvider;
	
	private XcpRepoCommand sharedCmd = null;
	
	public XcpRepoCommand create() {
		XcpRepoCommand cmd = repoProvider.get();
		cmd.connect();
		return cmd;
	}

	public XcpRepoCommand create(String repository, String username, String password) {
		XcpRepoCommand cmd = repoProvider.get();
		cmd.connect(repository, username, password);
		return cmd;
	}

	public interface UnregisterCallback {
		void unregistered();
	}
	private final List<UnregisterCallback> callbacks = new ArrayList<XcpRepoCmdFactory.UnregisterCallback>();
	
	public void registerSharedCmd(final XcpRepoCommand cmd) {
		if (cmd == null) {
			unregisterSharedCmd();
		} else {
			if (this.sharedCmd != null) {
				unregisterSharedCmd();
			}
			this.sharedCmd = cmd;
		}
	}
	
	public void unregisterSharedCmd() {
		for (UnregisterCallback unregisterCallback : callbacks) {
			unregisterCallback.unregistered();
		}
		this.callbacks.clear();
		this.sharedCmd = null;
	}
	
	public XcpRepoCommand useSharedCmd(UnregisterCallback onUnregister) {
		if (this.sharedCmd != null) {
			callbacks.add(onUnregister);
		}
		return this.sharedCmd;
	}

	public XcpRepoCommand getSharedCmd() {
		return this.sharedCmd;
	}
}
