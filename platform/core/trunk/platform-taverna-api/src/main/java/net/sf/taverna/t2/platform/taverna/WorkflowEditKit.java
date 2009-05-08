package net.sf.taverna.t2.platform.taverna;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Methods to construct and manipulate workflow models
 * 
 * @author Tom Oinn
 */
public interface WorkflowEditKit {

	/**
	 * The Edits interface is the full editing functionality for all elements of
	 * Taverna 2 workflows. Use this if the simple edits in this higher level
	 * interface don't provide the level of detail you need.
	 */
	Edits getEdits();

	/**
	 * Build and return a prototypical Processor with a Taverna 1 style dispatch
	 * stack, default iteration strategy and a single Activity as supplied. This
	 * is the default behaviour when dragging a new service into a workflow in
	 * the workbench, although this method does not add the new procesor to a
	 * dataflow, you need to use methods in the Edits object to do that.
	 * 
	 * @param activity
	 *            the Activity to build this Processor around
	 * @param name
	 *            a name for the new processor
	 * @return a new Processor with the specified name
	 */
	Processor createDefaultProcessor(Activity<?> activity, String name)
			throws EditException;

	/**
	 * Connect an output to an input (in the sense that the dataflow sees is, so
	 * a dataflow input is an output as data flows out of it into the
	 * dataflow!). This uses a simple short naming notation where processor
	 * ports are denoted by 'processorname.portname' and workflow input and
	 * output ports by names without a '.' character.
	 */
	void connect(Dataflow workflow, String outputName, String inputName)
			throws EditException;

}
