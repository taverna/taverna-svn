package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.annotation.impl.AbstractMutableAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

/**
 * Naive bean implementation of Datalink
 * 
 * @author Tom Oinn
 * 
 */
public class DatalinkImpl extends AbstractMutableAnnotatedThing implements
		Datalink {

	private EventForwardingOutputPort source;

	private EventHandlingInputPort sink;

	protected DatalinkImpl(EventForwardingOutputPort source,
			EventHandlingInputPort sink) {
		this.source = source;
		this.sink = sink;
	}

	public EventHandlingInputPort getSink() {
		return sink;
	}

	public EventForwardingOutputPort getSource() {
		return source;
	}

}
