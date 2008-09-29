package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

/**
 * Naive bean implementation of Datalink
 * 
 * @author Tom Oinn
 * 
 */
public class DatalinkImpl extends AbstractAnnotatedThing<Datalink> implements
		Datalink {

	private EventForwardingOutputPort source;

	private EventHandlingInputPort sink;

	private transient int resolvedDepth = -1;
	
	public int getResolvedDepth() {
		return this.resolvedDepth;
	}
	
	protected void setResolvedDepth(int newResolvedDepth) {
		this.resolvedDepth = newResolvedDepth;
	}
	
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
	
	@Override
	public String toString() {
		return "link("+resolvedDepth+")"+source.getName()+":"+sink.getName();
	}

}
