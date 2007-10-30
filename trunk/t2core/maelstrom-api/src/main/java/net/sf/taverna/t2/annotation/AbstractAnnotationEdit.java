package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Abstract superclass of edits operating on annotation or metadata objects,
 * ensures that the object is actually an implementation of MutableAnnotated
 * before delegating to the subclass to perform the actual edit logic.
 * 
 * @author Tom Oinn
 * 
 * @param <TargetType>
 * 
 */
public abstract class AbstractAnnotationEdit<TargetType> implements
		Edit<TargetType> {

	private AbstractAnnotatedThing<TargetType> subject;

	private boolean applied = false;

	protected AbstractAnnotationEdit(AbstractAnnotatedThing<TargetType> subject) {
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
