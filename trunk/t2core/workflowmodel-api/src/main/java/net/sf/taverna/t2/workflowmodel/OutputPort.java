package net.sf.taverna.t2.workflowmodel;

/**
 * Port representing the output of an activity, processor or workflow. In addition
 * to the name and port depth defined by the Port interface this includes a
 * granular depth property. The granular depth of an output is the depth of the
 * finest grained entity that can be emitted from that port. For example, if a
 * process conceptually returned a list of strings but was actually capable of
 * streaming strings as they were generated it would set a port depth of 1 and
 * granular depth of zero.
 * 
 * @author Tom Oinn
 * 
 */
public interface OutputPort extends Port {

	/**
	 * The granular depth is the depth of the finest grained item that can be
	 * emitted from this output port. A difference in this and the port depth
	 * indicates that the entity this port is attached to is capable of
	 * streaming data resulting from a single process invocation. The port depth
	 * defines the conceptual depth, so a process returning a stream of single
	 * items would set port depth to 1 and granular depth to zero.
	 * 
	 * @return granular depth of output port
	 */
	public int getGranularDepth();

}
