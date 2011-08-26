package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Abstraction of an edit acting on a Activity instance. Handles the check to
 * see that the Activity supplied is really a AbstractActivity.
 * 
 * @author David Withers
 *
 */
public abstract class AbstractActivityEdit implements Edit<Activity<?>> {

	private boolean applied = false;

	private Activity<?> activity;

	protected AbstractActivityEdit(Activity<?> activity) {
		if (activity == null) {
			throw new RuntimeException(
					"Cannot construct a activity edit with null activity");
		}
		this.activity = activity;
	}

	public final Activity<?> doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
		if (activity instanceof AbstractActivity == false) {
			throw new EditException(
					"Edit cannot be applied to a Activity which isn't an instance of AbstractActivity");
		}
		AbstractActivity<?> abstractActivity = (AbstractActivity<?>) activity;
		try {
			synchronized (abstractActivity) {
				doEditAction(abstractActivity);
				applied = true;
				return this.activity;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
	}

	/**
	 * Do the actual edit here
	 * 
	 * @param activity
	 *            The ActivityImpl to which the edit applies
	 * @throws EditException
	 */
	protected abstract void doEditAction(AbstractActivity<?> activity)
			throws EditException;

	/**
	 * Undo any edit effects here
	 */
	protected abstract void undoEditAction(AbstractActivity<?> activity);

	public final Activity<?> getSubject() {
		return activity;
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		AbstractActivity<?> abstractActivity = (AbstractActivity<?>) activity;
		synchronized (abstractActivity) {
			undoEditAction(abstractActivity);
			applied = false;
		}

	}
}
