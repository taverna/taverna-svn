package net.sf.taverna.t2.workflowmodel.processor.dispatch.events;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType;

/**
 * Superclass of events within the dispatch stack
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractDispatchEvent<EventType extends AbstractDispatchEvent<EventType>>
		extends Event<EventType> {

	protected AbstractDispatchEvent(String owner, int[] index,
			InvocationContext context) {
		super(owner, index, context);
	}

	/**
	 * Return the DispatchMessageType for this event object
	 * 
	 * @return instance of DispatchMessageType represented by this event
	 */
	public abstract DispatchMessageType getMessageType();

}
