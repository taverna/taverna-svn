package net.sf.taverna.t2.workflowmodel.processor.service;

import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * An input port on a Service instance. Simply used as a bean to hold port name
 * and depth properties.
 * 
 * @author Tom Oinn
 * 
 */
public class ServiceInputPort extends AbstractPort implements InputPort {

	public ServiceInputPort(String portName, int portDepth) {
		super(portName, portDepth);
	}

}
