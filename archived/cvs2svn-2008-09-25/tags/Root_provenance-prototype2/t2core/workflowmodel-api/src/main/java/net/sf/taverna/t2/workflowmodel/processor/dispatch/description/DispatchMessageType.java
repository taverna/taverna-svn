package net.sf.taverna.t2.workflowmodel.processor.dispatch.description;

/**
 * Enumeration of the possible message types passed between layers of the
 * dispatch stack.
 * 
 * @author Tom Oinn
 * 
 */
public enum DispatchMessageType {

	/**
	 * A reference to a queue of Job objects waiting to be used as input along
	 * with a list of activities to process them.
	 */
	JOB_QUEUE,

	/**
	 * A Job object and list of activities to be used to process the data in the
	 * Job. The Job will have been previously extracted from the JobQueue
	 */
	JOB,

	/**
	 * A Job object containing the result of a single activity invocation.
	 */
	RESULT,

	/**
	 * A (possibly partial) completion event from the layer below. This is only
	 * going to be used when the activity invocation is capable of streaming
	 * partial data back up through the dispatch stack before the activity has
	 * completed. Not all dispatch stack layers are compatible with this mode of
	 * operation, for example retry and recursion do not play well here!
	 */
	RESULT_COMPLETION,

	/**
	 * A failure message sent by the layer below to denote some kind of failure
	 * (surprisingly)
	 */
	ERROR;
	
}
