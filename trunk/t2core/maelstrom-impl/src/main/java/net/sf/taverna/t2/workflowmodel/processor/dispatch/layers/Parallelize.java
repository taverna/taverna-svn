package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.IterationInternalEvent;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.NotifiableLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerErrorReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerJobQueueReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerResultCompletionReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerResultReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.SupportsStreamedResult;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobQueueEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.*;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType.*;

/**
 * Dispatch layer which consumes a queue of events and fires off a fixed number
 * of simultaneous jobs to the layer below. It observes failure, data and
 * completion events coming up and uses these to determine when to push more
 * jobs downwards into the stack as well as when it can safely emit completion
 * events from the queue.
 * 
 * @author Tom Oinn
 * 
 */
@DispatchLayerErrorReaction(emits = {}, relaysUnmodified = true, stateEffects = {
		REMOVE_PROCESS_STATE, NO_EFFECT })
@DispatchLayerJobQueueReaction(emits = { JOB }, relaysUnmodified = false, stateEffects = { CREATE_PROCESS_STATE })
@DispatchLayerResultReaction(emits = {}, relaysUnmodified = true, stateEffects = {
		REMOVE_PROCESS_STATE, NO_EFFECT })
@DispatchLayerResultCompletionReaction(emits = {}, relaysUnmodified = true, stateEffects = {
		REMOVE_PROCESS_STATE, NO_EFFECT })
