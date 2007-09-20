package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAnnotationContainer;
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

	public Parallelize() {
		super();
		messageActions.put(DispatchMessageType.JOBQUEUE,
				DispatchLayerAction.ACTNORELAY);
		messageActions.put(DispatchMessageType.JOB,
				DispatchLayerAction.FORBIDDEN);
		producesMessage.put(DispatchMessageType.JOB, true);
	}

	public Parallelize(int maxJobs) {
		super();
		config.setMaximumJobs(maxJobs);
	}

	public DispatchLayerAction getReceiveJobQueueAction() {
		return DispatchLayerAction.ACT;
	}

	public void eventAdded(String owningProcess) {
		stateMap.get(owningProcess).fillFromQueue();
	}

	public void receiveJobQueue(String owningProcess,
			BlockingQueue<Event> queue, List<? extends ActivityAnnotationContainer> activities) {
		StateModel model = new StateModel(owningProcess, queue, activities,
				config.getMaximumJobs());
		stateMap.put(owningProcess, model);
		model.fillFromQueue();
	}

	public DispatchLayerAction getReceiveJobAction() {
		return DispatchLayerAction.FORBIDDEN;
	}

	public void receiveJob(Job job, List<? extends ActivityAnnotationContainer> activities) {
		throw new WorkflowStructureException(
				"Parallelize layer cannot handle job events");
	}

	public void receiveError(String owningProcess, int[] index,
			String errorMessage, Throwable detail) {
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

		private List<? extends ActivityAnnotationContainer> activities;

		public StateModel(String owningProcess, BlockingQueue<Event> queue,
				List<? extends ActivityAnnotationContainer> activities, int maxJobs) {
			this.queue = queue;
			this.activities = activities;
			this.maximumJobs = maxJobs;
		}

		List<Event> pendingEvents = new ArrayList<Event>();

		int activeJobs = 0;

		int maximumJobs = 2;

		@SuppressWarnings("unchecked")
		public void receiveEvent(Event e) {
			synchronized (pendingEvents) {
				if (e instanceof Completion && pendingEvents.isEmpty()) {
					getAbove().receiveResultCompletion((Completion) e);
				} else {
					pendingEvents.add(e);
				}
			}
			if (e instanceof Job) {
				activeJobs++;
				getBelow().receiveJob((Job) e, activities);
			}
		}

		public void fillFromQueue() {
			synchronized (pendingEvents) {
				while (queue.peek() != null && activeJobs < maximumJobs) {
					Event e = queue.remove();
					receiveEvent(e);
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
		public boolean finishWith(int[] index) {
			synchronized (pendingEvents) {
				boolean matched = false;
				Set<Event> removeMe = new HashSet<Event>();
				for (Event e : pendingEvents) {
					if (e instanceof Job) {
						Job j = (Job) e;
						if (index.length == j.getIndex().length) {
							boolean equal = true;
							for (int i = 0; i < index.length && equal; i++) {
								if (index[i] != j.getIndex()[i]) {
									equal = false;
								}
							}
							if (equal) {
								removeMe.add(e);
								matched = true;
							}
						}
					}
				}
				if (!matched) {
					return false;
				}
				for (Event e : removeMe) {
					pendingEvents.remove(e);
					activeJobs--;
				}
				boolean finished = false;
				while (!finished) {
					if (pendingEvents.isEmpty()
							|| pendingEvents.get(0) instanceof Job) {
						finished = true;
					} else {
						Completion c = (Completion) pendingEvents.get(0);
						getAbove().receiveResultCompletion(c);
						pendingEvents.remove(c);
					}
				}
			}
			fillFromQueue();
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
