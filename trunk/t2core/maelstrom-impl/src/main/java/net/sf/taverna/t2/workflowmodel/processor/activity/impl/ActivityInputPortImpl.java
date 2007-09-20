package net.sf.taverna.t2.workflowmodel.processor.activity.impl;

import java.util.Set;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * An input port on an Activity instance. Simply used as a bean to hold port name
 * and depth properties.
 * 
 * @author Tom Oinn
 * 
 */
public class ActivityInputPortImpl extends AbstractPort implements InputPort {

	public ActivityInputPortImpl(String portName, int portDepth) {
		super(portName, portDepth);
	}
	
	public ActivityInputPortImpl(String portName,int portDepth,Set<WorkflowAnnotation> annotations) {
		this(portName,portDepth);
		this.annotations.addAll(annotations);
	}

}
