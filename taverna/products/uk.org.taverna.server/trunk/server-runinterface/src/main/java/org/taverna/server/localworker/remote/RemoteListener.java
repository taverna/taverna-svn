package org.taverna.server.localworker.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An event listener that is attached to a {@link RemoteSingleRun}.
 * 
 * @author Donal Fellows
 */
public interface RemoteListener extends Remote {
	/**
	 * @return The name of the listener.
	 */
	@NonNull
	public String getName() throws RemoteException;

	/**
	 * @return The type of the listener.
	 */
	@NonNull
	public String getType() throws RemoteException;

	/**
	 * @return The configuration document for the listener.
	 */
	@NonNull
	public String getConfiguration() throws RemoteException;

	/**
	 * @return The supported properties of the listener.
	 */
	@NonNull
	public String[] listProperties() throws RemoteException;

	/**
	 * Get the value of a particular property, which should be listed in the
	 * {@link #listProperties()} method.
	 * 
	 * @param propName
	 *            The name of the property to read.
	 * @return The value of the property.
	 */
	@NonNull
	public String getProperty(@NonNull String propName) throws RemoteException;

	/**
	 * Set the value of a particular property, which should be listed in the
	 * {@link #listProperties()} method.
	 * 
	 * @param propName
	 *            The name of the property to write.
	 * @param value
	 *            The value to set the property to.
	 */
	public void setProperty(@NonNull String propName, @NonNull String value)
			throws RemoteException;
}
