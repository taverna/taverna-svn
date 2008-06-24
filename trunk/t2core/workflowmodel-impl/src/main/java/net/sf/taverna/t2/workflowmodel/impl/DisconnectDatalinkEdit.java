package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

/**
 * Disconnect a datalink from its source and sink.
 * 
 * @author David Withers
 * 
 */
public class DisconnectDatalinkEdit extends AbstractDatalinkEdit {

	public DisconnectDatalinkEdit(Datalink datalink) {
		super(datalink);
	}

	@Override
	protected void doEditAction(DatalinkImpl datalink) throws EditException {
		EventForwardingOutputPort source = datalink.getSource();
		EventHandlingInputPort sink = datalink.getSink();
		if (source instanceof BasicEventForwardingOutputPort) {
			((BasicEventForwardingOutputPort) source).removeOutgoingLink(datalink);
		}
		if (sink instanceof AbstractEventHandlingInputPort) {
			((AbstractEventHandlingInputPort) sink).setIncomingLink(null);
		}
	}

	@Override
	protected void undoEditAction(DatalinkImpl datalink) {
		EventForwardingOutputPort source = datalink.getSource();
		EventHandlingInputPort sink = datalink.getSink();
		if (source instanceof BasicEventForwardingOutputPort) {
			((BasicEventForwardingOutputPort) source).addOutgoingLink(datalink);
		}
		if (sink instanceof AbstractEventHandlingInputPort) {
			((AbstractEventHandlingInputPort) sink).setIncomingLink(datalink);
		}
	}

}
