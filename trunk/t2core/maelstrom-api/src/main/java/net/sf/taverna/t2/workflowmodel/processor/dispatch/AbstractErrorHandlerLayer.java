package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAnnotationContainer;

/**
 * Superclass of error handling dispatch layers (for example retry and
 * failover). Provides generic functionality required by this class of layers.
 * <table>
 * <tr>
 * <th>DispatchMessageType</th>
 * <th>DispatchLayerAction</th>
 * <th>canProduce</th>
 * </tr>
 * <tr>
 * <td>ERROR</td>
 * <td>ACT</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>JOB</td>
 * <td>OBSERVE</td>
 * <td>true</td>
 * </tr> *
 * <tr>
 * <td>JOBQUEUE</td>
 * <td>FORBIDDEN</td>
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
public abstract class AbstractErrorHandlerLayer<ConfigurationType> extends
		AbstractDispatchLayer<ConfigurationType> {

	/**
	 * Sets action on ERROR to ACT by default and on JOB to OBSERVE, as the
	 * common pattern for subclasses of this type is to observe jobs going down,
	 * making a record of the state in the JobState class and to then respond to
	 * errors coming back up based on that state model.
	 * <p>
	 * Many subclasses will in fact rewrite the JOB message, for example the
	 * failover behaviour relies on a rewrite of the activity set.
	 * <p>
	 * This also sets the property that the layer can actively produce new JOB
	 * events as failure management layers by default will react to failure
	 * messages by producing new JOBs and sending them back down.
	 * 
	 */
	protected AbstractErrorHandlerLayer() {
		super();
		messageActions.put(DispatchMessageType.ERROR, DispatchLayerAction.ACT);
		messageActions
				.put(DispatchMessageType.JOB, DispatchLayerAction.OBSERVE);
		producesMessage.put(DispatchMessageType.JOB, true);
	}

	/**
	 * Map of process name -> list of state models
	 */
	protected Map<String, List<JobState>> stateMap = new HashMap<String, List<JobState>>();

	/**
	 * Generate an appropriate state object from the specified job and list of
	 * activities. The state object is a concrete subclass of JobState.
	 * 
	 * @param j
	 * @param activities
	 * @return
	 */
	protected abstract JobState getStateObject(Job j, List<? extends ActivityAnnotationContainer> activities);

	/**
	 * Abstract superclass of all state models for pending failure handlers.
	 * This object is responsible for handling failure messages if they occur
	 * and represents the current state of the failure handling algorithm on a
	 * per job basis.
	 * 
	 * @author Tom
	 * 
	 */
	protected abstract class JobState {
		protected Job job;

		protected List<? extends ActivityAnnotationContainer> activities;

		protected JobState(Job job, List<? extends ActivityAnnotationContainer> activities) {
			this.job = job;
			this.activities = activities;
		}

		/**
		 * Called when the layer below pushes an error up and where the error
		 * index and owning process matches that of this state object. The
		 * implementation must deal with the error, either by handling it and
		 * pushing a new job down the stack or by rejecting it. If this method
		 * returns false the error has not been dealt with and MUST be pushed up
		 * the stack by the active dispatch layer. In this case the layer will
		 * be a subclass of AbstractErrorHandlerLayer and the logic to do this
		 * is already included in the receive methods for results, errors and
		 * completion events.
		 * 
		 * @return true if the error was handled.
		 */
		public abstract boolean handleError();

	}

	/**
	 * Receive a job from the layer above, store it for later retries and pass
	 * it down to the next layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void receiveJob(Job job, List<? extends ActivityAnnotationContainer> activities) {

		List<JobState> stateList = null;
		synchronized (stateMap) {
			stateList = stateMap.get(job.getOwningProcess());
			if (stateList == null) {
				stateList = new ArrayList<JobState>();
				stateMap.put(job.getOwningProcess(), stateList);
			}
		}
		stateList.add(getStateObject(job, activities));
		getBelow().receiveJob(job, activities);
	}

	/**
	 * If an error occurs we can either handle the error or send it to the layer
	 * above for further processing.
	 */
	@Override
	public void receiveError(String owningProcess, int[] index,
			String errorMessage, Throwable detail) {
		List<JobState> activeJobs = stateMap.get(owningProcess);
		// Take a copy of the list so we don't modify it while iterating over it
		for (JobState rs : new ArrayList<JobState>(activeJobs)) {
			if (identicalIndex(rs.job.getIndex(), index)) {
				boolean handled = rs.handleError();
				if (!handled) {
					activeJobs.remove(rs);
					getAbove().receiveError(owningProcess, index, errorMessage,
							detail);
					return;
				}
			}
		}
	}

	/**
	 * If we see a result with an index matching one of those in the current
	 * retry state we can safely forget that state object
	 */
	@Override
	public void receiveResult(Job j) {
		forget(j.getOwningProcess(), j.getIndex());
		getAbove().receiveResult(j);
	}

	/**
	 * If we see a completion event with an index matching one of those in the
	 * current retry state we can safely forget that state object
	 */
	@Override
	public void receiveResultCompletion(Completion c) {
		forget(c.getOwningProcess(), c.getIndex());
		getAbove().receiveResultCompletion(c);
	}

	/**
	 * Clear cached state for the specified process when notified by the
	 * dispatch stack
	 */
	@Override
	public void finishedWith(String owningProcess) {
		stateMap.remove(owningProcess);
	}

	/**
	 * Remove the specified pending retry job from the cache
	 * 
	 * @param owningProcess
	 * @param index
	 */
	private void forget(String owningProcess, int[] index) {
		List<JobState> activeJobs = stateMap.get(owningProcess);
		for (JobState rs : new ArrayList<JobState>(activeJobs)) {
			if (identicalIndex(rs.job.getIndex(), index)) {
				activeJobs.remove(rs);
			}
		}
	}

	/**
	 * Compare two arrays of ints, return true if they are the same length and
	 * if at every index the two integer values are equal
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean identicalIndex(int[] a, int[] b) {
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