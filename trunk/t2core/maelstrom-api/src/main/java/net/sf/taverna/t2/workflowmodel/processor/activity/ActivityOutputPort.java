package net.sf.taverna.t2.workflowmodel.processor.activity;

import net.sf.taverna.t2.workflowmodel.AbstractOutputPort;

/**
 * An output port on a Service instance, used as a bean to hold port name, depth
 * and granular depth properties.
 * 
 * @author Tom Oinn
 * 
 */
public class ActivityOutputPort extends AbstractOutputPort {

	public ActivityOutputPort(String portName, int portDepth, int granularDepth) {
		super(portName, portDepth, granularDepth);
	}

}
