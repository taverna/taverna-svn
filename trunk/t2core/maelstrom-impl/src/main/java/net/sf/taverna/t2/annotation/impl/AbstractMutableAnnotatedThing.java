package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.AbstractAnnotatedThing;

/**
 * Extension of AbstractAnnotatedThing with the implementation interface
 * allowing removal and addition of annotation
 * 
 * @author Tom Oinn
 * 
 */
public class AbstractMutableAnnotatedThing extends AbstractAnnotatedThing
		implements MutableAnnotated {

	public void addAnnotation(WorkflowAnnotation newAnnotation) {
		this.annotations.add(newAnnotation);
	}

	public void removeAnnotation(WorkflowAnnotation annotationToRemove) {
		this.annotations.remove(annotationToRemove);
	}

}
