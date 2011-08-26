package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

/**
 * Set the iteration strategy
 * 
 * @author Stian Soiland-Reyes
 *
 */
public class SetIterationStrategyStackEdit extends AbstractProcessorEdit {

	private final IterationStrategyStack iterationStrategyStack;
	private IterationStrategyStackImpl oldStrategyStack;

	public SetIterationStrategyStackEdit(Processor processor,
			IterationStrategyStack iterationStrategyStack) {
		super(processor);
		this.iterationStrategyStack = iterationStrategyStack;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		oldStrategyStack = processor.getIterationStrategy();

		if (!(iterationStrategyStack instanceof IterationStrategyStackImpl)) {
			throw new EditException(
					"Unknown implementation of iteration strategy "
							+ iterationStrategyStack);
		}
		processor.iterationStack = (IterationStrategyStackImpl) iterationStrategyStack;
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		processor.iterationStack = oldStrategyStack;
	}

}
