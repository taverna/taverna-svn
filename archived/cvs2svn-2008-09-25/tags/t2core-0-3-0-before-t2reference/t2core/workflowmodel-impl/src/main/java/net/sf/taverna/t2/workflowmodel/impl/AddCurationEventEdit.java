package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationAssertionImpl;
import net.sf.taverna.t2.annotation.AnnotationChainImpl;
import net.sf.taverna.t2.annotation.CurationEvent;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

public class AddCurationEventEdit implements Edit<AnnotationAssertion> {

	private AnnotationAssertion annotationAssertion;
	private CurationEvent curationEvent;
	private boolean applied;

	@SuppressWarnings("unchecked")
	public AddCurationEventEdit(AnnotationAssertion annotationAssertion,
			CurationEvent curationEvent) {
				this.annotationAssertion = annotationAssertion;
				this.curationEvent = curationEvent;
	}

	public AnnotationAssertion doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied");
		}
		if (!(annotationAssertion instanceof AnnotationAssertionImpl)) {
			throw new EditException(
					"Object being edited must be instance of AnnotationAssertionImpl");
		}

		try {
			synchronized (annotationAssertion) {
				((AnnotationAssertionImpl) annotationAssertion)
						.addCurationEvent(curationEvent);
				applied = true;
				return this.annotationAssertion;
			}
		} catch (Exception e) {
			applied = false;
			throw new EditException("There was a problem with the edit", e);
		}
	}

	public Object getSubject() {
		return annotationAssertion;
	}

	public boolean isApplied() {
		return applied;
	}

	public void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		((AnnotationAssertionImpl)annotationAssertion).removeCurationEvent(curationEvent);
		applied = false;
	}

}
