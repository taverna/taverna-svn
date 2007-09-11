package net.sf.taverna.t2.workflowmodel.processor.activity;

import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * An input port on a Service instance. Simply used as a bean to hold port name
 * and depth properties.
 * 
 * @author Tom Oinn
 * 
 */
public class ActivityInputPort extends AbstractPort implements InputPort {

	public ActivityInputPort(String portName, int portDepth) {
		super(portName, portDepth);
	}

}
