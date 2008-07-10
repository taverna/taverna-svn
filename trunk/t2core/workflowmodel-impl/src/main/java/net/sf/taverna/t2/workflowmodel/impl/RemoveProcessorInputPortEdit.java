package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;

public class RemoveProcessorInputPortEdit implements Edit<Processor> {
	
	private final ProcessorImpl processor;
	public boolean isApplied=false;
	private final ProcessorInputPort port;

	public RemoveProcessorInputPortEdit(Processor processor,ProcessorInputPort port) {
		this.processor = (ProcessorImpl)processor;
		this.port = port;
		
	}

	public Processor doEdit() throws EditException {
		return processor;
	}

	public Object getSubject() {
		return processor;
	}

	public boolean isApplied() {
		return isApplied;
	}

	public void undo() {
		
		isApplied=false;
	}

}
