package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.annotation.Annotated;
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
 */
public abstract class AbstractAnnotationEdit<TargetType extends Annotated>
		implements Edit<TargetType> {

	private TargetType subject;

	private boolean applied = false;

	protected AbstractAnnotationEdit(TargetType subject) {
		this.subject = subject;
	}

	public final TargetType doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit already applied!");
		}
		try {
			MutableAnnotated targetObject = (MutableAnnotated) subject;
			doEditAction(targetObject);
			this.applied = true;
			return subject;
		} catch (ClassCastException cce) {
			throw new EditException(
					"Cannot manipulate the annotation, subject is not an instance of MutableAnnotated");
		}
	}

	protected abstract void doEditAction(MutableAnnotated subject)
			throws EditException;
	
	protected abstract void undoEditAction(MutableAnnotated subject);

	public final TargetType getSubject() {
		return this.subject;
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		undoEditAction((MutableAnnotated)subject);
		applied = false;
	}

}
