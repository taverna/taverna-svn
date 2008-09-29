package net.sf.taverna.t2.facade;

import net.sf.taverna.t2.invocation.WorkflowDataToken;

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
	 *            the WorkflowDataToken containing the result.
	 * @param portName
	 *            The name of the output port on the workflow from which this
	 *            token is produced, this now folds in the owning process which
	 *            was part of the signature for this method
	 */
	public void resultTokenProduced(WorkflowDataToken token, String portName);

}
