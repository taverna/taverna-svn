package net.sf.taverna.t2.annotation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Convenient abstract superclass for annotated things, manages edits.
 * Subclasses of this must implement the Annotated interface with their own
 * interface type as the parameter, so for example Processor subclasses would
 * implement Annotated&lt;Processor&gt;
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractAnnotatedThing<T> implements Annotated<T> {

	private Set<AnnotationChain> annotations = new HashSet<AnnotationChain>();

	/**
	 * Return the set of annotations bound to this annotated object, the set
	 * returned is an unmodifiable copy of the internal annotation set, if you
	 * need to modify the annotations you should use the get methods for Edit
	 * objects to do so.
	 * 
	 * @see net.sf.taverna.t2.annotation.Annotated#getAnnotations()
	 */
	public final Set<AnnotationChain> getAnnotations() {
		return Collections.unmodifiableSet(annotations);
	}

	/**
	 * Superclass of edits to remove, add and replace annotations on instances
	 * of the enclosing AbstractAnnotatedThing class
	 * 
	 * @author Tom
	 * 
	 * @param <TargetType>
	 */
	private static abstract class AbstractAnnotationEdit<TargetType> implements
			Edit<TargetType> {

		private AbstractAnnotatedThing<TargetType> subject;

		private boolean applied = false;

		protected AbstractAnnotationEdit(
				AbstractAnnotatedThing<TargetType> subject) {
			this.subject = subject;
		}

		@SuppressWarnings("unchecked")
		public final TargetType doEdit() throws EditException {
			synchronized (subject) {
				if (applied) {
					throw new EditException("Edit already applied!");
				}
				doEditAction(subject);
				this.applied = true;
				return (TargetType) subject;
			}
		}

		protected abstract void doEditAction(AbstractAnnotatedThing<?> subject)
				throws EditException;

		protected abstract void undoEditAction(AbstractAnnotatedThing<?> subject);

		@SuppressWarnings("unchecked")
		public final TargetType getSubject() {
			return (TargetType) subject;
		}

		public final boolean isApplied() {
			return this.applied;
		}

		public final void undo() {
			synchronized (subject) {
				if (!applied) {
					throw new RuntimeException(
							"Attempt to undo edit that was never applied");
				}
				undoEditAction(subject);
				applied = false;
			}
		}

	}

	/**
	 * @see net.sf.taverna.t2.annotation.Annotated#getAddAnnotationEdit(net.sf.taverna.t2.annotation.WorkflowAnnotation)
	 */
	public final Edit<T> getAddAnnotationEdit(
			final AnnotationChain newAnnotation) {
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

	/**
	 * @see net.sf.taverna.t2.annotation.Annotated#getRemoveAnnotationEdit(net.sf.taverna.t2.annotation.WorkflowAnnotation)
	 */
	public final Edit<T> getRemoveAnnotationEdit(
			final AnnotationChain annotationToRemove) {
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

}
