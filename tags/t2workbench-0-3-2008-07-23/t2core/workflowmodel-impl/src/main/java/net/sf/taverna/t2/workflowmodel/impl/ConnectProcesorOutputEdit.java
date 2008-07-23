package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Connect the named output port on a given processor to the specified
 * EventHandlingInputPort, updating the connected port list on the output port
 * of the processor such that events will be forwarded as appropriate. If the
 * target port is a FilteringInputPort then also set the filter level
 * appropriately.
 * 
 * @author Tom Oinn
 * 
 */
public class ConnectProcesorOutputEdit extends AbstractProcessorEdit {

	private EventHandlingInputPort target;

	private String outputName;

	private BasicEventForwardingOutputPort outputPort;
	
	private DatalinkImpl newLink = null;

	public ConnectProcesorOutputEdit(Processor p, String outputName,
			EventHandlingInputPort targetPort) {
		super(p);
		this.target = targetPort;
		this.outputName = outputName;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		for (BasicEventForwardingOutputPort popi : processor.outputPorts) {
			if (popi.getName().equals(outputName)) {
				newLink = new DatalinkImpl(popi, target);
				popi.addOutgoingLink(newLink);
				if (target instanceof AbstractEventHandlingInputPort) {
					((AbstractEventHandlingInputPort)target).setIncomingLink(newLink);
				}
				outputPort = popi;
				return;
			}
		}
		throw new EditException("Cannot locate output port with name '"
				+ outputName + "'");
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		outputPort.removeOutgoingLink(newLink);
		if (target instanceof AbstractEventHandlingInputPort) {
			((AbstractEventHandlingInputPort)target).setIncomingLink(null);
		}
	}

}
