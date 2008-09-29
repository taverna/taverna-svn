package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;

/**
 * Port definition with depth and name
 * 
 * @author Tom Oinn
 *
 */
public abstract class AbstractPort extends AbstractAnnotatedThing<Port> implements Port {

	protected String name;
	protected int depth;
	
	protected AbstractPort(String name, int depth) {
		this.name = name;
		this.depth = depth;
	}
	
	public int getDepth() {
		return this.depth;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getName() + " (" + getDepth() + ")";
	}
	
}
