package net.sf.taverna.t2.workflowmodel.processor.activity.impl;

import java.util.Set;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * An input port on an Activity instance. Simply used as a bean to hold port
 * name and depth properties.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 * 
 */
public class ActivityInputPortImpl extends AbstractPort implements InputPort {

	/**
	 * Constructs an Activity input port instance with the provided name and
	 * depth.
	 * 
	 * @param portName
	 * @param portDepth
	 */
	public ActivityInputPortImpl(String portName, int portDepth) {
		super(portName, portDepth);
	}

	/**
	 * Constructs an Activity input port with the provided name and depth,
	 * together with a list of predetermined annotations.
	 * 
	 * @param portName
	 * @param portDepth
	 * @param annotations
	 */
	public ActivityInputPortImpl(String portName, int portDepth,
			Set<WorkflowAnnotation> annotations) {
		this(portName, portDepth);
		for (WorkflowAnnotation annotation : annotations) {
			try {
				getAddAnnotationEdit(annotation).doEdit();
			} catch (EditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
