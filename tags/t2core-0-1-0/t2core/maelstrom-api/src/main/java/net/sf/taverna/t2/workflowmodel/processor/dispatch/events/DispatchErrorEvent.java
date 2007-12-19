package net.sf.taverna.t2.workflowmodel.processor.dispatch.events;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifierException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType;

/**
 * Message within the dispatch stack representing a single error report. This
 * may then be handled by upstream layers to retry jobs etc. If it reaches the
 * top of the dispatch stack the behaviour is configurable but by default it
 * will abort that workflow instance, being treated as a catastrophic
 * unhandleable problem.
 * 
 * @author Tom Oinn
 * 
 */
public class DispatchErrorEvent extends
		AbstractDispatchEvent<DispatchErrorEvent> {

	private Throwable cause;
	private String message;
	private DispatchErrorType failureType;
	private Activity<?> failedActivity;

	/**
	 * Create a new error event
	 * 
	 * @param owningProcess
	 * @param index
	 * @param context
	 * @param errorMessage
	 * @param t
	 */
	public DispatchErrorEvent(String owningProcess, int[] index,
			InvocationContext context, String errorMessage, Throwable t,
			DispatchErrorType failureType, Activity<?> failedActivity) {
		super(owningProcess, index, context);
		this.message = errorMessage;
		this.cause = t;
		this.failureType = failureType;
		this.failedActivity = failedActivity;
	}

	/**
	 * Return the type of failure, this is used by upstream dispatch layers to
	 * determine whether they can reasonably handle the error message
	 */
	public DispatchErrorType getFailureType() {
		return this.failureType;
	}

	/**
	 * Return the Activity instance which failed to produce this error message
	 */
	public Activity<?> getFailedActivity() {
		return this.failedActivity;
	}

	/**
	 * Return the throwable behind this error, or null if there was no exception
	 * raised to create it.
	 * 
	 * @return
	 */
	public Throwable getCause() {
		return this.cause;
	}

	/**
	 * Return the textual message representing this error
	 * 
	 * @return
	 */
	public String getMessage() {
		return this.message;
	}

	@Override
	public DispatchErrorEvent popOwningProcess()
			throws ProcessIdentifierException {
		return new DispatchErrorEvent(popOwner(), index, context, message,
				cause, failureType, failedActivity);
	}

	@Override
	public DispatchErrorEvent pushOwningProcess(String localProcessName)
			throws ProcessIdentifierException {
		return new DispatchErrorEvent(pushOwner(localProcessName), index,
				context, message, cause, failureType, failedActivity);
	}

	/**
	 * DispatchMessageType.ERROR
	 */
	@Override
	public DispatchMessageType getMessageType() {
		return DispatchMessageType.ERROR;
	}

}
