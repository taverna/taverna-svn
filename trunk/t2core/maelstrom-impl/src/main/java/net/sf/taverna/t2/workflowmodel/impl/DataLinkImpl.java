package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.DataLink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

/**
 * Naive bean implementation of DataLink
 * 
 * @author Tom Oinn
 * 
 */
public class DataLinkImpl implements DataLink {

	private EventForwardingOutputPort source;

	private EventHandlingInputPort sink;

	protected DataLinkImpl(EventForwardingOutputPort source,
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
