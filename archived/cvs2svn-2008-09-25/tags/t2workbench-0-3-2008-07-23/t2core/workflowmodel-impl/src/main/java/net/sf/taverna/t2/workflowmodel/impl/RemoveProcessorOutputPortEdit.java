package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;

import org.apache.log4j.Logger;

public class RemoveProcessorOutputPortEdit extends AbstractProcessorEdit {

	private final ProcessorOutputPort port;
	private static Logger logger = Logger
			.getLogger(RemoveProcessorOutputPortEdit.class);

	public RemoveProcessorOutputPortEdit(Processor processor, ProcessorOutputPort port) {
		super(processor);
		this.port = port;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		if (processor.getOutputPortWithName(port.getName())==null) throw new EditException("The processor doesn't have a port named:"+port.getName());
		processor.outputPorts.remove(port);
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		try {
			new EditsImpl().getAddProcessorOutputPortEdit(processor, port).doEdit();
		} catch (EditException e) {
			logger.error("There was an error adding an input port to a Processor whilst undoing a remove");
		}
	}

}
