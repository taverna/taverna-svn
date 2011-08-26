package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.NamingException;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Removes a processor from a dataflow.
 * 
 * @author David Withers
 */
public class RemoveProcessorEdit extends AbstractDataflowEdit{
	
	private Processor processor;
	
	public RemoveProcessorEdit(Dataflow dataflow, Processor processor) {
		super(dataflow);
		this.processor = processor;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.removeProcessor(processor);
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		if (processor instanceof ProcessorImpl) {
			try {
				dataflow.addProcessor((ProcessorImpl) processor);
			} catch (NamingException e) {
				//a processor with this name has already been removed
			}
		}
	}
	
}
