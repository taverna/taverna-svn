package net.sf.taverna.t2.workflowmodel.processor.activity.impl;

import java.util.Set;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.AbstractOutputPort;

/**
 * An output port on a Service instance, used as a bean to hold port name, depth
 * and granular depth properties.
 * 
 * @author Tom Oinn
 * 
 */
public class ActivityOutputPortImpl extends AbstractOutputPort {

	public ActivityOutputPortImpl(String portName, int portDepth, int granularDepth) {
		super(portName, portDepth, granularDepth);
	}
	
	public ActivityOutputPortImpl(String portName,int portDepth,int granularDepth,Set<WorkflowAnnotation> annotations) {
		this(portName,portDepth,granularDepth);
		this.annotations.addAll(annotations);
	}

}
