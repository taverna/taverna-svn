package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.BasicEventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

/**
 * Extension of AbstractOutputPort for use as the output port on a
 * ProcessorImpl. Contains additional logic to relay workflow data tokens from
 * the internal crystalizer to each in a set of target FilteringInputPort
 * instances.
 * 
 * @author Tom Oinn
 * 
 */
public class ProcessorOutputPortImpl extends BasicEventForwardingOutputPort {

	protected ProcessorOutputPortImpl(String portName, int portDepth,
			int granularDepth) {
		super(portName, portDepth, granularDepth);
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
		for (EventHandlingInputPort target : targets) {
			target.receiveEvent(new WorkflowDataToken(newOwner, token
					.getIndex(), token.getData()));
		}
	}

}
