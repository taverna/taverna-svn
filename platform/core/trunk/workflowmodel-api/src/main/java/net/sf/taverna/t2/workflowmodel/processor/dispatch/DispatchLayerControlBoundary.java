package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.AbstractDispatchEvent;

/**
 * Defines the methods used when a dispatch layer is creating a new control
 * boundary and wants to handle this implicitly rather than explicitly within
 * the layer code. In general this applies to any event which may send multiple
 * events down into the stack cause by the reception of a single event, in such
 * cases the process identifier or iteration index (or both) must be at least
 * partially re-written to prevent tokens with duplicate identifiers.
 * 
 * @author Tom Oinn
 * 
 */
public interface DispatchLayerControlBoundary {

	/**
	 * When an token stream forks the outgoing tokens must have a process
	 * identifier that is the identifier of the token that gave rise to the fork
	 * plus a fork identifier of some kind. You must implement this method such
	 * that it returns identifiers which are unique in the scope of the process
	 * identifier of the supplied dispatch event, the returned string will be
	 * pushed onto the owning process identifier of outgoing tokens.
	 * <p>
	 * There is no corresponding method to handle incoming tokens as the
	 * behaviour is always to pop the last item off the process identifier.
	 * 
	 * @param event
	 * @return
	 */
	public String getOutgoingProcessSuffix(AbstractDispatchEvent<?> event);

	/**
	 * Transform the index array of the outgoing token, this is used in cases
	 * where the index array is pushed onto the process identifier and then set
	 * to the empty index
	 * 
	 * @param event
	 * @return
	 */
	public int[] transformOutgoingIndex(AbstractDispatchEvent<?> event);

	/**
	 * Implement the inverse of the previous method here - it is absolutely
	 * essential that these operations are, taken as a pair, the identity
	 * function.
	 * 
	 * @param event
	 * @return
	 */
	public int[] transformIncomingIndex(AbstractDispatchEvent<?> event);

}
