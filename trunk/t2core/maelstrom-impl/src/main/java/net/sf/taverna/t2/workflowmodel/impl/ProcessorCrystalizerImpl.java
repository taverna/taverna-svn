package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;

/**
 * AbstractCrystalizer bound to a specific ProcessorImpl
 * 
 * @author Tom Oinn
 * 
 */
public class ProcessorCrystalizerImpl extends AbstractCrystalizer {

	private ProcessorImpl parent;

	/**
	 * Create and bind to the specified ProcessorImpl
	 * 
	 * @param parent
	 */
	protected ProcessorCrystalizerImpl(ProcessorImpl parent) {
		this.parent = parent;
	}

	/**
	 * Always throws WorkflowStructureException, the process invocation logic
	 * should ensure we never see this but if it fails at least we'll know.
	 */
	public void completionCreated(Completion completion) {
		throw new WorkflowStructureException(
				"Completion event emited by crystalizer should never happen in the context of a ProcessorImpl");
		// for (ProcessorOutputPort p : parent.outputPorts) {
		// p.receiveEvent(completion);
		// }
	}

	public void jobCreated(Job outputJob) {
		for (String outputPortName : outputJob.getData().keySet()) {
			WorkflowDataToken token = new WorkflowDataToken(outputJob
					.getOwningProcess(), outputJob.getIndex(), outputJob
					.getData().get(outputPortName));
			parent.getOutputPortWithName(outputPortName).receiveEvent(token);
		}
	}

}
