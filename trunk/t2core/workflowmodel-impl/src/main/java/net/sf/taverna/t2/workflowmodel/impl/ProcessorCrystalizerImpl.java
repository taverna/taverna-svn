package net.sf.taverna.t2.workflowmodel.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

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
		for (String outputPortName : outputJob.getData().keySet()) {
			WorkflowDataToken token = new WorkflowDataToken(outputJob
					.getOwningProcess(), outputJob.getIndex(), outputJob
					.getData().get(outputPortName), outputJob.getContext());
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
	public Job getEmptyJob(String owningProcess, int[] index, InvocationContext context) {
		int wrappingDepth = parent.resultWrappingDepth;
		if (wrappingDepth < 0)
			throw new RuntimeException(
					"Processor hasn't been configured, cannot emit empty job");
		// The wrapping depth is the length of index array that would be used if
		// a single item of the output port type were returned. We can examine
		// the index array for the node we're trying to create and use this to
		// work out how much we need to add to the output port depth to create
		// empty lists of the right type given the index array.
		int depth = wrappingDepth - index.length;
		// TODO - why was this incrementing?
		// depth++;
		DataManager dManager = context.getDataManager();
		Map<String, EntityIdentifier> emptyJobMap = new HashMap<String, EntityIdentifier>();
		for (OutputPort op : parent.getOutputPorts()) {
			emptyJobMap.put(op.getName(), dManager.registerEmptyList(depth
					+ op.getDepth()));
		}
		return new Job(owningProcess, index, emptyJobMap, context);
	}

}
