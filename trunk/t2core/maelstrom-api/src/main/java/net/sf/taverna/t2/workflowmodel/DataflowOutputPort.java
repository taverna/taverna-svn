package net.sf.taverna.t2.workflowmodel;

/**
 * Output port of a DataFlow, exposes an internal EventHandlingInputPort into
 * which the internal workflow logic pushes data to be exposed outside the
 * workflow boundary.
 * 
 * @author Tom Oinn
 * 
 */
public interface DataflowOutputPort extends EventForwardingOutputPort {

	/**
	 * Get the internal input port for this workflow output
	 * 
	 * @return port into which the workflow can push data for this output
	 */
	public EventHandlingInputPort getInternalInputPort();

	/**
	 * Get the parent DataFlow to which this port belongs
	 */
	public Dataflow getDataflow();
	
}
