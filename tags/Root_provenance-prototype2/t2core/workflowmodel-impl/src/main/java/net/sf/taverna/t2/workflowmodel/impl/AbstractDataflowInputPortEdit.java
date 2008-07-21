package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Abstraction of an edit acting on a DataflowInputPort instance. Handles the check to
 * see that the DataflowInputPort supplied is really a DataflowInputPortImpl.
 * 
 * @author David Withers
 *
 */
public abstract class AbstractDataflowInputPortEdit implements Edit<DataflowInputPort> {

	private boolean applied = false;

	private DataflowInputPort dataflowInputPort;

	protected AbstractDataflowInputPortEdit(DataflowInputPort dataflowInputPort) {
		if (dataflowInputPort == null) {
			throw new RuntimeException(
					"Cannot construct a DataflowInputPort edit with null DataflowInputPort");
		}
		this.dataflowInputPort = dataflowInputPort;
	}

	public final DataflowInputPort doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
		if (dataflowInputPort instanceof DataflowInputPortImpl == false) {
			throw new EditException(
					"Edit cannot be applied to a DataflowInputPort which isn't an instance of DataflowInputPortImpl");
		}
		DataflowInputPortImpl dataflowInputPortImpl = (DataflowInputPortImpl) dataflowInputPort;
		try {
			synchronized (dataflowInputPortImpl) {
				doEditAction(dataflowInputPortImpl);
				applied = true;
				return this.dataflowInputPort;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
	}

	/**
	 * Do the actual edit here
	 * 
	 * @param dataflowInputPort
	 *            The DataflowInputPortImpl to which the edit applies
	 * @throws EditException
	 */
	protected abstract void doEditAction(DataflowInputPortImpl dataflowInputPort)
			throws EditException;

	/**
	 * Undo any edit effects here
	 */
	protected abstract void undoEditAction(DataflowInputPortImpl dataflowInputPort);

	public final DataflowInputPort getSubject() {
		return dataflowInputPort;
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		DataflowInputPortImpl dataflowInputPortImpl = (DataflowInputPortImpl) dataflowInputPort;
		synchronized (dataflowInputPortImpl) {
			undoEditAction(dataflowInputPortImpl);
			applied = false;
		}

	}
}
