package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.tsunami.SecurityAgentManager;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerAction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchMessageType;

/**
 * Context free invoker layer, does not pass index arrays of jobs into activity
 * instances.
 * <p>
 * This layer will invoke the first invokable activity in the activity list, so
 * any sane dispatch stack will have narrowed this down to a single item list by
 * this point, i.e. by the insertion of a failover layer.
 * <p>
 * Currently only handles activities implementing {@link AsynchronousActivity}. <table>
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

	static int threadCount = 0;
	
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
	int errorCount = 0;
	@Override
	/**
	 * Receive a job from the layer above and pick the first concrete activity
	 * from the list to invoke. Invoke this activity, creating a callback which
	 * will wrap up the result messages in the appropriate collection depth
	 * before sending them on (in general activities are not aware of their
	 * invocation context and should not be responsible for providing correct
	 * index arrays for results)
	 * <p>
	 * This layer will invoke the first invokable activity in the activity list,
	 * so any sane dispatch stack will have narrowed this down to a single item
	 * list by this point, i.e. by the insertion of a failover layer.
	 */
	public void receiveJob(final Job job, final List<? extends Activity<?>> activities) {
		for (Activity<?> a : activities) {
			
			if (a instanceof AsynchronousActivity) {

				// The activity is an AsynchronousActivity so we invoke it with an
				// AsynchronousActivityCallback object containing appropriate
				// callback methods to push results, completions and failures
				// back to the invocation layer.
				final AsynchronousActivity<?> as = (AsynchronousActivity<?>) a;

				// Get the registered DataManager for this process. In most
				// cases this will just be a single DataManager for the entire
				// workflow system but it never hurts to generalize
				final DataManager dManager = job.getContext().getDataManager();

				// Create a Map of EntityIdentifiers named appropriately given
				// the activity mapping
				Map<String, EntityIdentifier> inputData = new HashMap<String, EntityIdentifier>();
				for (String inputName : job.getData().keySet()) {
					String activityInputName = as.getInputPortMapping().get(
							inputName);
					if (activityInputName != null) {
						inputData.put(activityInputName, job.getData().get(
								inputName));
					}
				}

				// Create a callback object to receive events, completions and
				// failure notifications from the activity
				AsynchronousActivityCallback callback = new AsynchronousActivityCallback() {

					private boolean sentJob = false;
					
					public void fail(String message, Throwable t) {
						getAbove().receiveError(job.getOwningProcess(),
								job.getIndex(), message, t);
					}

					public void fail(String message) {
						fail(message, null);
					}

					public InvocationContext getContext() {
						return job.getContext();
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
									.getOwningProcess(), newIndex, job.getContext());
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

						// Construct a new result map using the activity mapping
						// (activity output name to processor output name)
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
								newIndex, resultMap, job.getContext());

						// Push the modified data to the layer above in the
						// dispatch stack
						getAbove().receiveResult(resultJob);

						sentJob = true;
					}

					public SecurityAgentManager getLocalSecurityManager() {
						// TODO Auto-generated method stub
						return null;
					}

					// TODO - this is a naive implementation, we can use this
					// hook to implement thread limit and reuse policies
					
					public void requestRun(Runnable runMe) {
						String newThreadName = job.toString();
						new Thread(runMe, newThreadName).start();
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
