package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.AddDispatchLayerEdit;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ErrorBounce;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;

public class DefaultDispatchStackEdit extends AbstractProcessorEdit {
	private Edit<?> compoundEdit=null;
	private static final int MAX_JOBS = 5;
	private static final long BACKOFF_FACTOR = (long) 1.1;
	private static final int MAX_DELAY = 5000;
	private static final int INITIAL_DELAY = 1000;
	private static final int MAX_RETRIES = 3;

	public DefaultDispatchStackEdit(Processor processor) {
		super(processor);
		DispatchStackImpl stack = ((ProcessorImpl)processor).getDispatchStack();
		// Top level parallelise layer
		int layer = 0;
		List<Edit<?>> edits = new ArrayList<Edit<?>>();
		edits.add(new AddDispatchLayerEdit(stack, new Parallelize(MAX_JOBS), layer++));
		edits.add(new AddDispatchLayerEdit(stack, new ErrorBounce(), layer++));
		edits.add(new AddDispatchLayerEdit(stack, new Failover(), layer++));
		edits.add(new AddDispatchLayerEdit(stack, new Retry(MAX_RETRIES, INITIAL_DELAY,
				MAX_DELAY, BACKOFF_FACTOR), layer++));
		edits.add(new AddDispatchLayerEdit(stack, new Invoke(), layer++));
		compoundEdit=new CompoundEdit(edits);
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		compoundEdit.doEdit();
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		compoundEdit.undo();
	}

}
