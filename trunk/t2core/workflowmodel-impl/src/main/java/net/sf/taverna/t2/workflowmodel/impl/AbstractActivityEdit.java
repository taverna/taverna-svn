/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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
		this.setActivity(activity);
	}

	public final Activity<?> doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
		if (getActivity() instanceof AbstractActivity == false) {
			throw new EditException(
					"Edit cannot be applied to a Activity which isn't an instance of AbstractActivity");
		}
		AbstractActivity<?> abstractActivity = (AbstractActivity<?>) getActivity();
		try {
			synchronized (abstractActivity) {
				doEditAction(abstractActivity);
				applied = true;
				return this.getActivity();
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
		return getActivity();
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		AbstractActivity<?> abstractActivity = (AbstractActivity<?>) getActivity();
		synchronized (abstractActivity) {
			undoEditAction(abstractActivity);
			applied = false;
		}

	}

	public void setActivity(Activity<?> activity) {
		this.activity = activity;
	}

	public Activity<?> getActivity() {
		return activity;
	}
}