@SupportsStreamedResult
public class Parallelize extends AbstractDispatchLayer<ParallelizeConfig>
		implements NotifiableLayer {

	private Map<String, StateModel> stateMap = new HashMap<String, StateModel>();

	private ParallelizeConfig config = new ParallelizeConfig();

	int sentJobsCount = 0;

	public Parallelize() {
		super();
	}

	/**
	 * Test constructor, only used by unit tests, should probably not be public
	 * access here?
	 * 
	 * @param maxJobs
	 */
	public Parallelize(int maxJobs) {
		super();
		config.setMaximumJobs(maxJobs);
	}

	public void eventAdded(String owningProcess) {
		if (stateMap.containsKey(owningProcess) == false) {
			throw new WorkflowStructureException(
					"Should never see this here, it means we've had duplicate completion events from upstream");
		} else {
			synchronized (stateMap.get(owningProcess)) {
				stateMap.get(owningProcess).fillFromQueue();
			}
		}
	}

	public void receiveJobQueue(DispatchJobQueueEvent queueEvent) {
		// System.out.println("Creating state for " + owningProcess);
		StateModel model = new StateModel(queueEvent, config.getMaximumJobs());
		stateMap.put(queueEvent.getOwningProcess(), model);
		model.fillFromQueue();
	}

	public void receiveJob(Job job, List<? extends Activity<?>> activities) {
		throw new WorkflowStructureException(
				"Parallelize layer cannot handle job events");
	}

	public void receiveError(DispatchErrorEvent errorEvent) {
		// System.out.println(sentJobsCount);
		StateModel model = stateMap.get(errorEvent.getOwningProcess());
		getAbove().receiveError(errorEvent);
		model.finishWith(errorEvent.getIndex());
	}

	@SuppressWarnings("unchecked")
	public void receiveResult(DispatchResultEvent resultEvent) {
		StateModel model = stateMap.get(resultEvent.getOwningProcess());
		DispatchLayer above = getAbove();
		above.receiveResult(resultEvent);
		model.finishWith(resultEvent.getIndex());
	}

	/**
	 * Only going to receive this if the activity invocation was streaming, in
	 * which case we need to handle all completion events and pass them up the
	 * stack.
	 */
	public void receiveResultCompletion(DispatchCompletionEvent completionEvent) {
		StateModel model = stateMap.get(completionEvent.getOwningProcess());
		getAbove().receiveResultCompletion(completionEvent);
		model.finishWith(completionEvent.getIndex());
	}

	public void finishedWith(String owningProcess) {
		// System.out.println("Removing state map for " + owningProcess);
		stateMap.remove(owningProcess);
	}

	/**
	 * Holds the state for a given owning process
	 * 
	 * @author Tom
	 * 
	 */
	class StateModel {

		private DispatchJobQueueEvent queueEvent;

		private BlockingQueue<IterationInternalEvent<? extends IterationInternalEvent<?>>> pendingEvents = new LinkedBlockingQueue<IterationInternalEvent<? extends IterationInternalEvent<?>>>();

		private int activeJobs = 0;

		private int maximumJobs;

		/**
		 * Construct state model for a particular owning process
		 * 
		 * @param owningProcess
		 *            Process to track parallel execution
		 * @param queue
		 *            reference to the queue into which jobs are inserted by the
		 *            iteration strategy
		 * @param activities
		 *            activities to pass along with job events down into the
		 *            stack below
		 * @param maxJobs
		 *            maximum number of concurrent jobs to keep 'hot' at any
		 *            given point
		 */
		protected StateModel(DispatchJobQueueEvent queueEvent, int maxJobs) {
			this.queueEvent = queueEvent;
			this.maximumJobs = maxJobs;
		}

		/**
		 * Poll the queue repeatedly until either the queue is empty or we have
		 * enough jobs pulled from it. The semantics for this are:
		 * <ul>
		 * <li>If the head of the queue is a Job and activeJobs < maximumJobs
		 * then increment activeJobs, add the Job to the pending events list at
		 * the end and send the message down the stack
		 * <li>If the head of the queue is a Completion and the pending jobs
		 * list is empty then send it to the layer above
		 * <li>If the head of the queue is a Completion and the pending jobs
		 * list is not empty then add the Completion to the end of the pending
		 * jobs list and return
		 * </ul>
		 */
		protected void fillFromQueue() {
			synchronized (this) {
				while (queueEvent.getQueue().peek() != null
						&& activeJobs < maximumJobs) {
					final IterationInternalEvent<? extends IterationInternalEvent<?>> e = queueEvent
							.getQueue().remove();

					if (e instanceof Completion && pendingEvents.peek() == null) {
						new Thread(new Runnable() {
							public void run() {
								getAbove().receiveResultCompletion(
										new DispatchCompletionEvent(e
												.getOwningProcess(), e
												.getIndex(), e.getContext()));
							}
						}).start();
						// getAbove().receiveResultCompletion((Completion) e);
					} else {
						pendingEvents.add(e);
					}
					if (e instanceof Job) {
						synchronized (this) {
							activeJobs++;
						}
						// sentJobsCount++;
						getBelow()
								.receiveJob(
										new DispatchJobEvent(e
												.getOwningProcess(), e
												.getIndex(), e.getContext(),
												((Job) e).getData(), queueEvent
														.getActivities()));
					}
				}
			}
		}

		/**
		 * Returns true if the index matched an existing Job exactly, if this
		 * method returns false then you have a partial completion event which
		 * should be sent up the stack without modification.
		 * 
		 * @param index
		 * @return
		 */
		protected boolean finishWith(int[] index) {
			synchronized (this) {

				for (IterationInternalEvent<? extends IterationInternalEvent<?>> e : new ArrayList<IterationInternalEvent<? extends IterationInternalEvent<?>>>(
						pendingEvents)) {
					if (e instanceof Job) {
						Job j = (Job) e;
						if (arrayEquals(j.getIndex(), index)) {
							// Found a job in the pending events list which has
							// the same index, remove it and decrement the
							// current count of active jobs
							pendingEvents.remove(e);
							activeJobs--;
							// Now pull any completion events that have reached
							// the head of the queue - this indicates that all
							// the job events which came in before them have
							// been processed and we can emit the completions
							while (pendingEvents.peek() != null
									&& pendingEvents.peek() instanceof Completion) {
								Completion c = (Completion) pendingEvents
										.remove();
								getAbove().receiveResultCompletion(
										new DispatchCompletionEvent(c
												.getOwningProcess(), c
												.getIndex(), c.getContext()));

							}
							// Refresh from the queue; as we've just decremented
							// the active job count there should be a worker
							// available
							fillFromQueue();
							// Return true to indicate that we removed a job
							// event from the queue, that is to say that the
							// index wasn't that of a partial completion.
							return true;
						}
					}
				}
			}
			return false;
		}

		private boolean arrayEquals(int[] a, int[] b) {
			if (a.length != b.length) {
				return false;
			}
			for (int i = 0; i < a.length; i++) {
				if (a[i] != b[i]) {
					return false;
				}
			}
			return true;
		}
	}

	public void configure(ParallelizeConfig config) {
		this.config = config;
	}

	public ParallelizeConfig getConfiguration() {
		return this.config;
	}

}
