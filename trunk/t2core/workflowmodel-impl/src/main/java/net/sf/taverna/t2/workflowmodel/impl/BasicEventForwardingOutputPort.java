package net.sf.taverna.t2.workflowmodel.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.AbstractOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;

/**
 * Extension of AbstractOutputPort implementing EventForwardingOutputPort
 * 
 * @author Tom Oinn
 * 
 */
public class BasicEventForwardingOutputPort extends AbstractOutputPort
		implements EventForwardingOutputPort {

	protected Set<DatalinkImpl> outgoingLinks;

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
		this.outgoingLinks = new HashSet<DatalinkImpl>();
	}

	/**
	 * Implements EventForwardingOutputPort
	 */
	public final Set<? extends Datalink> getOutgoingLinks() {
		return Collections.unmodifiableSet(this.outgoingLinks);
	}

	/**
	 * Forward the specified event to all targets
	 * 
	 * @param e
	 */
	public void sendEvent(WorkflowDataToken e) {
		for (Datalink link : outgoingLinks) {
			link.getSink().receiveEvent(e);
		}
	}

	protected void addOutgoingLink(DatalinkImpl link) {
		if (outgoingLinks.contains(link) == false) {
			outgoingLinks.add(link);
		}
	}

	protected void removeOutgoingLink(Datalink link) {
		outgoingLinks.remove(link);
	}

	void setDepth(int depth) {
		this.depth = depth;
	}
	
	void setGranularDepth(int granularDepth) {
		this.granularDepth = granularDepth;
	}

}
