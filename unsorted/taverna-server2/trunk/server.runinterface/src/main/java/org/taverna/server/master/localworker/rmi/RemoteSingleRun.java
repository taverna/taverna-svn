package org.taverna.server.master.localworker.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public interface RemoteSingleRun extends Remote {
	/**
	 * @return The name of the Baclava file to use for all inputs, or
	 *         <tt>null</tt> if no Baclava file is set.
	 */
	public String getInputBaclavaFile() throws RemoteException;

	/**
	 * Sets the Baclava file to use for all inputs. This overrides the use of
	 * individual inputs.
	 * 
	 * @param filename
	 *            The filename to use. Must not start with a <tt>/</tt> or
	 *            contain any <tt>..</tt> segments. Will be interpreted relative
	 *            to the run's working directory.
	 * @throws FilesystemAccessException
	 *             If the filename is invalid.
	 */
	public void setInputBaclavaFile(String filename) throws RemoteException;

	/**
	 * @return The list of input assignments.
	 */
	public List<RemoteInput> getInputs() throws RemoteException;

	/**
	 * Create an input assignment.
	 * 
	 * @param name
	 *            The name of the port that this will be an input for.
	 * @return The assignment reference.
	 */
	public RemoteInput makeInput(String name) throws RemoteException;

	/**
	 * @return The file (relative to the working directory) to write the outputs
	 *         of the run to as a Baclava document, or <tt>null</tt> if they are
	 *         to be written to non-Baclava files in a directory called
	 *         <tt>out</tt>.
	 */
	public String getOutputBaclavaFile() throws RemoteException;

	/**
	 * Sets where the output of the run is to be written to. This will cause the
	 * output to be generated as a Baclava document, rather than a collection of
	 * individual non-Baclava files in the subdirectory of the working directory
	 * called <tt>out</tt>.
	 * 
	 * @param filename
	 *            Where to write the Baclava file (or <tt>null</tt> to cause the
	 *            output to be written to individual files); overwrites any
	 *            previous setting of this value.
	 */
	public void setOutputBaclavaFile(String filename) throws RemoteException;

	/**
	 * @return When this run will expire, becoming eligible for automated
	 *         deletion.
	 */
	public Date getExpiry() throws RemoteException;

	/**
	 * Set when this run will expire.
	 * 
	 * @param d
	 *            Expiry time. Deletion will happen some time after that.
	 */
	public void setExpiry(Date d) throws RemoteException;

	/**
	 * @return The current status of the run.
	 */
	public RemoteStatus getStatus() throws RemoteException;

	/**
	 * Set the status of the run, which should cause it to move into the given
	 * state. This may cause some significant changes.
	 * 
	 * @param s
	 *            The state to try to change to.
	 */
	public void setStatus(RemoteStatus s) throws RemoteException;

	/**
	 * @return Handle to the main working directory of the run.
	 */
	public RemoteDirectory getWorkingDirectory() throws RemoteException;

	/**
	 * @return The list of listener instances attached to the run.
	 */
	public List<RemoteListener> getListeners() throws RemoteException;

	/**
	 * Add a listener to the run.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addListener(RemoteListener listener) throws RemoteException;

	/**
	 * @return The security context structure for this run.
	 */
	public RemoteSecurityContext getSecurityContext() throws RemoteException;

	/**
	 * Kill off this run, removing all resources which it consumes.
	 */
	public void destroy() throws RemoteException;
}
