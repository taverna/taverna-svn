/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.component;

import static java.lang.System.arraycopy;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType.ERROR;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType.RESULT;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType.RESULT_COMPLETION;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.ControlBoundary;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.MonitorableAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerJobReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

import org.apache.log4j.Logger;

/**
 * Context free invoker layer, does not pass index arrays of jobs into activity
 * instances.
 * <p>
 * This layer will invoke the first invokable activity in the activity list, so
 * any sane dispatch stack will have narrowed this down to a single item list by
 * this point, i.e. by the insertion of a failover layer.
 * <p>
 * Currently only handles activities implementing {@link AsynchronousActivity}.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
@DispatchLayerJobReaction(emits = { ERROR, RESULT_COMPLETION, RESULT }, relaysUnmodified = false, stateEffects = {})
@ControlBoundary
public class PatchedInvoke extends AbstractDispatchLayer<Object> {
	private static final Logger logger = Logger.getLogger(PatchedInvoke.class);
	private static Long invocationCount = 0L;
	private static final MonitorManager monitorManager = MonitorManager
			.getInstance();

	private static String getNextProcessID() {
		synchronized (invocationCount) {
			invocationCount = invocationCount + 1L;
		}
		return "invocation" + invocationCount;
	}

	public PatchedInvoke() {
		super();
	}

	@Override
	public void configure(Object config) {
		// No configuration, do nothing
	}

	@Override
	public Object getConfiguration() {
		return null;
	}

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
	@Override
	public void receiveJob(DispatchJobEvent jobEvent) {
		for (Activity<?> activity : jobEvent.getActivities()) {
			if (!(activity instanceof AsynchronousActivity))
				continue;

			/* Register with the monitor */
			String invocationProcessIdentifier = jobEvent.pushOwningProcess(
					getNextProcessID()).getOwningProcess();
			monitorManager.registerNode(activity,
					invocationProcessIdentifier.split(":"),
					new HashSet<MonitorableProperty<?>>());

			/*
			 * The activity is an AsynchronousActivity so we invoke it with an
			 * AsynchronousActivityCallback object containing appropriate
			 * callback methods to push results, completions and failures back
			 * to the invocation layer.
			 */
			invokeAsyncActivity(jobEvent, invocationProcessIdentifier,
					(AsynchronousActivity<?>) activity);
			return;
		}
	}

	private void invokeAsyncActivity(DispatchJobEvent jobEvent,
			String invocationProcessIdentifier,
			AsynchronousActivity<?> asyncActivity) {
		/*
		 * Get the registered DataManager for this process. In most cases this
		 * will just be a single DataManager for the entire workflow system but
		 * it never hurts to generalise.
		 */

		ReferenceService refService = jobEvent.getContext()
				.getReferenceService();

		/*
		 * Create a Map of EntityIdentifiers named appropriately given the
		 * activity mapping.
		 */
		Map<String, T2Reference> inputData = new HashMap<String, T2Reference>();
		for (String inputName : jobEvent.getData().keySet()) {
			String activityInputName = asyncActivity.getInputPortMapping().get(
					inputName);
			if (activityInputName != null)
				inputData.put(activityInputName,
						jobEvent.getData().get(inputName));
		}

		/*
		 * Create a callback object to receive events, completions and failure
		 * notifications from the activity
		 */
		AsynchronousActivityCallback callback = new PatchedInvokeCallBack(
				jobEvent, refService, invocationProcessIdentifier,
				asyncActivity);

		if (asyncActivity instanceof MonitorableAsynchronousActivity<?>) {
			/*
			 * Monitorable activity so get the monitorable properties and push
			 * them into the state tree after launching the job
			 */
			MonitorableAsynchronousActivity<?> maa = (MonitorableAsynchronousActivity<?>) asyncActivity;
			Set<MonitorableProperty<?>> props = maa
					.executeAsynchWithMonitoring(inputData, callback);
			monitorManager.addPropertiesToNode(
					invocationProcessIdentifier.split(":"), props);
		} else {
			/*
			 * Run the job, passing in the callback we've just created along
			 * with the (possibly renamed) input data map
			 */
			asyncActivity.executeAsynch(inputData, callback);
		}
	}

	protected class PatchedInvokeCallBack implements
			AsynchronousActivityCallback {
		protected final AsynchronousActivity<?> asyncActivity;
		protected final String invocationProcessIdentifier;
		protected final DispatchJobEvent jobEvent;
		protected final ReferenceService refService;
		protected boolean sentJob = false;
		private InvocationContext context;

		protected PatchedInvokeCallBack(DispatchJobEvent jobEvent,
				ReferenceService refService,
				String invocationProcessIdentifier,
				AsynchronousActivity<?> asyncActivity) {
			this.jobEvent = jobEvent;
			this.context = this.jobEvent.getContext();
			this.refService = refService;
			this.invocationProcessIdentifier = invocationProcessIdentifier;
			this.asyncActivity = asyncActivity;
		}

		@Override
		public void fail(String message) {
			fail(message, null);
		}

		@Override
		public void fail(String message, Throwable t) {
			fail(message, t, DispatchErrorType.INVOCATION);
		}

		@Override
		public void fail(String message, Throwable t,
				DispatchErrorType errorType) {
			logger.warn("Failed (" + errorType + ") invoking " + asyncActivity
					+ " for job " + jobEvent + ": " + message, t);
			monitorManager.deregisterNode(invocationProcessIdentifier);
			getAbove().receiveError(
					new DispatchErrorEvent(jobEvent.getOwningProcess(),
							jobEvent.getIndex(), jobEvent.getContext(),
							message, t, errorType, asyncActivity));
		}

		@Override
		public InvocationContext getContext() {
			return context;
		}

		@Override
		public String getParentProcessIdentifier() {
			return invocationProcessIdentifier;
		}

		@Override
		public void receiveCompletion(int[] completionIndex) {
			if (completionIndex.length == 0)
				/* Final result, clean up monitor state */
				monitorManager.deregisterNode(invocationProcessIdentifier);
			if (sentJob) {
				sendResultUpwards(completionIndex);
			} else {
				/*
				 * We haven't sent any 'real' data prior to completing a stream.
				 * This in effect means we're sending an empty top level
				 * collection so we need to register empty collections for each
				 * output port with appropriate depth (by definition if we're
				 * streaming all outputs are collection types of some kind)
				 */
				receiveResult(getListRefMap(), new int[0]);
			}
		}

		private int[] composeIndex(int[] index) {
			if (index.length == 0)
				return jobEvent.getIndex();

			int len = jobEvent.getIndex().length;
			int[] newIndex = new int[len + index.length];
			arraycopy(jobEvent.getIndex(), 0, newIndex, 0, len);
			arraycopy(index, 0, newIndex, len, index.length);
			return newIndex;
		}

		private void sendResultUpwards(int[] completionIndex) {
			DispatchCompletionEvent c = new DispatchCompletionEvent(
					jobEvent.getOwningProcess(), composeIndex(completionIndex),
					jobEvent.getContext());
			getAbove().receiveResultCompletion(c);
		}

		private Map<String, T2Reference> getListRefMap() {
			Map<String, T2Reference> listRefMap = new HashMap<String, T2Reference>();
			ListService listService = refService.getListService();
			for (OutputPort op : asyncActivity.getOutputPorts())
				listRefMap.put(
						op.getName(),
						listService.registerEmptyList(op.getDepth(),
								jobEvent.getContext()).getId());
			return listRefMap;
		}

		@Override
		public void receiveResult(Map<String, T2Reference> data, int[] index) {
			ErrorDocumentService errorDocumentService = refService
					.getErrorDocumentService();

			if (index.length == 0) {
				// Final result, clean up monitor state
				monitorManager.deregisterNode(invocationProcessIdentifier);

				// Fill in missing port values
				for (OutputPort activityOutput : asyncActivity.getOutputPorts()) {
					String name = activityOutput.getName();
					if (!data.containsKey(name)) {
						ErrorDocument errorDoc = errorDocumentService
								.registerError("No data returned on port",
										activityOutput.getDepth(), null);
						data.put(name, errorDoc.getId());
					}
				}
			}

			/*
			 * Construct a new result map using the activity mapping (activity
			 * output name to processor output name)
			 */
			Map<String, T2Reference> resultMap = new HashMap<String, T2Reference>();
			for (String outputName : data.keySet()) {
				String processorOutputName = asyncActivity
						.getOutputPortMapping().get(outputName);
				if (processorOutputName != null) {
					T2Reference ref = data.get(outputName);
					// if (ref.containsErrors()) {
					// Processor p = getProcessor();
					// String message = "Processor '" +
					// getProcessor().getLocalName() + "' - Port '" +
					// processorOutputName + "'";
					// resultMap.put (processorOutputName,
					// errorDocumentService
					// .registerError(message , Collections.singleton(ref),
					// ref.getDepth(), null).getId());
					// } else {
					resultMap.put(processorOutputName, ref);
					// }
				}
			}
			/*
			 * Construct a new index array if the specified index is non zero
			 * length, otherwise just use the original job's index array (means
			 * we're not streaming)
			 */
			DispatchResultEvent resultEvent = new DispatchResultEvent(
					jobEvent.getOwningProcess(), composeIndex(index),
					jobEvent.getContext(), resultMap, index.length > 0);
			/*
			 * Push the modified data to the layer above in the dispatch stack
			 */
			getAbove().receiveResult(resultEvent);

			sentJob = true;
		}

		@Override
		public void requestRun(Runnable runMe) {
			String newThreadName = jobEvent.toString();
			Thread thread = new Thread(runMe, newThreadName);
			thread.setContextClassLoader(asyncActivity.getClass()
					.getClassLoader());
			thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					fail("Uncaught exception while invoking " + asyncActivity,
							e);
				}
			});
			thread.start();
		}

		public void overrideContext(InvocationContext newContext) {
			this.context = newContext;
		}
	}
}
