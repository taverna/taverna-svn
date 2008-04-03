package net.sf.taverna.t2.workflowmodel.processor.dispatch.events;

/**
 * A simple enumeration of possible failure classes, used to determine whether
 * fault handling dispatch layers should attempt to handle a given failure
 * message.
 * 
 * @author Tom Oinn
 * 
 */
public enum DispatchErrorType {

	/**
	 * Indicates that the failure to invoke the activity was due to invalid
	 * input data, in this case there is no point in trying to invoke the
	 * activity again with the same data as it will always fail. Fault handling
	 * layers such as retry should pass this error type through directly; layers
	 * such as failover handlers should handle it as the input data may be
	 * applicable to other activities within the processor.
	 */
	DATA,

	/**
	 * Indicates that the failure was related to the invocation of the resource
	 * rather than the input data, and that an identical invocation at a later
	 * time may succeed.
	 */
	INVOCATION,

	/**
	 * Indicates that the failure was due to missing or incorrect authentication
	 * credentials and that retrying the activity invocation without modifying
	 * the credential set is pointless.
	 */
	AUTHENTICATION;

}
