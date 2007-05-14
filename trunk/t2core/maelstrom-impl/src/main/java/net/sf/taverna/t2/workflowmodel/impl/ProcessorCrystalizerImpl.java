package net.sf.taverna.t2.workflowmodel.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.ContextManager;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.OutputPort;
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

	public void completionCreated(Completion completion) {
		throw new WorkflowStructureException(
				"Should never see this if everything is working,"
						+ "if this occurs it is likely that the internal "
						+ "logic is broken, talk to Tom");
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

	@Override
	/**
	 * Used to construct a Job of empty lists at the appropriate depth in the
	 * event of a completion hitting the crystalizer before it sees a child
	 * node, i.e. the result of iterating over an empty collection structure of
	 * some kind.
	 */
	public Job getEmptyJob(String owningProcess, int[] index, int depth) {
		DataManager dManager = ContextManager.getDataManager(owningProcess);
		Map<String, EntityIdentifier> emptyJobMap = new HashMap<String, EntityIdentifier>();
		for (OutputPort op : parent.getOutputPorts()) {
			emptyJobMap.put(op.getName(), dManager.registerEmptyList(depth
					+ op.getDepth()));
		}
		return new Job(owningProcess, index, emptyJobMap);
	}

}
