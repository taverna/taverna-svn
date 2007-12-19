package net.sf.taverna.t2.workflowmodel;

/**
 * An input port on a Dataflow contains a nested output port within it. This
 * internal output port is used when connecting an edge from the workflow input
 * to a processor or workflow output (which in turn has a nested input port).
 * The workflow ports are therefore effectively pairs of ports with a relay
 * mechanism between the external and internal in the case of the dataflow
 * input.
 * 
 * @author Tom Oinn
 * 
 */
public interface DataflowInputPort extends EventHandlingInputPort, DataflowPort {

	/**
	 * Return the internal output port. Output ports have a granular depth
	 * property denoting the finest grained output token they can possibly
	 * produce, this is used to configure downstream filtering input ports. In
	 * this case the finest depth item is determined by the input to the
	 * workflow port and must be explicitly set.
	 * 
	 * @return the internal output port
	 */
	public EventForwardingOutputPort getInternalOutputPort();
	
	/**
	 * Define the finest grained item that will be sent to this input port. As
	 * all data are relayed through to the internal output port this is used to
	 * denote output port granularity as well as to configure any downstream
	 * connected filtering input ports.
	 */
	public int getGranularInputDepth();
	
	
}
