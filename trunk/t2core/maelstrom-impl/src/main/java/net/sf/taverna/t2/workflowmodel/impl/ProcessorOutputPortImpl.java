package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;

/**
 * Extension of AbstractOutputPort for use as the output port on a
 * ProcessorImpl. Contains additional logic to relay workflow data tokens from
 * the internal crystalizer to each in a set of target FilteringInputPort
 * instances.
 * 
 * @author Tom Oinn
 * 
 */
public class ProcessorOutputPortImpl extends BasicEventForwardingOutputPort implements ProcessorOutputPort{

	private ProcessorImpl parent = null;
	
	protected ProcessorOutputPortImpl(String portName, int portDepth,
			int granularDepth, ProcessorImpl parent) {
		super(portName, portDepth, granularDepth);
		this.parent = parent;
	}

	/**
	 * Strip off the last id in the owning process stack (as this will have been
	 * pushed onto the stack on entry to the processor) and relay the event to
	 * the targets.
	 * 
	 */
	protected void receiveEvent(WorkflowDataToken token) {
		String currentOwner = token.getOwningProcess();
		String newOwner = currentOwner.substring(0, currentOwner
				.lastIndexOf(':'));
		sendEvent(new WorkflowDataToken(newOwner, token
					.getIndex(), token.getData()));
	}
	
	public Processor getProcessor() {
		return this.parent;
	}
	
}
