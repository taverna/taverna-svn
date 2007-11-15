package net.sf.taverna.t2.facade;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Implement and use with the WorkflowInstanceFacade to listen for data
 * production events from the underlying workflow instance
 * 
 * @author Tom Oinn
 * 
 */
public interface ResultListener {

	/**
	 * Called when a new result token is produced by the workflow instance.
	 * 
	 * @param token
	 *            Entity identifier of the token
	 * @param index
	 *            If the token is part of a stream of tokens this will contain
	 *            the index of this particular token within the stream. If the
	 *            entity is a final one this index will be the empty int array
	 * @param portName
	 *            The name of the output port on the workflow from which this
	 *            token is produced
	 * @param owningProcess
	 * 		      The id of the owning process
	 */
	public void resultTokenProduced(EntityIdentifier token, int[] index,
			String portName, String owningProcess);

}
