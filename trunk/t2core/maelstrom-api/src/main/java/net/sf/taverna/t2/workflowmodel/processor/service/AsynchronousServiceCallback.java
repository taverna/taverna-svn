package net.sf.taverna.t2.workflowmodel.processor.service;

import java.util.Map;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.tsunami.SecurityAgentManager;
import net.sf.taverna.t2.cloudone.EntityIdentifier;

/**
 * The callback interface used by instances of AsynchronousService to push
 * results and failure messages back to the invocation layer.
 * 
 * TODO - add security hooks, i.e. some kind of 'getSecurityAgentProxies', need
 * to actually write the security agent framework first though!
 * 
 * @author Tom Oinn
 * 
 */
public interface AsynchronousServiceCallback {

	/**
	 * Services use a DataManager instance to resolve the identifiers in the
	 * input data and to store and register result data.
	 * 
	 * @return
	 */
	public DataManager getLocalDataManager();

	/**
	 * Services access a SecurityAgentManager to handle authentication with the
	 * external resource. The SecurityManager provides access to a set of
	 * security agents, or will when it's actually implemented! In the meantime
	 * this is just a placeholder and you can't actually do anything with it,
	 * it's just here to prevent API churn when we implement the security layer
	 * in the near future.
	 */
	public SecurityAgentManager getLocalSecurityManager();

	/**
	 * Push a map of named identifiers out to the invocation layer which is then
	 * responsible for wrapping them up into an appropriate Job object and
	 * sending it up the dispatch stack. The keys of the map are names local to
	 * the service, the callback object is responsible for rewriting them
	 * according to the service mapping rules (i.e. Service.getXXXPortMapping)
	 * 
	 * @param data
	 *            a single result data packet
	 * @param index
	 *            the index of the result in the context of this single process
	 *            invocation. If there's no streaming involved this should be a
	 *            zero length int[].
	 */
	public void receiveResult(Map<String, EntityIdentifier> data, int[] index);

	/**
	 * If (and only if) the service is streaming data then this method can be
	 * called to signal a (possibly partial) completion of the stream. If this
	 * is a total completion event, i.e. one with a zero length index array and
	 * there have been no result data sent the callback object will create a
	 * single job containing empty lists and send that instead otherwise it will
	 * be passed straight through. The index array is relative to this
	 * particular service invocation as the invocation has no contextual
	 * awareness.
	 * 
	 * @param completionIndex
	 */
	public void receiveCompletion(int[] completionIndex);

	/**
	 * If the job fails (as opposed to succeeding and sending an error for which
	 * the receiveResult method is used) this method will cause an error to be
	 * sent up the dispatch stack, triggering any appropriate handling methods
	 * such as retry, failover etc. This particular method accepts both a free
	 * text message and an instance of Throwable for additional information.
	 * 
	 * @param message
	 * @param t
	 */
	public void fail(String message, Throwable t);

	/**
	 * If the job fails (as opposed to succeeding and sending an error for which
	 * the receiveResult method is used) this method will cause an error to be
	 * sent up the dispatch stack, triggering any appropriate handling methods
	 * such as retry, failover etc. This method just takes a free text message
	 * for cases where a failure is properly described by an instance of
	 * Throwable
	 * 
	 * @param message
	 */
	public void fail(String message);

}
