package net.sf.taverna.t2.invocation;

import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;

/**
 * Used to connect an entity within the workflow that can generate failures
 * (such as a Processor) to the parent dataflow without forcing a direct link
 * from processor to dataflow.
 * 
 * @author Tom Oinn
 * 
 */
public interface FailureHandler {

	/**
	 * Called by an entity in the workflow to indicate that a failure has
	 * occurred and request that the handler propagate this information to the
	 * parent, usually a dataflow
	 * 
	 * @param processIdentifier
	 *            the process identifier of the failed process, it is implicit
	 *            that anything below this should be considered failed as well.
	 * @param invocationContext
	 *            the invocation context to message with the failure in order to
	 *            ensure that no further processing takes place
	 * @param workflowEntity
	 *            the source of the failure
	 * @param message
	 *            a free text message describing the failure
	 * @param cause
	 *            a Throwable, which may be null, further specifying the
	 *            underlying failure
	 */
	public void processFailed(ProcessIdentifier processIdentifier,
			InvocationContext invocationContext,
			NamedWorkflowEntity workflowEntity, String message, Throwable cause);

}
