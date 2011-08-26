package net.sf.taverna.t2.workflowmodel;

import java.util.List;

/**
 * Allows multiple outputs to be routed to a single input within the dataflow.
 * The merge operation defines a total order over its input ports, this order is
 * used to modify the index array of all incoming events by adding the port
 * index as a prefix to that array. At a conceptual level this means that any
 * event coming in is 'binned' by port index creating a collection of whatever
 * the port type was. This also places a constraint that all input ports must
 * have the same cardinality (i.e. depth + length of index array must be equal
 * for all events on all ports). If this constraint is violated the merge
 * operation is free to throw a WorkflowStructureException at runtime although
 * it would be preferable if implementing classes were capable of static type
 * analysis to preclude this from happening.
 * 
 * @author Tom Oinn
 * 
 */
public interface Merge extends TokenProcessingEntity {

	/**
	 * The Merge object contains an ordered list of InputPort objects. Data and
	 * completion events arriving at an input port have the index of that input
	 * within the list prepended to their index array, effectively placing them
	 * in a virtual collection the top level of which corresponds to the various
	 * input ports defined within the Merge node. When final completion events
	 * from all input ports are received the Merge object registers the top
	 * level collection with the attached DataManager and emits it and the
	 * completion event through the single output port.
	 * 
	 * @return Ordered list of InputPort objects
	 */
	List<? extends MergeInputPort> getInputPorts();

	/**
	 * The Merge object has a single output port through which modified events
	 * are emitted as described in the javadoc for getInputPorts
	 * 
	 * @return OutputPort for this Merge object
	 */
	EventForwardingOutputPort getOutputPort();

}
