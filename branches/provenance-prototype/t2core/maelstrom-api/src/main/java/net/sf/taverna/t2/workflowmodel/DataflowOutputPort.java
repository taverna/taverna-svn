package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.facade.ResultListener;

/**
 * Output port of a DataFlow, exposes an internal EventHandlingInputPort into
 * which the internal workflow logic pushes data to be exposed outside the
 * workflow boundary.
 * 
 * @author Tom Oinn
 * 
 */
public interface DataflowOutputPort extends EventForwardingOutputPort, DataflowPort {

	/**
	 * Get the internal input port for this workflow output
	 * 
	 * @return port into which the workflow can push data for this output
	 */
	public EventHandlingInputPort getInternalInputPort();
	
	/**
	 * Add a ResultListener, capable of listening to results being received by the output port
	 * @param listener the ResultListener
	 * 
	 * @see ResultListener
	 */
	public void addResultListener(ResultListener listener);
	
	/**
	 * Remove a ResultListener
	 * @param listener the ResultListener
	 * 
	 * @see ResultListener
	 */
	public void removeResultListener(ResultListener listener);

}
