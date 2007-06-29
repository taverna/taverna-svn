package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.DataLink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

/**
 * Extends AbstractPort with the getIncomingLinks method and an additional
 * implementation method to set the incoming data link
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractEventHandlingInputPort extends AbstractPort
		implements EventHandlingInputPort {

	private DataLink incomingLink = null;

	protected AbstractEventHandlingInputPort(String name, int depth) {
		super(name, depth);
	}

	public DataLink getIncomingLink() {
		return this.incomingLink;
	}

	protected void setIncomingLink(DataLink newLink) {
		this.incomingLink = newLink;
	}

}
