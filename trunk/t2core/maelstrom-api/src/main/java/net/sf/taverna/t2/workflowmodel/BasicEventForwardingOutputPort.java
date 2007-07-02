package net.sf.taverna.t2.workflowmodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.invocation.Event;

/**
 * Extension of AbstractOutputPort implementing EventForwardingOutputPort
 * 
 * @author Tom Oinn
 * 
 */
public class BasicEventForwardingOutputPort extends AbstractOutputPort
		implements EventForwardingOutputPort {

	protected Set<Datalink> outgoingLinks;

	/**
	 * Construct a new abstract output port with event forwarding capability
	 * 
	 * @param portName
	 * @param portDepth
	 * @param granularDepth
	 */
	public BasicEventForwardingOutputPort(String portName, int portDepth,
			int granularDepth) {
		super(portName, portDepth, granularDepth);
		this.outgoingLinks = new HashSet<Datalink>();
	}

	/**
	 * Implements EventForwardingOutputPort
	 */
	public final Set<Datalink> getOutgoingLinks() {
		return Collections.unmodifiableSet(this.outgoingLinks);
	}

	/**
	 * Forward the specified event to all targets
	 * 
	 * @param e
	 */
	public void sendEvent(Event e) {
		for (Datalink link : outgoingLinks) {
			link.getSink().receiveEvent(e);
		}
	}

}
