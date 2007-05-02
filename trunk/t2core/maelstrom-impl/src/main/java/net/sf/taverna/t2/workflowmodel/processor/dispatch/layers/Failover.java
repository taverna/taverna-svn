package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractErrorHandlerLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerAction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchMessageType;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;

/**
 * Failure handling dispatch layer, consumes job events with multiple services
 * and emits the same job but with only the first service. On failures the job
 * is resent to the layer below with a new service list containing the second in
 * the original list and so on. If a failure is received and there are no
 * further services to use the job fails and the failure is sent back up to the
 * layer above.
 * * <table>
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
		messageActions.put(DispatchMessageType.JOB, DispatchLayerAction.REWRITE);
		
	}
	
	@Override
	protected JobState getStateObject(Job j, List<Service> services) {
		return new FailoverState(j, services);
	}

	/**
	 * Receive a job from the layer above, store it in the state map then relay
	 * it to the layer below with a modified service list containing only the
	 * service at index 0
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void receiveJob(Job job, List<Service> services) {

		List<JobState> stateList = null;
		synchronized (stateMap) {
			stateList = stateMap.get(job.getOwningProcess());
			if (stateList == null) {
				stateList = new ArrayList<JobState>();
				stateMap.put(job.getOwningProcess(), stateList);
			}
		}
		stateList.add(getStateObject(job, services));
		List<Service> newServiceList = new ArrayList<Service>();
		newServiceList.add(services.get(0));
		getBelow().receiveJob(job, newServiceList);
	}

	class FailoverState extends JobState {

		int currentServiceIndex = 0;

		public FailoverState(Job j, List<Service> services) {
			super(j, services);
		}

		@SuppressWarnings("unchecked")
		public boolean handleError() {
			currentServiceIndex++;
			if (currentServiceIndex == services.size()) {
				return false;
			} else {
				List<Service> newServiceList = new ArrayList<Service>();
				newServiceList.add(services.get(currentServiceIndex));
				getBelow().receiveJob(job, newServiceList);
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
