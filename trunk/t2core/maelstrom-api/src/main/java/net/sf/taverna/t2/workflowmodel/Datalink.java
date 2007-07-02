package net.sf.taverna.t2.workflowmodel;

/**
 * A single point to point data link from an instance of
 * EventForwardingOutputPort to an instance of EventHandlingInputPort
 * 
 * @author Tom Oinn
 * 
 */
public interface Datalink {

	/**
	 * Get the sink for events flowing through this link
	 * 
	 * @return input port receiving events
	 */
	public EventHandlingInputPort getSink();

	/**
	 * Get the source for events flowing through this link
	 * 
	 * @return output port generating events
	 */
	public EventForwardingOutputPort getSource();

}
