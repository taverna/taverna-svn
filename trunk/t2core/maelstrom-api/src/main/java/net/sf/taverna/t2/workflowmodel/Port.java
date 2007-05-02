package net.sf.taverna.t2.workflowmodel;

/**
 * Named port which receives events from some other entity and handles them
 * appropriately.
 * 
 * @author Tom Oinn
 * 
 */
public interface Port {

	public String getName();

	public int getDepth();

}
