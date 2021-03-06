package org.taverna.server.master.interfaces;

import java.security.Principal;
import java.util.List;

import org.taverna.server.master.common.Workflow;
import org.taverna.server.master.exceptions.NoCreateException;
import org.taverna.server.master.exceptions.NoDestroyException;
import org.taverna.server.master.exceptions.NoUpdateException;

/**
 * Simple policy interface.
 * 
 * @author Donal Fellows
 */
public interface Policy {
	/**
	 * @return The maximum number of runs that the system can support.
	 */
	public int getMaxRuns();

	/**
	 * Get the limit on the number of runs for this user.
	 * 
	 * @param user
	 *            Who to get the limit for
	 * @return The maximum number of runs for this user, or <tt>null</tt> if no
	 *         per-user limit is imposed and only system-wide limits are to be
	 *         enforced.
	 */
	public Integer getMaxRuns(Principal user);

	/**
	 * Test whether the user can create an instance of the given workflow.
	 * 
	 * @param user
	 *            Who wants to do the creation.
	 * @param workflow
	 *            The workflow they wish to instantiate.
	 * @throws NoCreateException
	 *             If they may not instantiate it.
	 */
	public void permitCreate(Principal user, Workflow workflow)
			throws NoCreateException;

	/**
	 * Test whether the user can destroy a workflow instance run or manipulate
	 * its expiry date.
	 * 
	 * @param user
	 *            Who wants to do the deletion.
	 * @param run
	 *            What they want to delete.
	 * @throws NoDestroyException
	 *             If they may not destroy it.
	 */
	public void permitDestroy(Principal user, TavernaRun run)
			throws NoDestroyException;

	/**
	 * Return whether the user has access to a particular workflow run.
	 * <b>Note</b> that this does not throw any exceptions!
	 * 
	 * @param user
	 *            Who wants to read the workflow's state.
	 * @param run
	 *            What do they want to read from.
	 * @return Whether they can read it. Note that this check is always applied
	 *         before testing whether the workflow can be updated or deleted by
	 *         the user.
	 */
	public boolean permitAccess(Principal user, TavernaRun run);

	/**
	 * Test whether the user can modify a workflow run (other than for its
	 * expiry date).
	 * 
	 * @param user
	 *            Who wants to do the modification.
	 * @param run
	 *            What they want to modify.
	 * @throws NoUpdateException
	 *             If they may not modify it.
	 */
	public void permitUpdate(Principal user, TavernaRun run)
			throws NoUpdateException;

	/**
	 * Get the workflows that the given user may execute.
	 * 
	 * @param user
	 *            Who are we finding out on behalf of.
	 * @return A list of workflows that they may instantiate, or <tt>null</tt>
	 *         if any workflow may be submitted.
	 */
	public List<Workflow> listPermittedWorkflows(Principal user);
}
