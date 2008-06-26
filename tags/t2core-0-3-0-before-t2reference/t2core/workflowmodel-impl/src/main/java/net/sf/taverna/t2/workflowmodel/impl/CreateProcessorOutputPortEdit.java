package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Add a new output port to the specified ProcessorImpl
 * 
 * @author Tom Oinn
 * 
 */
public class CreateProcessorOutputPortEdit extends AbstractProcessorEdit {

	private int portDepth;

	private int portGranularity;

	private String portName;

	public CreateProcessorOutputPortEdit(Processor processor, String portName,
			int portDepth, int portGranularity) {
		super(processor);
		this.portName = portName;
		this.portDepth = portDepth;
		this.portGranularity = portGranularity;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		if (processor.getOutputPortWithName(portName) != null) {
			throw new EditException("Duplicate output port name");
		}
		ProcessorOutputPortImpl pop = new ProcessorOutputPortImpl(processor,portName,
				portDepth, portGranularity);
		processor.outputPorts.add(pop);
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		BasicEventForwardingOutputPort pop = processor.getOutputPortWithName(portName);
		if (pop != null) {
			processor.outputPorts.remove(pop);
		}

	}

}
