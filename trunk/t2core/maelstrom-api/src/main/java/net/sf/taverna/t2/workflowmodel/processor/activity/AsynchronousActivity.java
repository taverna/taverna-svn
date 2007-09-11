package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * A concrete invokable service with an asynchronous invocation API and no
 * knowledge of invocation context. This is the most common concrete service
 * type in Taverna 2, it has no knowledge of any enclosing iteration or other
 * handling process. The service may stream results in the sense that it can use
 * the AsynchronousServiceCallback object to push multiple results followed by a
 * completion event. If a completion event is received by the callback before
 * any data events the callback will insert a data event containing empty
 * collections of the appropriate depth.
 * 
 * @author Tom Oinn
 * 
 */
public interface AsynchronousActivity<T> extends Activity<T> {

	/**
	 * Invoke the service in an asynchronous manner. The service uses the
	 * specified ServiceCallback object to push results, errors and completion
	 * events back to the dispatch stack.
	 */
	public void executeAsynch(Map<String, EntityIdentifier> data,
			AsynchronousActivityCallback callback);

}
