package net.sf.taverna.t2.workflowmodel;

/**
 * Port definition with depth and name
 * 
 * @author Tom Oinn
 *
 */
public abstract class AbstractPort extends AbstractAnnotatedThing implements Port {

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

}
