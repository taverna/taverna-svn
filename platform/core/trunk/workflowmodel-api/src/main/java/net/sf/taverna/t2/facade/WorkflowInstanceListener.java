package net.sf.taverna.t2.facade;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;

/**
 * Listener used to notify interested parties of changes to workflow instance
 * state, result data, completion and failure events. This replaces the previous
 * FailureListener and ResultListener interfaces.
 * <p>
 * Where there are multiple potential messages generated, for example where a
 * result is generated then the workflow completes and therefore changes from
 * state RUNNING to state COMPLETED the message order will be the data message
 * first, then the state change followed by the completion message (whether
 * success or failure)
 * <p>
 * If a listener is attached to a workflow facade after messages have been
 * produced the listener will be immediately notified of the final sequence of
 * messages where such indicate terminal states. These include completion,
 * failure and final result tokens. Similarly for non-terminal states where the
 * workflow instance is still running any completed data tokens (data tokens
 * emited by a workflow output port with an empty index array) will be messaged
 * to the listener
 * 
 * @author Tom Oinn
 * 
 */
public interface WorkflowInstanceListener {

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

	/**
	 * Called if the workflow fails in a critical and fundamental way. Most
	 * internal failures of individual process instances will not trigger this,
	 * being handled either by the per processor dispatch stack through retry,
	 * failover etc or by being converted into error tokens and injected
	 * directly into the data stream. This therefore denotes a catastrophic and
	 * unrecoverable problem.
	 * <p>
	 * No further messages will be sent to the listener once this has been
	 * called.
	 * 
	 * @param failedProcess
	 *            the process identifier of the entity within the workflow that
	 *            triggered the failure, if known
	 * @param invocationContext
	 *            the invocation context of the failed workflow
	 * @param workflowEntity
	 *            an entity within the workflow that was the source of the
	 *            failure, this may or may not be the specific failed component,
	 *            if the component is not known this will be the dataflow itself
	 * @param message
	 *            a free text message describing the failure
	 * @param cause
	 *            a Throwable corresponding to an underlying failure if there is
	 *            such a thing. This may be null if there is no exception or
	 *            error causing the failure as in the case of an explicit
	 *            cancellation of a workflow instance
	 */
	public void workflowFailed(ProcessIdentifier failedProcess,
			InvocationContext invocationContext,
			NamedWorkflowEntity workflowEntity, String message, Throwable cause);

	/**
	 * Called when the workflow completes successfully, this will be called
	 * after any result token messages, and guarantees that no further messages
	 * will be sent to this listener.
	 */
	public void workflowCompleted(ProcessIdentifier owningProcess);

	/**
	 * Called when the status of the workflow instance is modified
	 * 
	 * @param newStatus
	 */
	public void workflowStatusChanged(WorkflowInstanceStatus oldStatus,
			WorkflowInstanceStatus newStatus);

}
