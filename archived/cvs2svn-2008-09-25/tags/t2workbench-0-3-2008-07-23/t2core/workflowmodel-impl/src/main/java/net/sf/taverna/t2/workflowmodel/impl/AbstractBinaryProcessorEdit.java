package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.OrderedPair;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Generalization over all operations acting on an ordered pair of ProcessorImpl
 * objects. These include most operations where a relationship is created,
 * modified or destroyed between two processors.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractBinaryProcessorEdit implements
		Edit<OrderedPair<Processor>> {

	private OrderedPair<Processor> processors;

	private boolean applied = false;

	public AbstractBinaryProcessorEdit(Processor a, Processor b) {
		this.processors = new OrderedPair<Processor>(a, b);
	}

	public final OrderedPair<Processor> doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
		if (processors.getA() instanceof ProcessorImpl == false
				|| processors.getB() instanceof ProcessorImpl == false) {
			throw new EditException(
					"Edit cannot be applied to a Processor which isn't an instance of ProcessorImpl");
		}
		ProcessorImpl pia = (ProcessorImpl) processors.getA();
		ProcessorImpl pib = (ProcessorImpl) processors.getB();

		try {
			synchronized (processors) {
				doEditAction(pia, pib);
				applied = true;
				return this.processors;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
	}

	public final OrderedPair<Processor> getSubject() {
		return this.processors;
	}

	public final boolean isApplied() {
		return this.applied;
	}

	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		ProcessorImpl pia = (ProcessorImpl) processors.getA();
		ProcessorImpl pib = (ProcessorImpl) processors.getB();
		synchronized (processors) {
			undoEditAction(pia, pib);
			applied = false;
		}

	}

	/**
	 * Do the actual edit here
	 * 
	 * @param processorA
	 *            The ProcessorImpl which is in some sense the source of the
	 *            relation between the two being asserted or operated on by this
	 *            edit
	 * @param processorB
	 *            The ProcessorImpl at the other end of the relation. *
	 * @throws EditException
	 */
	protected abstract void doEditAction(ProcessorImpl processorA,
			ProcessorImpl processorB) throws EditException;

	/**
	 * Undo any edit effects here
	 */
	protected abstract void undoEditAction(ProcessorImpl processorA,
			ProcessorImpl processorB);

}
