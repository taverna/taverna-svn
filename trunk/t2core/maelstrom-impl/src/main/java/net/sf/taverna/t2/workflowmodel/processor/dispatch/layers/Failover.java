package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAnnotationContainer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractErrorHandlerLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerAction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchMessageType;

/**
 * Failure handling dispatch layer, consumes job events with multiple activities
 * and emits the same job but with only the first activity. On failures the job
 * is resent to the layer below with a new activity list containing the second in
 * the original list and so on. If a failure is received and there are no
 * further activities to use the job fails and the failure is sent back up to the
 * layer above. * <table>
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
 * <td>REWRITE</td>
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
public class Failover extends AbstractErrorHandlerLayer<Object> {

	public Failover() {
		super();
		messageActions
				.put(DispatchMessageType.JOB, DispatchLayerAction.REWRITE);

	}

	@Override
	protected JobState getStateObject(Job j,
			List<? extends ActivityAnnotationContainer> activities) {
		return new FailoverState(j, activities);
	}

	/**
	 * Receive a job from the layer above, store it in the state map then relay
	 * it to the layer below with a modified activity list containing only the
	 * activity at index 0
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void receiveJob(Job job,
			List<? extends ActivityAnnotationContainer> activities) {

		List<JobState> stateList = null;
		synchronized (stateMap) {
			stateList = stateMap.get(job.getOwningProcess());
			if (stateList == null) {
				stateList = new ArrayList<JobState>();
				stateMap.put(job.getOwningProcess(), stateList);
			}
		}
		stateList.add(getStateObject(job, activities));
		List<ActivityAnnotationContainer> newActivityList = new ArrayList<ActivityAnnotationContainer>();
		newActivityList.add(activities.get(0));
		getBelow().receiveJob(job, newActivityList);

	}

	class FailoverState extends JobState {

		int currentActivityIndex = 0;

		public FailoverState(Job j, List<? extends ActivityAnnotationContainer> activities) {
			super(j, activities);
		}

		@SuppressWarnings("unchecked")
		public boolean handleError() {
			currentActivityIndex++;
			if (currentActivityIndex == activities.size()) {
				return false;
			} else {
				List<ActivityAnnotationContainer> newActivityList = new ArrayList<ActivityAnnotationContainer>();
				newActivityList.add(activities.get(currentActivityIndex));
				getBelow().receiveJob(job, newActivityList);
				return true;
			}
		}

	}

	public void configure(Object config) {
		// Do nothing - there is no configuration to do
	}

	public Object getConfiguration() {
		return null;
	}

}
