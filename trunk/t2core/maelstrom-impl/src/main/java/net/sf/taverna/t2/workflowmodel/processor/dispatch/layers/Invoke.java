package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.ContextManager;
import net.sf.taverna.t2.tsunami.SecurityAgentManager;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerAction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchMessageType;
import net.sf.taverna.t2.workflowmodel.processor.service.AsynchronousService;
import net.sf.taverna.t2.workflowmodel.processor.service.AsynchronousServiceCallback;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;

/**
 * Context free invoker layer, does not pass index arrays of jobs into service
 * instances.
 * <p>
 * This layer will invoke the first invokable service in the service list, so
 * any sane dispatch stack will have narrowed this down to a single item list by
 * this point, i.e. by the insertion of a failover layer.
 * <p>
 * Currently only handles services implementing AsynchronousService. <table>
 * <tr>
 * <th>DispatchMessageType</th>
 * <th>DispatchLayerAction</th>
 * <th>canProduce</th>
 * </tr>
 * <tr>
 * <td>ERROR</td>
 * <td>FORBIDDEN</td>
 * <td>true</td>
 * </tr> *
 * <tr>
 * <td>JOB</td>
 * <td>ACTNORELAY</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>JOBQUEUE</td>
 * <td>FORBIDDEN</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>RESULT</td>
 * <td>FORBIDDEN</td>
 * <td>true</td>
 * </tr> *
 * <tr>
 * <td>RESULTCOMPLETION</td>
 * <td>FORBIDDEN</td>
 * <td>true</td>
 * </tr>
 * </table>
 * 
 * @author Tom Oinn
 * 
 */
public class Invoke extends AbstractDispatchLayer<Object> {

	public Invoke() {
		super();
		messageActions.put(DispatchMessageType.JOB,
				DispatchLayerAction.ACTNORELAY);
		messageActions.put(DispatchMessageType.RESULT,
				DispatchLayerAction.FORBIDDEN);
		messageActions.put(DispatchMessageType.RESULTCOMPLETION,
				DispatchLayerAction.FORBIDDEN);
		messageActions.put(DispatchMessageType.ERROR,
				DispatchLayerAction.FORBIDDEN);
		producesMessage.put(DispatchMessageType.ERROR, true);
		producesMessage.put(DispatchMessageType.RESULT, true);
		producesMessage.put(DispatchMessageType.RESULTCOMPLETION, true);
	}

	@Override
	/**
	 * Receive a job from the layer above and pick the first concrete service
	 * from the list to invoke. Invoke this service, creating a callback which
	 * will wrap up the result messages in the appropriate collection depth
	 * before sending them on (in general services are not aware of their
	 * invocation context and should not be responsible for providing correct
	 * index arrays for results)
	 * <p>
	 * This layer will invoke the first invokable service in the service list,
	 * so any sane dispatch stack will have narrowed this down to a single item
	 * list by this point, i.e. by the insertion of a failover layer.
	 */
	public void receiveJob(final Job job, List<Service> services) {
		for (Service s : services) {
			if (s instanceof AsynchronousService) {

				// The service is an AsynchronousService so we invoke it with an
				// AsynchronousServiceCallback object containing appropriate
				// callback methods to push results, completions and failures
				// back to the invocation layer.
				final AsynchronousService<?> as = (AsynchronousService<?>) s;

				// Get the registered DataManager for this process. In most
				// cases this will just be a single DataManager for the entire
				// workflow system but it never hurts to generalize
				final DataManager dManager = ContextManager.getDataManager(job
						.getOwningProcess());

				// Create a Map of EntityIdentifiers named appropriately given
				// the service mapping
				Map<String, EntityIdentifier> inputData = new HashMap<String, EntityIdentifier>();
				for (String inputName : job.getData().keySet()) {
					String serviceInputName = as.getInputPortMapping().get(
							inputName);
					if (serviceInputName != null) {
						inputData.put(serviceInputName, job.getData().get(
								inputName));
					}
				}

				// Create a callback object to receive events, completions and
				// failure notifications from the service
				AsynchronousServiceCallback callback = new AsynchronousServiceCallback() {

					private boolean sentJob = false;

					public void fail(String message, Throwable t) {
						getAbove().receiveError(job.getOwningProcess(),
								job.getIndex(), message, t);
					}

					public void fail(String message) {
						fail(message, null);
					}

					/**
					 * Return the final datamanager from the surrounding context
					 */
					public DataManager getLocalDataManager() {
						return dManager;
					}

					public void receiveCompletion(int[] completionIndex) {
						if (sentJob) {
							int[] newIndex;
							if (completionIndex.length == 0) {
								newIndex = job.getIndex();
							} else {
								newIndex = new int[job.getIndex().length
										+ completionIndex.length];
								int i = 0;
								for (int indexValue : job.getIndex()) {
									newIndex[i++] = indexValue;
								}
								for (int indexValue : completionIndex) {
									newIndex[i++] = indexValue;
								}
							}
							Completion c = new Completion(job
									.getOwningProcess(), newIndex);
							getAbove().receiveResultCompletion(c);
						} else {
							// We haven't sent any 'real' data prior to
							// completing a stream. This in effect means we're
							// sending an empty top level collection so we need
							// to register empty collections for each output
							// port with appropriate depth (by definition if
							// we're streaming all outputs are collection types
							// of some kind)
							Map<String, EntityIdentifier> emptyListMap = new HashMap<String, EntityIdentifier>();
							for (OutputPort op : as.getOutputPorts()) {
								String portName = op.getName();
								int portDepth = op.getDepth();
								emptyListMap.put(portName, dManager
										.registerEmptyList(portDepth));
							}
							receiveResult(emptyListMap, new int[0]);
						}

					}

					public void receiveResult(
							Map<String, EntityIdentifier> data, int[] index) {

						// Construct a new result map using the service mapping
						// (service output name to processor output name)
						Map<String, EntityIdentifier> resultMap = new HashMap<String, EntityIdentifier>();
						for (String outputName : data.keySet()) {
							String processorOutputName = as
									.getOutputPortMapping().get(outputName);
							if (processorOutputName != null) {
								resultMap.put(processorOutputName, data
										.get(outputName));
							}
						}
						// Construct a new index array if the specified index is
						// non zero length, otherwise just use the original
						// job's index array (means we're not streaming)
						int[] newIndex;
						if (index.length == 0) {
							newIndex = job.getIndex();
						} else {
							newIndex = new int[job.getIndex().length
									+ index.length];
							int i = 0;
							for (int indexValue : job.getIndex()) {
								newIndex[i++] = indexValue;
							}
							for (int indexValue : index) {
								newIndex[i++] = indexValue;
							}
						}
						Job resultJob = new Job(job.getOwningProcess(),
								newIndex, resultMap);

						// Push the modified data to the layer above in the
						// dispatch stack
						getAbove().receiveResult(resultJob);

						sentJob = true;
					}

					public SecurityAgentManager getLocalSecurityManager() {
						// TODO Auto-generated method stub
						return null;
					}

				};

				// Run the job, passing in the callback we've just created along
				// with the (possibly renamed) input data map
				as.executeAsynch(inputData, callback);

				return;
			}
		}
	}

	public void configure(Object config) {
		// No configuration, do nothing
	}

	public Object getConfiguration() {
		return null;
	}

}
