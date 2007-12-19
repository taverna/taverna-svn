package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

/**
 * Extends AbstractPort with the getIncomingLink method and an additional
 * implementation method to set the incoming data link
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractEventHandlingInputPort extends AbstractPort
		implements EventHandlingInputPort {

	private Datalink incomingLink = null;

	protected AbstractEventHandlingInputPort(String name, int depth) {
		super(name, depth);
	}

	public Datalink getIncomingLink() {
		return this.incomingLink;
	}

	protected void setIncomingLink(Datalink newLink) {
		this.incomingLink = newLink;
	}
}
