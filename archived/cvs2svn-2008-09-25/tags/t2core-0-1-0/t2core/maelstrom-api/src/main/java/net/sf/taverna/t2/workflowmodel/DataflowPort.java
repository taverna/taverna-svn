package net.sf.taverna.t2.workflowmodel;

/**
 * Defines that the implementing port belongs to a Dataflow
 * 
 * @author Tom Oinn
 * 
 */
public interface DataflowPort {

	/**
	 * Get the parent DataFlow to which this port belongs
	 */
	public Dataflow getDataflow();

}
