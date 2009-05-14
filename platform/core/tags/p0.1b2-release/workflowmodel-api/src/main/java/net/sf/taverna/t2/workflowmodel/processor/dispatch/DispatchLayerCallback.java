package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobQueueEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

/**
 * Provided to methods handling events in a dispatch layer to allow those
 * methods to emit further events and manipulate the state model of the layer in
 * various ways.
 * <p>
 * Remember when sending new events to always ensure the process and iteration
 * parts of the old event are preserved. If your layer needs to add or remove
 * parts of the process identifier it should define the methods in
 * DispatchLayerControlBoundary
 * 
 * @author Tom Oinn
 * 
 */
public interface DispatchLayerCallback {

	/**
	 * Call this to clear any layer state that may have been created. In general
	 * this is called by layers which have been accumulating state on a
	 * per-iteration basis but which now know that they cannot receive any
	 * further events with that combination of iteration and process identifier,
	 * probably because the layer itself has sent a final event.
	 */
	public void clearLayerState();

	/**
	 * Send an error event in response to the event being handled by the
	 * recipient of the callback
	 */
	public void sendError(DispatchErrorEvent error);

	/**
	 * Send a result event in response to the event being handled by the
	 * recipient of the callback
	 */
	public void sendResult(DispatchResultEvent result);

	/**
	 * Send a result completion event in response to the event being handled by
	 * the recipient of the callback. This is used to indicate that an activity
	 * has completed stream processing, there are't many layers that will ever
	 * call this method.
	 */
	public void sendResultCompletion(DispatchCompletionEvent completion);

	/**
	 * Send a job event in response to the event being handled by the recipient
	 * of the callback
	 */
	public void sendJob(DispatchJobEvent job);

	/**
	 * Send a job queue in response to the event being handled by the recipient
	 * of the callback (it's very unlikely you'll be using this method)
	 */
	public void sendJobQueue(DispatchJobQueueEvent queue);

	/**
	 * Construct a new result event, automatically inserting the correct process
	 * identifier, index and context. Use this unless you need to explicitly
	 * manipulate these properties.
	 * 
	 * @param data
	 *            a map of named outputs from the underlying activity, where the
	 *            keys in the map are the names of output ports on the parent
	 *            processor, <em>not</em> those on the activity.
	 * @param isStreaming
	 *            set this to true if the result is part of a streamed activity
	 *            invocation. In general if you're using this method this should
	 *            be set to false but there may be exceptions
	 */
	public DispatchResultEvent createResultEvent(Map<String, T2Reference> data,
			boolean isStreaming);

	/**
	 * Construct a new error event, automatically inserting the correct process
	 * identifier, index and context. Use this unless you need to explicitly
	 * manipulate these properties.
	 * 
	 * @param message
	 *            a free text message describing the error
	 * @param cause
	 *            a throwable providing more detail, may be null
	 * @param type
	 *            the type of error, whether data, process or security related
	 * @param sourceActivity
	 *            the activity which led to this error, may be null
	 */
	public DispatchErrorEvent createErrorEvent(String message, Throwable cause,
			DispatchErrorType type, Activity<?> sourceActivity);

	/**
	 * Construct a new job event, automatically inserting the correct process
	 * identifier, index and context. Use this unless you need to explicitly
	 * manipulate these properties.
	 * 
	 * @param data
	 *            the input data for the job, keys are the names of processor
	 *            input ports
	 * @param activities
	 *            a list of activities to be used to process the data
	 */
	public DispatchJobEvent createJobEvent(Map<String, T2Reference> data,
			List<? extends Activity<?>> activities);

}
