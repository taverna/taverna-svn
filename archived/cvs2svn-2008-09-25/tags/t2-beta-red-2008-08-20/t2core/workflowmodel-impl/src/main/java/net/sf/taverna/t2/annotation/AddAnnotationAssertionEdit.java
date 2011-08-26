package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

public class AddAnnotationAssertionEdit implements Edit<AnnotationChain> {

	private boolean applied;

	@SuppressWarnings("unchecked")
	private AnnotationAssertion annotationAssertion;
	private AnnotationChain annotationChain;

	@SuppressWarnings("unchecked")
	public AddAnnotationAssertionEdit(AnnotationChain annotationChain,
			AnnotationAssertion annotationAssertion) {
		this.annotationChain = annotationChain;
		this.annotationAssertion = annotationAssertion;
	}

	public AnnotationChain doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied");
		}
		if (!(annotationChain instanceof AnnotationChainImpl)) {
			throw new EditException(
					"Object being edited must be instance of AnnotationChainImpl");
		}

		try {
			synchronized (annotationChain) {
				((AnnotationChainImpl) annotationChain)
						.addAnnotationAssertion(annotationAssertion);
				applied = true;
				return this.annotationChain;
			}
		} catch (Exception e) {
			applied = false;
			throw new EditException("There was a problem with the edit", e);
		}

	}

	public Object getSubject() {
		return annotationChain;
	}

	public boolean isApplied() {
		return applied;
	}

	public void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		((AnnotationChainImpl)annotationChain).removeAnnotationAssertion(annotationAssertion);
		applied = false;
	}

}
