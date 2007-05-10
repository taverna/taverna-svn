package net.sf.taverna.t2.workflowmodel.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.ContextManager;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.OutputPort;
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

	public void completionCreated(Completion completion) {
		if (completion.getIndex().length == 0) {
			int depth = parent.getEmptyListDepth(completion.getOwningProcess());
			Map<String, EntityIdentifier> emptyJobMap = new HashMap<String, EntityIdentifier>();
			DataManager dManager = ContextManager.getDataManager(completion.getOwningProcess());
			for (OutputPort op : parent.getOutputPorts()) {
				emptyJobMap.put(op.getName(), dManager.registerEmptyList(depth+op.getDepth()));
			}
			jobCreated(new Job(completion.getOwningProcess(), new int[0], emptyJobMap));
		} else {
			// We can ignore this here, it means we had an iteration over an
			// empty list at some point but this will eventually be handled by
			// the top level completion.
			/**
			 * throw new WorkflowStructureException( "Completion event emited by
			 * crystalizer should never happen in the context of a
			 * ProcessorImpl");
			 */
		}
	}

	public void jobCreated(Job outputJob) {
		if (outputJob.getIndex().length == 0) {
			parent.forgetDepthFor(outputJob.getOwningProcess());
		}
		for (String outputPortName : outputJob.getData().keySet()) {
			WorkflowDataToken token = new WorkflowDataToken(outputJob
					.getOwningProcess(), outputJob.getIndex(), outputJob
					.getData().get(outputPortName));
			parent.getOutputPortWithName(outputPortName).receiveEvent(token);
		}
	}

}
