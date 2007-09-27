package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Add a new WorkflowAnnotation to an instance of MutableAnnotated
 * 
 * @param <TargetType> The Annotated type that the annotation will be applied to.
 * @author Tom
 *
 */
public final class AddAnnotationEdit<TargetType extends Annotated> extends AbstractAnnotationEdit<TargetType> {

	private WorkflowAnnotation annotation;
	
	public AddAnnotationEdit(TargetType target, WorkflowAnnotation newAnnotation) {
		super(target);
		this.annotation = newAnnotation;
	}
	
	@Override
	protected void doEditAction(MutableAnnotated subject) throws EditException {
		subject.addAnnotation(annotation);
	}

	@Override
	protected void undoEditAction(MutableAnnotated subject) {
		subject.removeAnnotation(annotation);
	}

}
