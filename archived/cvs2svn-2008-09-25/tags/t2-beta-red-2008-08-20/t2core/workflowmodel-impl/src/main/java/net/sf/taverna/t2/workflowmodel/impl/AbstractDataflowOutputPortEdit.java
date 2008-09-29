package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Abstraction of an edit acting on a DataflowOutputPort instance. Handles the check to
 * see that the DataflowOutputPort supplied is really a DataflowOutputPortImpl.
 * 
 * @author David Withers
 *
 */
public abstract class AbstractDataflowOutputPortEdit implements Edit<DataflowOutputPort> {

	private boolean applied = false;

	private DataflowOutputPort dataflowOutputPort;

	protected AbstractDataflowOutputPortEdit(DataflowOutputPort dataflowOutputPort) {
		if (dataflowOutputPort == null) {
			throw new RuntimeException(
					"Cannot construct a DataflowOutputPort edit with null DataflowOutputPort");
		}
		this.dataflowOutputPort = dataflowOutputPort;
	}

	public final DataflowOutputPort doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
		if (dataflowOutputPort instanceof DataflowOutputPortImpl == false) {
			throw new EditException(
					"Edit cannot be applied to a DataflowOutputPort which isn't an instance of DataflowOutputPortImpl");
		}
		DataflowOutputPortImpl dataflowOutputPortImpl = (DataflowOutputPortImpl) dataflowOutputPort;
		try {
			synchronized (dataflowOutputPortImpl) {
				doEditAction(dataflowOutputPortImpl);
				applied = true;
				return this.dataflowOutputPort;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
	}

	/**
	 * Do the actual edit here
	 * 
	 * @param dataflowOutputPort
	 *            The DataflowOutputPortImpl to which the edit applies
	 * @throws EditException
	 */
	protected abstract void doEditAction(DataflowOutputPortImpl dataflowOutputPort)
			throws EditException;

	/**
	 * Undo any edit effects here
	 */
	protected abstract void undoEditAction(DataflowOutputPortImpl dataflowOutputPort);

	public final DataflowOutputPort getSubject() {
		return dataflowOutputPort;
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		DataflowOutputPortImpl dataflowOutputPortImpl = (DataflowOutputPortImpl) dataflowOutputPort;
		synchronized (dataflowOutputPortImpl) {
			undoEditAction(dataflowOutputPortImpl);
			applied = false;
		}

	}
}
