package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.List;
import java.util.Map;

/**
 * Stack of iteration strategy containers. The stacking behaviour allows for
 * staged implicit iteration where intermediate strategies are used to drill
 * into the collection structure to a certain depth with a final one used to
 * render job objects containing data at the correct depth for the process. This
 * was achieved in Taverna 1 through the combination of nested workflows and
 * 'forcing' processors which could echo and therefore force input types of the
 * workflow to a particular cardinality.
 * 
 * @author Tom Oinn
 * 
 */
public interface IterationStrategyStack {

	/**
	 * The iteration strategy stack consists of an ordered list of iteration
	 * strategies.
	 * 
	 * @return An unmodifiable copy of the list containing the iteration
	 *         strategy objects in order, with the strategy at position 0 in the
	 *         list being the one to which data is fed first.
	 */
	public List<? extends IterationStrategy> getStrategies();

	/**
	 * Calculate the depth of the iteration strategy stack as a whole given a
	 * set of named inputs and their cardinalities. This depth is the length of
	 * the index array which will be added to any output data, so the resultant
	 * output of each port in the owning processor is the depth of that port as
	 * defined by the activity plus this value.
	 * 
	 * @param inputDepths
	 * @return
	 * @throws IterationTypeMismatchException
	 * @throws MissingIterationInputException
	 */
	public int getIterationDepth(Map<String, Integer> inputDepths)
			throws IterationTypeMismatchException,
			MissingIterationInputException;
	

}
