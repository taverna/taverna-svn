package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

import org.apache.log4j.Logger;

public class RemoveProcessorInputPortEdit extends AbstractProcessorEdit {

	private final ProcessorInputPort port;
	private static Logger logger = Logger
			.getLogger(RemoveProcessorInputPortEdit.class);

	public RemoveProcessorInputPortEdit(Processor processor, ProcessorInputPort port) {
		super(processor);
		this.port = port;
	}
	
	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		if (processor.getInputPortWithName(port.getName())==null) throw new EditException("The processor doesn't have a port named:"+port.getName());
		for (IterationStrategyImpl is : processor.iterationStack.getStrategies()) {
			NamedInputPortNode nipn = new NamedInputPortNode(port.getName(),
					port.getDepth());
			is.removeInputByName(port.getName());
		}
		processor.inputPorts.remove(port);
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		try {
			new EditsImpl().getAddProcessorInputPortEdit(processor, port).doEdit();
		} catch (EditException e) {
			logger.error("There was an error adding an input port to a Processor whilst undoing a remove");
		}
	}
	
	

}
