package net.sf.taverna.t2.workflowmodel.processor.dispatch;

/**
 * Defines the scope in which a dispatch layer manages state. This allows the
 * wrapper code that calls the dispatch layers to set up the appropriate state
 * automatically and obviates the layer itself from this responsibility.
 * 
 * @author Tom Oinn
 * 
 */
public enum DispatchLayerStateScoping {

	/**
	 * The layer has no state
	 */
	NONE,

	/**
	 * The layer has a state model which is keyed on the process identifier part
	 * of the received token only
	 */
	PROCESS,

	/**
	 * The layer has a state model which is keyed on the combination of process
	 * identifier and iteration index array. Most layers that have state will
	 * use this model.
	 */
	ITERATION;

}
