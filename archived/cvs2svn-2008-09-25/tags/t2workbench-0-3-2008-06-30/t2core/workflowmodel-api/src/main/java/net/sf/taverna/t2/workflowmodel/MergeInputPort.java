package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.invocation.WorkflowDataToken;

/**
 * Input port on a Merge object
 * 
 * @author Tom Oinn
 * 
 */
public interface MergeInputPort extends EventHandlingInputPort {

	/**
	 * Receive an arbitrary workflow event. The index of this port relative to
	 * its parent Merge object is prepended to the event index and the event
	 * forwarded through the Merge output port to any targets.
	 * <p>
	 * If this is a workflow data token and the first such received under a
	 * given owning process ID the implementing method also must also store the
	 * cardinality, i.e. length of index array + depth of token. Subsequent
	 * events are matched to this, if they have unequal cardinality the parent
	 * Merge operation will throw a WorkflowStructureException as the merge
	 * would result in a collection which violated the constraints defined by
	 * the Taverna 2 data model.
	 * 
	 * @param e
	 *            arbitrary workflow event, will be forwarded unchanged other
	 *            than an alteration of the index array by prefixing the index
	 *            of this input port relative to the parent Merge object
	 */
	public void receiveEvent(WorkflowDataToken t);

}
