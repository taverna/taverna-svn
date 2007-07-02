package net.sf.taverna.t2.workflowmodel;

/**
 * Input port on a Processor
 * 
 * @author Tom Oinn
 *
 */
public interface ProcessorInputPort extends FilteringInputPort {

	/**
	 * Get the Processor to which this port belongs
	 */
	public Processor getProcessor();
	
}
