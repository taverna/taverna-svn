package org.taverna.server.master.localworker.rmi;

import java.rmi.RemoteException;

/**
 * Represents a file in the working directory of a workflow instance run, or
 * in some sub-directory of it.
 * 
 * @author Donal Fellows
 * @see RemoteDirectory
 */
public interface RemoteFile extends RemoteDirectoryEntry {
	/**
	 * @return The literal byte contents of the file.
	 */
	public byte[] getContents() throws RemoteException;

	/**
	 * Write the data to the file, totally replacing what was there before.
	 * 
	 * @param data
	 *            The literal bytes that will form the new contents of the file.
	 */
	public void setContents(byte[] data) throws RemoteException;

	/**
	 * @return The length of the file, in bytes.
	 */
	public long getSize() throws RemoteException;
}
