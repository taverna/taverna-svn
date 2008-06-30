package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.annotation.Annotated;

/**
 * Named port which receives events from some other entity and handles them
 * appropriately.
 * 
 * @author Tom Oinn
 * 
 */
public interface Port extends Annotated<Port> {

	public String getName();

	public int getDepth();

}
