package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Abstraction of an edit acting on a Processor instance. Handles the check to
 * see that the Processor supplied is really a ProcessorImpl.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractProcessorEdit implements Edit<Processor> {

	private boolean applied = false;

	private Processor processor;

	protected AbstractProcessorEdit(Processor p) {
		if (p == null) {
			throw new RuntimeException(
					"Cannot construct a processor edit with null processor");
		}
		this.processor = p;
	}

	public final Processor doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
		if (!(processor instanceof ProcessorImpl)) {
			throw new EditException(
					"Edit cannot be applied to a Processor which isn't an instance of ProcessorImpl");
		}
		ProcessorImpl pi = (ProcessorImpl) processor;
		try {
			synchronized (pi) {
				doEditAction(pi);
				applied = true;
				return this.processor;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
	}

	/**
	 * Do the actual edit here
	 * 
	 * @param processor
	 *            The ProcessorImpl to which the edit applies
	 * @throws EditException
	 */
	protected abstract void doEditAction(ProcessorImpl processor)
			throws EditException;

	/**
	 * Undo any edit effects here
	 */
	protected abstract void undoEditAction(ProcessorImpl processor);

	public final Processor getSubject() {
		return processor;
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		ProcessorImpl pi = (ProcessorImpl) processor;
		synchronized (pi) {
			undoEditAction(pi);
			applied = false;
		}

	}

}
