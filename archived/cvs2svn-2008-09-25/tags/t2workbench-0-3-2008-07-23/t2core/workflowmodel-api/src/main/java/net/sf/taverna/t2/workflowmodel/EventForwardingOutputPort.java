package net.sf.taverna.t2.workflowmodel;

import java.util.Set;

/**
 * An extension of OutputPort defining a set of target EventReceivingInputPorts
 * to which internally generated events will be relayed. This is the interface
 * used by output ports on a workflow entity with internal logic generating or
 * relaying events.
 * 
 * @author Tom Oinn
 * 
 */
public interface EventForwardingOutputPort extends OutputPort {

	/**
	 * The set of EventHandlingInputPort objects which act as targets for events
	 * produced from this OutputPort
	 * 
	
	public Set<EventHandlingInputPort> getTargets();
*/

	/**
	 * The set of datalinks for which this output port is the source of events
	 */
	public Set<? extends Datalink> getOutgoingLinks();
}
