package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.invocation.WorkflowDataToken;

/**
 * Input port capable of receiving and reacting to workflow events.
 * 
 * @author Tom Oinn
 * 
 */
public interface EventHandlingInputPort extends InputPort {

	/**
	 * Receive an arbitrary workflow event.
	 */
	public void receiveEvent(WorkflowDataToken t);

	/**
	 * If this port is connected to a Datalink return the link, otherwise return
	 * null
	 */
	public Datalink getIncomingLink();

}
