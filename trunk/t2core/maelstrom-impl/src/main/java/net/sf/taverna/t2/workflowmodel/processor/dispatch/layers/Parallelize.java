package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerAction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchMessageType;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.NotifiableLayer;

/**
 * Dispatch layer which consumes a queue of events and fires off a fixed number
 * of simultaneous jobs to the layer below. It observes failure, data and
 * completion events coming up and uses these to determine when to push more
 * jobs downwards into the stack as well as when it can safely emit completion
 * events from the queue. <table>
 * <tr>
 * <th>DispatchMessageType</th>
 * <th>DispatchLayerAction</th>
 * <th>canProduce</th>
 * </tr>
 * <tr>
 * <td>ERROR</td>
 * <td>PASSTHROUGH</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>JOB</td>
 * <td>FORBIDDEN</td>
 * <td>true</td>
 * </tr> *
 * <tr>
 * <td>JOBQUEUE</td>
 * <td>ACTNORELAY</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>RESULT</td>
 * <td>PASSTHROUGH</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>RESULTCOMPLETION</td>
 * <td>PASSTHROUGH</td>
 * <td>false</td>
 * </tr>
 * </table>
 * 
 * @author Tom Oinn
 * 
 */
public class Parallelize extends AbstractDispatchLayer<ParallelizeConfig>
		implements NotifiableLayer {

	private Map<String, StateModel> stateMap = new HashMap<String, StateModel>();

	private ParallelizeConfig config = new ParallelizeConfig();

	int sentJobsCount = 0;

	public Parallelize() {
		super();
		messageActions.put(DispatchMessageType.JOBQUEUE,
				DispatchLayerAction.ACTNORELAY);
		messageActions.put(DispatchMessageType.JOB,
				DispatchLayerAction.FORBIDDEN);
		producesMessage.put(DispatchMessageType.JOB, true);
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

	public DispatchLayerAction getReceiveJobQueueAction() {
		return DispatchLayerAction.ACT;
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

	public void receiveJobQueue(String owningProcess,
			BlockingQueue<Event> queue,
			List<? extends Activity<?>> activities) {
		// System.out.println("Creating state for " + owningProcess);
		StateModel model = new StateModel(owningProcess, queue, activities,
				config.getMaximumJobs());
		stateMap.put(owningProcess, model);
		model.fillFromQueue();
	}

	public DispatchLayerAction getReceiveJobAction() {
		return DispatchLayerAction.FORBIDDEN;
	}

	public void receiveJob(Job job,
			List<? extends Activity<?>> activities) {
		throw new WorkflowStructureException(
				"Parallelize layer cannot handle job events");
	}

	public void receiveError(String owningProcess, int[] index,
			String errorMessage, Throwable detail) {
		// System.out.println(sentJobsCount);
		StateModel model = stateMap.get(owningProcess);
		getAbove().receiveError(owningProcess, index, errorMessage, detail);
		model.finishWith(index);
	}

	public void receiveResult(Job j) {
		StateModel model = stateMap.get(j.getOwningProcess());
		getAbove().receiveResult(j);
		model.finishWith(j.getIndex());
	}

	/**
	 * Only going to receive this if the activity invocation was streaming, in
	 * which case we need to handle all completion events and pass them up the
	 * stack.
	 */
	public void receiveResultCompletion(Completion c) {
		StateModel model = stateMap.get(c.getOwningProcess());
		getAbove().receiveResultCompletion(c);
		model.finishWith(c.getIndex());
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

		private BlockingQueue<Event> queue;

		private List<? extends Activity<?>> activities;

		private BlockingQueue<Event> pendingEvents = new LinkedBlockingQueue<Event>();

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
		protected StateModel(String owningProcess, BlockingQueue<Event> queue,
				List<? extends Activity<?>> activities,
				int maxJobs) {
			this.queue = queue;
			this.activities = activities;
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
				while (queue.peek() != null && activeJobs < maximumJobs) {
					final Event e = queue.remove();

					if (e instanceof Completion && pendingEvents.peek() == null) {
						new Thread(new Runnable() {
							public void run() {
								getAbove().receiveResultCompletion(
										(Completion) e);
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
						getBelow().receiveJob((Job) e, activities);
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

				for (Event e : new ArrayList<Event>(pendingEvents)) {
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
								getAbove().receiveResultCompletion(c);

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
