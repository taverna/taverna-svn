package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.annotation.Annotated;

/**
 * A single point to point data link from an instance of
 * EventForwardingOutputPort to an instance of EventHandlingInputPort
 * 
 * @author Tom Oinn
 * 
 */
public interface Datalink extends Annotated<Datalink> {

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

	/**
	 * Each datalink has a resolved depth, this being the constant sum of index
	 * array length + item depth for all tokens exchanged along this link. Where
	 * no iteration or data streaming is occuring this will evaluate to the
	 * output port depth the link is from (as is always the case with the
	 * internal output ports in dataflow inputs)
	 * 
	 * @return
	 */
	public int getResolvedDepth();

}
