package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Abstraction of an edit acting on a Datalink instance. Handles the check to
 * see that the Datalink supplied is really a DatalinkImpl.
 * 
 * @author David Withers
 *
 */
public abstract class AbstractDatalinkEdit implements Edit<Datalink> {

	private boolean applied = false;

	private Datalink datalink;

	protected AbstractDatalinkEdit(Datalink datalink) {
		if (datalink == null) {
			throw new RuntimeException(
					"Cannot construct a datalink edit with null datalink");
		}
		this.datalink = datalink;
	}

	public final Datalink doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
		if (datalink instanceof DatalinkImpl == false) {
			throw new EditException(
					"Edit cannot be applied to a Datalink which isn't an instance of DatalinkImpl");
		}
		DatalinkImpl datalinkImpl = (DatalinkImpl) datalink;
		try {
			synchronized (datalinkImpl) {
				doEditAction(datalinkImpl);
				applied = true;
				return this.datalink;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
	}

	/**
	 * Do the actual edit here
	 * 
	 * @param datalink
	 *            The DatalinkImpl to which the edit applies
	 * @throws EditException
	 */
	protected abstract void doEditAction(DatalinkImpl datalink)
			throws EditException;

	/**
	 * Undo any edit effects here
	 */
	protected abstract void undoEditAction(DatalinkImpl datalink);

	public final Datalink getSubject() {
		return datalink;
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		DatalinkImpl datalinkImpl = (DatalinkImpl) datalink;
		synchronized (datalinkImpl) {
			undoEditAction(datalinkImpl);
			applied = false;
		}

	}
}
