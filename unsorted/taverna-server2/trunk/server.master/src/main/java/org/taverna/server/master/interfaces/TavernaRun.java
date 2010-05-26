package org.taverna.server.master.interfaces;

import java.util.Date;
import java.util.List;

import org.taverna.server.master.SCUFL;
import org.taverna.server.master.Status;
import org.taverna.server.master.exceptions.BadStateChangeException;

/**
 * The interface to a taverna workflow run, or "run" for short.
 * 
 * @author Donal Fellows
 */
public interface TavernaRun {
	/**
	 * @return What was this run was create to execute.
	 */
	public SCUFL getWorkflow();

	/**
	 * @return When this run will expire, becoming eligible for automated
	 *         deletion.
	 */
	public Date getExpiry();

	/**
	 * Set when this run will expire.
	 * 
	 * @param d
	 *            Expiry time. Deletion will happen some time after that.
	 */
	public void setExpiry(Date d);

	/**
	 * @return The current status of the run.
	 */
	public Status getStatus();

	/**
	 * Set the status of the run, which should cause it to move into the given
	 * state. This may cause some significant changes.
	 * 
	 * @param s
	 *            The state to try to change to.
	 * @throws BadStateChangeException
	 *             If the change to the given state is impossible.
	 */
	public void setStatus(Status s) throws BadStateChangeException;

	/**
	 * @return Handle to the main working directory of the run.
	 */
	public Directory getWorkingDirectory();

	/**
	 * @return The list of listener instances attached to the run.
	 */
	public List<Listener> getListeners();

	/**
	 * Add a listener to the run.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addListener(Listener listener);

	/**
	 * @return The security context structure for this run.
	 */
	public TavernaSecurityContext getSecurityContext();

	/**
	 * Kill off this run, removing all resources which it consumes.
	 */
	public void destroy();
}
