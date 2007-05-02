package net.sf.taverna.t2.workflowmodel;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.invocation.Event;

/**
 * Extension of AbstractOutputPort implementing EventForwardingOutputPort
 * 
 * @author Tom Oinn
 * 
 */
public class BasicEventForwardingOutputPort extends
		AbstractOutputPort implements EventForwardingOutputPort {

	/**
	 * A set of target EventHandlingInputPort instances
	 */
	protected Set<EventHandlingInputPort> targets;

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
		this.targets = new HashSet<EventHandlingInputPort>();
	}

	/**
	 * Implements EventForwardingOutputPort
	 */
	public final Set<EventHandlingInputPort> getTargets() {
		return this.targets;
	}

	/**
	 * Forward the specified event to all targets
	 * 
	 * @param e
	 */
	public void sendEvent(Event e) {
		for (EventHandlingInputPort target : targets) {
			target.receiveEvent(e);
		}
	}

}
