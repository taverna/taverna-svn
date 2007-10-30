package net.sf.taverna.t2.annotation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Convenient abstract superclass for annotated things
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractAnnotatedThing<T> implements Annotated<T> {

	private Set<WorkflowAnnotation> annotations = new HashSet<WorkflowAnnotation>();

	public Set<WorkflowAnnotation> getAnnotations() {
		return Collections.unmodifiableSet(annotations);
	}

	public Edit<T> getAddAnnotationEdit(final WorkflowAnnotation newAnnotation) {
		return new AbstractAnnotationEdit<T>(this) {
			@Override
			protected void doEditAction(AbstractAnnotatedThing<?> subject)
					throws EditException {
				annotations.add(newAnnotation);
			}

			@Override
			protected void undoEditAction(AbstractAnnotatedThing<?> subject) {
				annotations.remove(newAnnotation);
			}
		};
	}

	public Edit<T> getRemoveAnnotationEdit(
			final WorkflowAnnotation annotationToRemove) {
		return new AbstractAnnotationEdit<T>(this) {
			@Override
			protected void doEditAction(AbstractAnnotatedThing<?> subject)
					throws EditException {
				annotations.remove(annotationToRemove);
			}

			@Override
			protected void undoEditAction(AbstractAnnotatedThing<?> subject) {
				annotations.add(annotationToRemove);
			}
		};
	}

	public Edit<T> getReplaceAnnotationEdit(final WorkflowAnnotation oldAnnotation,
			final WorkflowAnnotation newAnnotation) {
		return new AbstractAnnotationEdit<T>(this) {
			@Override
			protected void doEditAction(AbstractAnnotatedThing<?> subject)
					throws EditException {
				annotations.remove(oldAnnotation);
				annotations.add(newAnnotation);
			}

			@Override
			protected void undoEditAction(AbstractAnnotatedThing<?> subject) {
				annotations.remove(newAnnotation);
				annotations.add(oldAnnotation);
			}
		};
	}

}
