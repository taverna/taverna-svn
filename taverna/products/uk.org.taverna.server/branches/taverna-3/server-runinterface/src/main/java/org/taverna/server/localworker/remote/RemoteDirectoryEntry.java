/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.localworker.remote;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * An entry in a {@link RemoteDirectory} representing a file or sub-directory.
 * 
 * @author Donal Fellows
 * @see RemoteDirectory
 * @see RemoteFile
 */
public interface RemoteDirectoryEntry extends Remote {
	/**
	 * @return The "local" name of the entry. This will never be "<tt>..</tt>"
	 *         or contain the character "<tt>/</tt>".
	 * @throws RemoteException
	 *             If anything goes wrong with the communication.
	 */
	@NonNull
	public String getName() throws RemoteException;

	/**
	 * Gets the directory containing this directory entry.
	 * 
	 * @return A directory handle, or <tt>null</tt> if called on the workflow
	 *         run's working directory.
	 * @throws RemoteException
	 *             If anything goes wrong with the communication.
	 */
	@Nullable
	public RemoteDirectory getContainingDirectory() throws RemoteException;

	/**
	 * Destroy this directory entry, deleting the file or sub-directory. The
	 * workflow run's working directory can never be manually destroyed.
	 * 
	 * @throws RemoteException
	 *             If anything goes wrong with the communication.
	 * @throws IOException
	 *             If things go wrong when deleting the directory entry.
	 */
	public void destroy() throws RemoteException, IOException;
}
