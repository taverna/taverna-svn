package net.sf.taverna.t2.workflowmodel;

/**
 * Simple implementation of OutputPort, extends AbstractPort and adds the
 * granular depth bean getter.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractOutputPort extends AbstractPort implements OutputPort {

	protected int granularDepth;

	protected AbstractOutputPort(String portName, int portDepth,
			int granularDepth) {
		super(portName, portDepth);
		this.granularDepth = granularDepth;
	}

	public int getGranularDepth() {
		return granularDepth;
	}

}
