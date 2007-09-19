package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.Set;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;
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
	
	public ActivityInputPort(String portName,int portDepth,Set<WorkflowAnnotation> annotations) {
		this(portName,portDepth);
		this.annotations.addAll(annotations);
	}

}
