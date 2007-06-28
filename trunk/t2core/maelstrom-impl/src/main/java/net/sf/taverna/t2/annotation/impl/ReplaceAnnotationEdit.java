package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.EditException;

public final class ReplaceAnnotationEdit<TargetType extends Annotated> extends AbstractAnnotationEdit<TargetType> {

private WorkflowAnnotation oldAnnotation;
private WorkflowAnnotation newAnnotation;
	
	public ReplaceAnnotationEdit(TargetType target, WorkflowAnnotation oldAnnotation, WorkflowAnnotation newAnnotation) {
		super(target);
		this.oldAnnotation = oldAnnotation;
		this.newAnnotation = newAnnotation;
	}
	
	@Override
	protected void doEditAction(MutableAnnotated subject) throws EditException {
		subject.removeAnnotation(oldAnnotation);
		subject.addAnnotation(newAnnotation);
	}

	@Override
	protected void undoEditAction(MutableAnnotated subject) {
		subject.removeAnnotation(newAnnotation);
		subject.addAnnotation(oldAnnotation);
	}

}
