package net.sf.taverna.t2.facade;

/**
 * A simple state enumeration for a workflow instance accessed through a
 * WorkflowInstanceFacade
 * 
 * @author Tom Oinn
 * 
 */
public enum WorkflowInstanceStatus {
	/**
	 * The workflow instance has been constructed but has neither been fired nor
	 * had data pushed to it and is therefore in a ready but inactive state
	 */
	READY,

	/**
	 * The workflow instance is currently processing data
	 */
	RUNNING,

	/**
	 * The workflow instance has completed processing data and will emit no
	 * further output
	 */
	COMPLETED,

	/**
	 * The workflow instance has failed and will emit no further output
	 */
	FAILED,

	/**
	 * The workflow instance has been paused, this may only be reached from the
	 * state RUNNING and may only transition to that state
	 */
	PAUSED;

}
