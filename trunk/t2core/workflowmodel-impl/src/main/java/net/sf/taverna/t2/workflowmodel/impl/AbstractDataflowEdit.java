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

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Abstraction of an edit acting on a Dataflow instance. Handles the check to
 * see that the Dataflow supplied is really a DataflowImpl.
 * 
 * @author David Withers
 *
 */
public abstract class AbstractDataflowEdit implements Edit<Dataflow> {

	private boolean applied = false;

	private Dataflow dataflow;

	protected AbstractDataflowEdit(Dataflow dataflow) {
		if (dataflow == null) {
			throw new RuntimeException(
					"Cannot construct a dataflow edit with null dataflow");
		}
		this.dataflow = dataflow;
	}

	public final Dataflow doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
		if (dataflow instanceof DataflowImpl == false) {
			throw new EditException(
					"Edit cannot be applied to a Dataflow which isn't an instance of DataflowImpl");
		}
		DataflowImpl dataflowImpl = (DataflowImpl) dataflow;
		try {
			synchronized (dataflowImpl) {
				doEditAction(dataflowImpl);
				applied = true;
				return this.dataflow;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
	}

	/**
	 * Do the actual edit here
	 * 
	 * @param dataflow
	 *            The DataflowImpl to which the edit applies
	 * @throws EditException
	 */
	protected abstract void doEditAction(DataflowImpl dataflow)
			throws EditException;

	/**
	 * Undo any edit effects here
	 */
	protected abstract void undoEditAction(DataflowImpl dataflow);

	public final Dataflow getSubject() {
		return dataflow;
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		DataflowImpl dataflowImpl = (DataflowImpl) dataflow;
		synchronized (dataflowImpl) {
			undoEditAction(dataflowImpl);
			applied = false;
		}

	}
}
