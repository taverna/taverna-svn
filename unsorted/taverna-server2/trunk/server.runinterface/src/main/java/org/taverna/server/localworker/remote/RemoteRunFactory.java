package org.taverna.server.localworker.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteRunFactory extends Remote {
	/**
	 * Makes a workflow run that will process a particular SCUFL document.
	 * 
	 * @param scufl
	 *            The (serialized) workflow to instantiate as a run.
	 * @return A remote handle for the run.
	 */
	public RemoteSingleRun make(String scufl) throws RemoteException;
}
