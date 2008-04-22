package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.monitor.MonitorableProperty;

/**
 * An extension of AsynchronousActivity with the additional stipulation that
 * implementing classes must return a set of monitorable properties for the
 * activity invocation instance when invoked. This allows for deep state
 * management, where the monitor state extends out from the workflow engine into
 * the remote resources themselves and is dependant on the resource proxied by
 * the activity implementation providing this information.
 * 
 * @author Tom Oinn
 * 
 */
public interface MonitorableAsynchronousActivity<ConfigType> extends
		AsynchronousActivity<ConfigType> {

	/**
	 * This has the same invocation semantics as
	 * AsynchronousActivity.executeAsynch and all implementations should also
	 * implement that method, with the difference that this one returns
	 * immediately with a set of monitorable properties which represent
	 * monitorable or steerable state within the invocation itself.
	 * 
	 * @param data
	 * @param callback
	 * @return a set of monitorable properties representing internal state of
	 *         the invoked resource
	 */
	public Set<MonitorableProperty<?>> executeAsynchWithMonitoring(
			Map<String, EntityIdentifier> data,
			AsynchronousActivityCallback callback);

}
