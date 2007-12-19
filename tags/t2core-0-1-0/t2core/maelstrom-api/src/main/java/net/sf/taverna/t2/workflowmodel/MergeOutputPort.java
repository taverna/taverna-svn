package net.sf.taverna.t2.workflowmodel;

/**
 * An EventForwardingOutputPort that is associated with Merge instances.
 * In particular it provides access to the Merge instance it is associated with.
 * 
 * @see Merge
 * @see EventForwardingOutputPort
 * 
 * @author Stuart Owen
 *
 */
public interface MergeOutputPort extends EventForwardingOutputPort {

	/**
	 * @return the Merge instance the port is associated with. 
	 */
	Merge getMerge();
}
