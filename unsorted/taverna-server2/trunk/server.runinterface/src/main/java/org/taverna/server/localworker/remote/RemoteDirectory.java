package org.taverna.server.localworker.remote;

import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Represents a directory that is the working directory of a workflow run, or a
 * sub-directory of it.
 * 
 * @author Donal Fellows
 * @see RemoteFile
 */
public interface RemoteDirectory extends RemoteDirectoryEntry {
	/**
	 * @return A list of the contents of the directory.
	 */
	public Collection<RemoteDirectoryEntry> getContents()
			throws RemoteException;

	/**
	 * Creates a sub-directory of this directory.
	 * 
	 * @param name
	 *            The name of the sub-directory.
	 * @return A handle to the newly-created directory.
	 */
	public RemoteDirectory makeSubdirectory(String name) throws RemoteException;

	/**
	 * Creates an empty file in this directory.
	 * 
	 * @param name
	 *            The name of the file to create.
	 * @return A handle to the newly-created file.
	 */
	public RemoteFile makeEmptyFile(String name) throws RemoteException;
}
