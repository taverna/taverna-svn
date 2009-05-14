package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Carrier type for a set of monitorable properties produced from a monitorable
 * invocation. This is in effect a continuation, the invoker will call the
 * getProperties method to register the activity with the monitor, then use the
 * executeAsynch to actually invoke the activity.
 * <p>
 * Typically this interface will be implemented with an anonymous inner class
 * within a monitorable activity, and is used to work around the simultanous
 * requirement to return the monitorable properties immediately but to also
 * allow the activity to run in its own thread and not require it to handle
 * thread allocation
 * 
 * @author Tom Oinn
 * 
 */
public interface MonitorableActivityContinuation {

	/**
	 * Return the set of monitorable properties representing this invocation
	 */
	public Set<MonitorableProperty<?>> getProperties();

	/**
	 * Continue the invocation
	 */
	public void executeAsynch(Map<String, T2Reference> data,
			AsynchronousActivityCallback callback);

}
