package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * A concrete invokable activity with an asynchronous invocation API and no
 * knowledge of invocation context. This is the most common concrete activity
 * type in Taverna 2, it has no knowledge of any enclosing iteration or other
 * handling process. The activity may stream results in the sense that it can use
 * the AsynchronousActivityCallback object to push multiple results followed by a
 * completion event. If a completion event is received by the callback before
 * any data events the callback will insert a data event containing empty
 * collections of the appropriate depth.
 * 
 * @param <ConfigurationType> the ConfigurationType associated with the Activity.
 * @author Tom Oinn
 * 
 */
public interface AsynchronousActivity<ConfigurationType> extends Activity<ConfigurationType> {

	/**
	 * Invoke the activity in an asynchronous manner. The activity uses the
	 * specified ActivityCallback object to push results, errors and completion
	 * events back to the dispatch stack.
	 */
	public void executeAsynch(Map<String, EntityIdentifier> data,
			AsynchronousActivityCallback callback);

}
