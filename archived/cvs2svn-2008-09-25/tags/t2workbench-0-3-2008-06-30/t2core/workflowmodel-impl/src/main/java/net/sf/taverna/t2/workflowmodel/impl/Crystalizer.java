package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.IterationInternalEvent;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

/**
 * Recieves Job and Completion events and emits Jobs unaltered. Completion
 * events additionally cause registration of lists for each key in the datamap
 * of the jobs at immediate child locations in the index structure. These list
 * identifiers are sent in place of the Completion events.
 * <p>
 * State for a given process ID is purged when a final completion event is
 * received so there is no need for an explicit cache purge operation in the
 * public API (although for termination of partially complete workflows it may
 * be sensible for subclasses to provide one)
 * <p>
 * 
 * @author Tom Oinn
 */
public interface Crystalizer {

	/**
	 * Receive a Job or Completion, Jobs are emitted unaltered and cached,
	 * Completion events trigger registration of a corresponding list - this may
	 * be recursive in nature if the completion event's index implies nested
	 * lists which have not been registered.
	 */
	public void receiveEvent(IterationInternalEvent<? extends IterationInternalEvent<?>> event);

	/**
	 * This method is called when a new Job has been handled by the
	 * AbstractCrystalizer, either by direct passthrough or by list
	 * registration.
	 * 
	 */
	public void jobCreated(Job outputJob);

	/**
	 * Called whenever a completion not corresponding to a node in the cache is
	 * generated. In many cases this is an indication of an error state, the
	 * processor implementation should ensure that completion events are only
	 * sent to the crystalizer if there has been at least one data event with a
	 * lower depth on the same path.
	 */
	public void completionCreated(Completion completion);

}