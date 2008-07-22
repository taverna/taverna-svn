package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Add a new output port to the specified ProcessorImpl
 * 
 * @author Tom Oinn
 * 
 */
public class AddProcessorOutputPortEdit extends AbstractProcessorEdit {

	private final ProcessorOutputPortImpl port;

	public AddProcessorOutputPortEdit(Processor processor, OutputPort port) {
		super(processor);
		this.port = (ProcessorOutputPortImpl)port;
		
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		if (processor.getOutputPortWithName(port.getName()) != null) {
			throw new EditException("Duplicate output port name");
		}
		
		processor.outputPorts.add(port);
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		BasicEventForwardingOutputPort pop = processor.getOutputPortWithName(port.getName());
		if (pop != null) {
			processor.outputPorts.remove(pop);
		}

	}

}
