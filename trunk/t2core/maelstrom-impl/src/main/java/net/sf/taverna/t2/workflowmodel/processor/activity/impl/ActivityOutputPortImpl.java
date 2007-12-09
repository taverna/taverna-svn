package net.sf.taverna.t2.workflowmodel.processor.activity.impl;

import java.util.Set;

import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.workflowmodel.AbstractOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * An output port on an Activity instance, used as a bean to hold port name,
 * depth and granular depth properties.
 * 
 * @author Tom Oinn
 * 
 */
public class ActivityOutputPortImpl extends AbstractOutputPort {

	/**
	 * Constructs an Activity output port instance with the provided name,depth
	 * and granular depth.
	 * 
	 * @param portName
	 * @param portDepth
	 * @param granularDepth
	 */
	public ActivityOutputPortImpl(String portName, int portDepth,
			int granularDepth) {
		super(portName, portDepth, granularDepth);
	}

	/**
	 * Constructs an Activity input port with the provided name, depth and
	 * granularDepth together with a list of predetermined annotations.
	 * 
	 * @param portName
	 * @param portDepth
	 * @param granularDepth
	 * @param annotations
	 */
	public ActivityOutputPortImpl(String portName, int portDepth,
			int granularDepth, Set<AnnotationChain> annotations) {
		this(portName, portDepth, granularDepth);
		for (AnnotationChain newAnnotation : annotations) {
			try {
				getAddAnnotationEdit(newAnnotation).doEdit();
			} catch (EditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
