package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * An Edit class responsible for add a Processor to the dataflow.
 * 
 * @author Stuart Owen
 *
 */
public class AddProcessorEdit extends AbstractDataflowEdit{
	
	private Processor processor;
	
	protected AddProcessorEdit(Dataflow dataflow, Processor processor) {
		super(dataflow);
		this.processor=processor;
	}

	/**
	 * Adds the Processor instance to the Dataflow
	 * 
	 * @throws EditException if the edit has already taken place (without an intermediate undo) or a processor with that name already exists.
	 */
	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		if (processor instanceof ProcessorImpl) {
			dataflow.addProcessor((ProcessorImpl)processor);
		}
		else {
			throw new EditException("The Processor is of the wrong implmentation, it should be of type ProcessorImpl");
		}
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		dataflow.removeProcessor(processor);
	}
}
