package net.sf.taverna.t2.workflowmodel;

import java.util.List;

/**
 * Contains a validation report from a dataflow validation check. Processors are
 * classified as failed, unsatisfied or valid depending on whether they directly
 * fail type validation, cannot be checked due to unsatisfied incoming links or
 * pass respectively.
 * 
 * @author Tom Oinn
 * 
 */
public interface DataflowValidationReport {

	/**
	 * Overal validity - if the workflow is valid it can be run, otherwise there
	 * are problems somewhere and a facade can't be created from it.
	 * 
	 * @return whether the workflow is valid (true) or not (false)
	 */
	public boolean isValid();

	/**
	 * The workflow will be marked as invalid if there are processors with
	 * unlinked input ports or where there are cycles causing the type checking
	 * algorithm to give up. In these cases offending processors or any
	 * ancestors that are affected as a knock on effect will be returned in this
	 * list.
	 * 
	 * @return list of Processor instances within the Dataflow for which it is
	 *         impossible to determine validity due to missing inputs or cyclic
	 *         dependencies
	 */
	public List<? extends Processor> getUnsatisfiedProcessors();

	/**
	 * The workflow will be marked as invalid if any processor fails to type
	 * check.
	 * 
	 * @return list of Processor instances within the Dataflow which caused
	 *         explicit type check failures
	 */
	public List<? extends Processor> getFailedProcessors();

	/**
	 * The workflow will be marked as invalid if any of the dataflow output
	 * ports can't be typed based on incoming links. This happens if the port
	 * isn't linked (a common enough issue for new users in previous releases of
	 * Taverna) or if the internal port is linked but the entity it links to
	 * isn't validated.
	 * 
	 * @return a list of DataflowOutputPort implementations which are not typed
	 *         correctly. These will have output depth of -1 indicating an
	 *         unknown depth, they may or may not have a granular depth set but
	 *         if the overall depth is -1 this isn't important as the thing
	 *         won't run anyway.
	 */
	public List<? extends DataflowOutputPort> getUnresolvedOutputs();

}
