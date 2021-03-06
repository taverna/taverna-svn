package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationAssertionImpl;
import net.sf.taverna.t2.annotation.AnnotationSourceSPI;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

@SuppressWarnings("unchecked")
public class AddAnnotationSourceEdit implements Edit<AnnotationAssertion> {

	private AnnotationAssertion annotationAssertion;
	private AnnotationSourceSPI annotationSource;
	private boolean applied;

	@SuppressWarnings("unchecked")
	public AddAnnotationSourceEdit(AnnotationAssertion annotationAssertion,
			AnnotationSourceSPI annotationSource) {
		this.annotationAssertion = annotationAssertion;
		this.annotationSource = annotationSource;
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
						.setAnnotationSource(annotationSource);
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
		((AnnotationAssertionImpl) annotationAssertion)
				.removeAnnotationSource();
		applied = false;
	}

}
