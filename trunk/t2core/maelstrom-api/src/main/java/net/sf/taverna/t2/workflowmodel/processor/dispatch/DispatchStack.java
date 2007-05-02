package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.List;

/**
 * The dispatch stack is responsible for consuming a queue of jobs from the
 * iteration strategy and dispatching those jobs through a stack based control
 * flow to an appropriate invocation target. Conceptually the queue and
 * description of services enter the stack at the top, travel down to an
 * invocation layer at the bottom from which results, errors and completion
 * events rise back up to the top layer. Dispatch stack layers are stored as an
 * ordered list with index 0 being the top of the stack.
 * 
 * @author Tom Oinn
 * 
 */
public interface DispatchStack {

	/**
	 * The DispatchStack consists of an ordered list of DispatchLayer instances
	 * where the DispatchLayer at index zero is at the bottom of the stack and
	 * is almost always an invocation layer of some kind (in any working
	 * dispatch stack configuration)
	 * 
	 */
	public List<DispatchLayer> getLayers();

	/**
	 * Return the layer above (lower index!) the specified layer, or a reference
	 * to the internal top layer dispatch layer if there is no layer above the
	 * specified one. Remember - input data and services go down, results,
	 * errors and completion events bubble back up the dispatch stack.
	 * <p>
	 * The top layer within the dispatch stack is always invisible and is held
	 * within the DispatchStackImpl object itself, being used to route data out
	 * of the entire stack
	 * 
	 * @param layer
	 * @return
	 */
	public DispatchLayer layerAbove(DispatchLayer layer);

	/**
	 * Return the layer below (higher index) the specified layer, or null if
	 * there is no layer below this one.
	 * 
	 * @param layer
	 * @return
	 */
	public DispatchLayer layerBelow(DispatchLayer layer);

}