package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.EditException;

public final class RemoveAnnotationEdit<TargetType extends Annotated> extends AbstractAnnotationEdit<TargetType> {

private WorkflowAnnotation annotation;
	
	public RemoveAnnotationEdit(TargetType target, WorkflowAnnotation newAnnotation) {
		super(target);
		this.annotation = newAnnotation;
	}
	
	@Override
	protected void doEditAction(MutableAnnotated subject) throws EditException {
		subject.removeAnnotation(annotation);
	}

	@Override
	protected void undoEditAction(MutableAnnotated subject) {
		subject.addAnnotation(annotation);
	}

}
