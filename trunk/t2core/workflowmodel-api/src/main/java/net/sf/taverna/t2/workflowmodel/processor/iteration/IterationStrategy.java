package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.Map;

public interface IterationStrategy {

	/**
	 * The iteration strategy results in a set of job objects with a particular
	 * job index. This method returns the length of that index array when the
	 * specified input types are used. Input types are defined in terms of name
	 * and integer pairs where the name is the name of a NamedInputPortNode in
	 * the iteration strategy and the integer is the depth of the input data
	 * collection (i.e. item depth + index array length for that item which
	 * should be a constant).
	 * 
	 * @param inputDepths
	 *            map of port names to input collection depth
	 * @return the length of the index array which will be generated for each
	 *         resultant job object.
	 */
	public int getIterationDepth(Map<String, Integer> inputDepths)
			throws IterationTypeMismatchException;

	/**
	 * Return a map of port name -> desired cardinality for this iteration
	 * strategy
	 */
	public Map<String, Integer> getDesiredCardinalities();

	public TerminalNode getTerminalNode();

}
