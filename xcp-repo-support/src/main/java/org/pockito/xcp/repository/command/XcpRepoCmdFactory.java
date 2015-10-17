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
	
	private ThreadLocal<XcpRepoCommand> sharedCmd = new ThreadLocal<XcpRepoCommand>(){
		@Override
		protected XcpRepoCommand initialValue() {
			return null;
		}
		
	};
	
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
	private final ThreadLocal<List<UnregisterCallback>> callbacks = new ThreadLocal<List<XcpRepoCmdFactory.UnregisterCallback>>(){

		@Override
		protected List<UnregisterCallback> initialValue() {
			return new ArrayList<XcpRepoCmdFactory.UnregisterCallback>();
		}
		
	};
	
	public void registerSharedCmd(final XcpRepoCommand cmd) {
		if (cmd == null) {
			unregisterSharedCmd();
		} else {
			if (this.sharedCmd.get() != null) {
				unregisterSharedCmd();
			}
			this.sharedCmd.set(cmd);
		}
	}
	
	public void unregisterSharedCmd() {
		for (UnregisterCallback unregisterCallback : callbacks.get()) {
			unregisterCallback.unregistered();
		}
		this.callbacks.get().clear();
		this.sharedCmd.set(null);
	}
	
	public XcpRepoCommand useSharedCmd(UnregisterCallback onUnregister) {
		if (this.sharedCmd.get() != null) {
			callbacks.get().add(onUnregister);
		}
		return this.sharedCmd.get();
	}

	public XcpRepoCommand getSharedCmd() {
		return this.sharedCmd.get();
	}
}
