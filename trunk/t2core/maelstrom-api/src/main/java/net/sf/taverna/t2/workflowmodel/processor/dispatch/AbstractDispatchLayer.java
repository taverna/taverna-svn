package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType;

/**
 * Convenience abstract implementation of DispatchLayer
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractDispatchLayer<ConfigurationType> implements
		DispatchLayer<ConfigurationType> {

	public void setDispatchStack(DispatchStack parentStack) {

		this.dispatchStack = parentStack;

	}

	private DispatchStack dispatchStack;

	protected final DispatchLayer<?> getAbove() {
		return this.dispatchStack.layerAbove(this);
	}

	protected final DispatchLayer<?> getBelow() {
		return this.dispatchStack.layerBelow(this);
	}

	public void receiveError(String owningProcess, int[] errorIndex,
			String errorMessage, Throwable detail) {
		DispatchLayer<?> above = dispatchStack.layerAbove(this);
		if (above != null) {
			above.receiveError(owningProcess, errorIndex, errorMessage, detail);
		}
	}

	@SuppressWarnings("unchecked")
	public void receiveJob(Job job, List<? extends Activity<?>> activities) {
		DispatchLayer<?> below = dispatchStack.layerBelow(this);
		if (below != null) {
			below.receiveJob(job, activities);
		}
	}

	@SuppressWarnings("unchecked")
	public void receiveJobQueue(String owningProcess,
			BlockingQueue<Event> queue, List<? extends Activity<?>> activities) {
		DispatchLayer below = dispatchStack.layerBelow(this);
		if (below != null) {
			below.receiveJobQueue(owningProcess, queue, activities);
		}

	}

	public void receiveResult(Job job) {
		DispatchLayer<?> above = dispatchStack.layerAbove(this);
		if (above != null) {
			above.receiveResult(job);
		}
	}

	public void receiveResultCompletion(Completion completion) {
		DispatchLayer<?> above = dispatchStack.layerAbove(this);
		if (above != null) {
			above.receiveResultCompletion(completion);
		}

	}

	public void finishedWith(String owningProcess) {
		// Do nothing by default
	}

}
