package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.invocation.Event;

/**
 * Input port capable of receiving and reacing to workflow events.
 * 
 * @author Tom Oinn
 * 
 */
public interface EventHandlingInputPort extends InputPort {

	/**
	 * Receive an arbitrary workflow event.
	 */
	public void receiveEvent(Event e);

	/**
	 * If this port is connected to a Datalink return the link, otherwise return
	 * null
	 */
	public Datalink getIncomingLink();

}
