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
package net.sf.taverna.t2.workflowmodel.processor.dispatch.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.IterationInternalEvent;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerControlBoundary;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerStateScoping;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.NotifiableLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.AbstractDispatchEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobQueueEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

import org.apache.log4j.Logger;

/**
 * The dispatch stack is responsible for consuming a queue of jobs from the
 * iteration strategy and dispatching those jobs through a stack based control
 * flow to an appropriate invocation target. Conceptually the queue and
 * description of activities enter the stack at the top, travel down to an
 * invocation layer at the bottom from which results, errors and completion
 * events rise back up to the top layer. Dispatch stack layers are stored as an
 * ordered list with index 0 being the top of the stack.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class DispatchStackImpl extends
		AbstractAnnotatedThing<DispatchStack> implements DispatchStack {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DispatchStackImpl.class);

	private Map<ProcessIdentifier, BlockingQueue<IterationInternalEvent<? extends IterationInternalEvent<?>>>> queues = new HashMap<ProcessIdentifier, BlockingQueue<IterationInternalEvent<? extends IterationInternalEvent<?>>>>();

	private List<DispatchLayer<?, ?>> dispatchLayers = new ArrayList<DispatchLayer<?, ?>>();

	/**
	 * Override to return the list of activities to be used by this dispatch
	 * stack.
	 * 
	 * @return list of activities to be used by jobs in this dispatch stack
	 */
	protected abstract List<? extends Activity<?>> getActivities();

	/**
	 * Called when an event (Completion or Job) hits the top of the dispatch
	 * stack and needs to be pushed out of the processor
	 * 
	 * @param e
	 */
	protected abstract void pushEvent(
			IterationInternalEvent<? extends IterationInternalEvent<?>> e);

	/**
	 * Called when a failure event has hit the top of the dispatch stack, this
	 * indicates trouble! the enclosing processor should handle the failure by
	 * propagating it out to the dataflow
	 */
	protected abstract void errorEventReceived(DispatchErrorEvent e);

	/**
	 * Called to determine whether all the preconditions for this dispatch stack
	 * are satisfied. Jobs with the given owningProcess are not processed by the
	 * dispatch stack until this returns true. Once it has returned true for a
	 * given owning process it must always return true, the precondition is not
	 * allowed to change from true back to false.
	 * 
	 * @param enclosingProcess
	 * @return whether all preconditions to invocation are satisfied.
	 */
	protected abstract boolean conditionsSatisfied(
			ProcessIdentifier enclosingProcess);

	/**
	 * Called when the specified owning process is finished with, that is to say
	 * all invocation has been performed and any layer state caches have been
	 * purged.
	 * 
	 * @param owningProcess
	 */
	protected abstract void finishedWith(ProcessIdentifier owningProcess,
			InvocationContext context);

	/**
	 * Defines the enclosing process name, usually Processor.getName() on the
	 * parent
	 */
	protected abstract String getProcessName();

	private DispatchLayer<Object, Object> topLayer = new TopLayer();

	/**
	 * Receive an event to be fed into the top layer of the dispatch stack for
	 * processing. This has the effect of creating a queue if there isn't one
	 * already, honouring any conditions that may be defined by an enclosing
	 * processor through the conditionsSatisfied() check method.
	 * <p>
	 * Because the condition checking logic must check against the enclosing
	 * process any attempt to call this method with an owning process without a
	 * colon in will fail with an index array out of bounds error. All owning
	 * process identifiers must resemble 'enclosingProcess:processorName' at the
	 * minimum.
	 * 
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	public void receiveEvent(IterationInternalEvent event) {
		if (!event.isActive()) {
			return;
		}
		BlockingQueue<IterationInternalEvent<? extends IterationInternalEvent<?>>> queue = null;
		ProcessIdentifier owningProcess = event.getOwningProcess();
		synchronized (queues) {
			ProcessIdentifier enclosingProcess = owningProcess.getParent();
			if (!queues.containsKey(owningProcess)) {
				queue = new LinkedBlockingQueue<IterationInternalEvent<? extends IterationInternalEvent<?>>>();
				queues.put(owningProcess, queue);
				queue.add(event);

				// If all preconditions are satisfied push the queue to the
				// dispatch layer
				if (conditionsSatisfied(enclosingProcess)) {
					DispatchJobQueueEvent dispatchEvent = new DispatchJobQueueEvent(
							owningProcess, event.getContext(), queue,
							getActivities());
					firstLayer().receiveJobQueue(dispatchEvent,
							callbackForLayer(0, dispatchEvent),
							getStateForLayer(0, dispatchEvent));
				}
			} else {
				queue = queues.get(owningProcess);
				queue.add(event);

				// If all preconditions are satisfied then notify the queue
				// addition to any NotifiableLayer instances. If the
				// preconditions are not satisfied the queue isn't visible to
				// the dispatch stack yet so do nothing.
				if (conditionsSatisfied(enclosingProcess)) {
					for (DispatchLayer layer : dispatchLayers) {
						if (layer instanceof NotifiableLayer) {
							((NotifiableLayer) layer).eventAdded(owningProcess);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private DispatchLayerCallback callbackForLayer(final int layerIndex,
			final AbstractDispatchEvent event) {
		// Get the layer and see if it's a control boundary. If it is then we
		// have to add code to the methods that send tokens down into the stack
		// to handle the mutation of the index array and owning process
		// identifier
		if (layerIndex < 0) {
			return null;
		}
		final DispatchLayer layer = dispatchLayers.get(layerIndex);
		final boolean controlBoundary = (layer instanceof DispatchLayerControlBoundary);
		DispatchLayerCallback callback = new DispatchLayerCallback() {

			public void clearLayerState() {
				clearStateForLayer(layerIndex, event);
			}

			public void sendError(DispatchErrorEvent error) {
				handleErrorEvent(error, layerIndex);
			}

			public void sendJob(DispatchJobEvent job) {
				if (controlBoundary) {
					DispatchLayerControlBoundary boundary = (DispatchLayerControlBoundary) layer;
					String owningProcessSuffix = boundary
							.getOutgoingProcessSuffix(job);
					int[] index = boundary.transformOutgoingIndex(job);
					DispatchJobEvent newJob = new DispatchJobEvent(job
							.getOwningProcess()
							.createChild(owningProcessSuffix), index, job
							.getContext(), job.getData(), job.getActivities());
					handleJobEvent(newJob, layerIndex);
				} else {
					handleJobEvent(job, layerIndex);
				}

			}

			public void sendJobQueue(DispatchJobQueueEvent queue) {
				if (controlBoundary) {
					DispatchLayerControlBoundary boundary = (DispatchLayerControlBoundary) layer;
					String owningProcessSuffix = boundary
							.getOutgoingProcessSuffix(queue);
					DispatchJobQueueEvent newQueue = new DispatchJobQueueEvent(
							queue.getOwningProcess().createChild(
									owningProcessSuffix), queue.getContext(),
							queue.getQueue(), queue.getActivities());
					handleJobQueueEvent(newQueue, layerIndex);
				} else {
					handleJobQueueEvent(queue, layerIndex);
				}

			}

			public void sendResult(DispatchResultEvent result) {
				handleResultEvent(result, layerIndex);
			}

			public void sendResultCompletion(DispatchCompletionEvent completion) {
				handleResultCompletionEvent(completion, layerIndex);
			}

			public DispatchErrorEvent createErrorEvent(String message,
					Throwable cause, DispatchErrorType type,
					Activity<?> sourceActivity) {
				return new DispatchErrorEvent(event.getOwningProcess(), event
						.getIndex(), event.getContext(), message, cause, type,
						sourceActivity);
			}

			public DispatchJobEvent createJobEvent(
					Map<String, T2Reference> data,
					List<? extends Activity<?>> activities) {
				return new DispatchJobEvent(event.getOwningProcess(), event
						.getIndex(), event.getContext(), data, activities);
			}

			public DispatchResultEvent createResultEvent(
					Map<String, T2Reference> data, boolean isStreaming) {
				return new DispatchResultEvent(event.getOwningProcess(), event
						.getIndex(), event.getContext(), data, isStreaming);
			}

		};
		return callback;
	}

	private Map<String, Object> layerStates = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	private Object getStateForLayer(int layerIndex, AbstractDispatchEvent event) {
		if (layerIndex < 0) {
			return null;
		}
		DispatchLayer layer = dispatchLayers.get(layerIndex);
		if (layer.getStateScope().equals(DispatchLayerStateScoping.NONE)) {
			return null;
		} else {
			String stateKey = getStateKey(layerIndex, layer, event);
			synchronized (layerStates) {
				if (!layerStates.containsKey(stateKey)) {
					Object newState = layer.createNewStateModel(getProcessor());
					layerStates.put(stateKey, newState);
					return newState;
				} else {
					return layerStates.get(stateKey);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Object getStateForLayer(int layerIndex, String processIdentifier) {
		DispatchLayer layer = dispatchLayers.get(layerIndex);
		if (!layer.getStateScope().equals(DispatchLayerStateScoping.PROCESS)) {
			return null;
		}
		String stateKey = processIdentifier + "-" + layerIndex;
		synchronized (layerStates) {
			if (!layerStates.containsKey(stateKey)) {
				Object newState = layer.createNewStateModel(getProcessor());
				layerStates.put(stateKey, newState);
				return newState;
			} else {
				return layerStates.get(stateKey);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void clearStateForLayer(int layerIndex, AbstractDispatchEvent event) {
		DispatchLayer layer = dispatchLayers.get(0);
		if (layer.getStateScope().equals(DispatchLayerStateScoping.NONE)) {
			return;
		} else {
			String stateKey = getStateKey(layerIndex, layer, event);
			synchronized (layerStates) {
				layerStates.remove(stateKey);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private String getStateKey(int layerIndex, DispatchLayer layer,
			AbstractDispatchEvent event) {
		if (layer.getStateScope().equals(DispatchLayerStateScoping.PROCESS)) {
			return event.getOwningProcess() + "-" + layerIndex;
		} else if (layer.getStateScope().equals(
				DispatchLayerStateScoping.ITERATION)) {
			StringBuffer sb = new StringBuffer();
			sb.append(event.getOwningProcess());
			for (int part : event.getIndex()) {
				sb.append(":" + part);
			}
			sb.append("-" + layerIndex);
			return sb.toString();
		} else {
			throw new IllegalStateException(
					"Can't generate a state key for a layer with no state");
		}
	}

	@SuppressWarnings("unchecked")
	private void handleJobQueueEvent(DispatchJobQueueEvent e,
			int sourceLayerIndex) {
		int targetIndex = sourceLayerIndex + 1;
		DispatchLayer targetLayer = dispatchLayers.get(targetIndex);
		targetLayer.receiveJobQueue(e, callbackForLayer(targetIndex, e),
				getStateForLayer(targetIndex, e));
	}

	@SuppressWarnings("unchecked")
	private void handleJobEvent(DispatchJobEvent e, int sourceLayerIndex) {
		int targetIndex = sourceLayerIndex + 1;
		DispatchLayer targetLayer = dispatchLayers.get(targetIndex);
		targetLayer.receiveJob(e, callbackForLayer(targetIndex, e),
				getStateForLayer(targetIndex, e));
	}

	@SuppressWarnings("unchecked")
	private void handleResultEvent(DispatchResultEvent e, int sourceLayerIndex) {
		// If the result is final and the source layer has a state model then
		// clear it up
		checkFinalStateClear(e, sourceLayerIndex);
		int targetIndex = sourceLayerIndex - 1;
		DispatchLayer targetLayer;
		if (targetIndex >= 0) {
			targetLayer = dispatchLayers.get(targetIndex);
		} else {
			targetLayer = topLayer;
		}
		// Check whether this is a control boundary and process the event
		// appropriately if so before passing it to the layer
		if (targetLayer instanceof DispatchLayerControlBoundary) {
			DispatchLayerControlBoundary boundary = (DispatchLayerControlBoundary) targetLayer;
			ProcessIdentifier process = e.getOwningProcess().getParent();
			int[] index = boundary.transformIncomingIndex(e);
			DispatchResultEvent newEvent = new DispatchResultEvent(process,
					index, e.getContext(), e.getData(), e.isStreamingEvent());
			targetLayer.receiveResult(newEvent, callbackForLayer(targetIndex,
					newEvent), getStateForLayer(targetIndex, newEvent));
		} else {
			targetLayer.receiveResult(e, callbackForLayer(targetIndex, e),
					getStateForLayer(targetIndex, e));
		}
	}

	@SuppressWarnings("unchecked")
	private void handleResultCompletionEvent(DispatchCompletionEvent e,
			int sourceLayerIndex) {
		checkFinalStateClear(e, sourceLayerIndex);
		int targetIndex = sourceLayerIndex - 1;
		DispatchLayer targetLayer;
		if (targetIndex >= 0) {
			targetLayer = dispatchLayers.get(targetIndex);
		} else {
			targetLayer = topLayer;
		}
		// Check whether this is a control boundary and process the event
		// appropriately if so before passing it to the layer
		if (targetLayer instanceof DispatchLayerControlBoundary) {
			DispatchLayerControlBoundary boundary = (DispatchLayerControlBoundary) targetLayer;
			ProcessIdentifier process = e.getOwningProcess().getParent();
			int[] index = boundary.transformIncomingIndex(e);
			DispatchCompletionEvent newEvent = new DispatchCompletionEvent(
					process, index, e.getContext());
			targetLayer.receiveResultCompletion(newEvent, callbackForLayer(
					targetIndex, newEvent), getStateForLayer(targetIndex,
					newEvent));
		} else {
			targetLayer.receiveResultCompletion(e, callbackForLayer(
					targetIndex, e), getStateForLayer(targetIndex, e));
		}
	}

	@SuppressWarnings("unchecked")
	private void handleErrorEvent(DispatchErrorEvent e, int sourceLayerIndex) {
		checkFinalStateClear(e, sourceLayerIndex);
		int targetIndex = sourceLayerIndex - 1;
		DispatchLayer targetLayer;
		if (targetIndex >= 0) {
			targetLayer = dispatchLayers.get(targetIndex);
		} else {
			targetLayer = topLayer;
		}
		// Check whether this is a control boundary and process the event
		// appropriately if so before passing it to the layer
		if (targetLayer instanceof DispatchLayerControlBoundary) {
			DispatchLayerControlBoundary boundary = (DispatchLayerControlBoundary) targetLayer;
			ProcessIdentifier process = e.getOwningProcess().getParent();
			int[] index = boundary.transformIncomingIndex(e);
			DispatchErrorEvent newEvent = new DispatchErrorEvent(process,
					index, e.getContext(), e.getMessage(), e.getCause(), e
							.getFailureType(), e.getFailedActivity());
			targetLayer.receiveError(newEvent, callbackForLayer(targetIndex,
					newEvent), getStateForLayer(targetIndex, newEvent));
		} else {
			targetLayer.receiveError(e, callbackForLayer(targetIndex, e),
					getStateForLayer(targetIndex, e));
		}
	}

	@SuppressWarnings("unchecked")
	private void checkFinalStateClear(AbstractDispatchEvent e,
			int sourceLayerIndex) {
		if (e instanceof DispatchResultEvent) {
			DispatchResultEvent dre = (DispatchResultEvent) e;
			if (dre.isStreamingEvent()) {
				return;
			}
		}
		DispatchLayer layer = dispatchLayers.get(sourceLayerIndex);
		if (e.isFinal()
				&& layer.getStateScope().equals(
						DispatchLayerStateScoping.PROCESS)) {
			clearStateForLayer(sourceLayerIndex, e);
		} else if (layer.getStateScope().equals(
				DispatchLayerStateScoping.ITERATION)) {
			clearStateForLayer(sourceLayerIndex, e);
		}
	}

	/**
	 * Called when a set of conditions which were unsatisfied in the context of
	 * a given owning process become satisfied. At this point any jobs in the
	 * queue for that owning process identifier should be pushed through to the
	 * dispatch mechanism. As the queue itself will not have been pushed through
	 * at this point this just consists of messaging the first layer with the
	 * queue and activity set.
	 * 
	 * @param owningProcess
	 */
	@SuppressWarnings("unchecked")
	public void satisfyConditions(ProcessIdentifier enclosingProcess) {
		if (conditionsSatisfied(enclosingProcess)) {
			ProcessIdentifier owningProcess = enclosingProcess
					.createChild(getProcessName());
			synchronized (queues) {
				if (queues.containsKey(owningProcess)) {
					/**
					 * At least one event has been received with this process ID
					 * and a queue exists for it.
					 */
					DispatchJobQueueEvent event = new DispatchJobQueueEvent(
							owningProcess, queues.get(owningProcess).peek()
									.getContext(), queues.get(owningProcess),
							getActivities());
					firstLayer().receiveJobQueue(event,
							callbackForLayer(0, event),
							getStateForLayer(0, event));
				} else {
					/**
					 * Do nothing, if the conditions are satisfied before any
					 * jobs are received this mechanism is effectively redundant
					 * and the normal notification system for the events will
					 * let everything work through as per usual
					 */
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack#getLayers
	 * ()
	 */
	public List<DispatchLayer<?, ?>> getLayers() {
		return Collections.unmodifiableList(this.dispatchLayers);
	}

	public void addLayer(DispatchLayer<?, ?> newLayer) {
		dispatchLayers.add(newLayer);
	}

	public void addLayer(DispatchLayer<?, ?> newLayer, int index) {
		dispatchLayers.add(index, newLayer);
	}

	public int removeLayer(DispatchLayer<?, ?> layer) {
		int priorIndex = dispatchLayers.indexOf(layer);
		dispatchLayers.remove(layer);
		return priorIndex;
	}

	/**
	 * Return the layer above (lower index!) the specified layer, or a reference
	 * to the internal top layer dispatch layer if there is no layer above the
	 * specified one. Remember - input data and activities go down, results,
	 * errors and completion events bubble back up the dispatch stack.
	 * <p>
	 * The top layer within the dispatch stack is always invisible and is held
	 * within the DispatchStackImpl object itself, being used to route data out
	 * of the entire stack
	 * 
	 * @param layer
	 * @return
	 */
	public DispatchLayer<?, ?> layerAbove(DispatchLayer<?, ?> layer) {
		int layerIndex = dispatchLayers.indexOf(layer);
		if (layerIndex > 0) {
			return dispatchLayers.get(layerIndex - 1);
		} else if (layerIndex == 0) {
			return topLayer;
		} else {
			return null;
		}
	}

	/**
	 * Return the layer below (higher index) the specified layer, or null if
	 * there are no layers below this one
	 */
	public DispatchLayer<?, ?> layerBelow(DispatchLayer<?, ?> layer) {
		int layerIndex = dispatchLayers.indexOf(layer);
		if (layerIndex < dispatchLayers.size() - 1) {
			return dispatchLayers.get(layerIndex + 1);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected DispatchLayer firstLayer() {
		return dispatchLayers.get(0);
	}

	protected class TopLayer extends AbstractDispatchLayer<Object, Object> {

		@Override
		public void receiveResult(DispatchResultEvent resultEvent,
				DispatchLayerCallback callback, Object state) {
			DispatchStackImpl.this.pushEvent(new Job(resultEvent
					.getOwningProcess(), resultEvent.getIndex(), resultEvent
					.getData(), resultEvent.getContext()));
			if (resultEvent.getIndex().length == 0) {
				sendCachePurge(resultEvent.getOwningProcess(), resultEvent
						.getContext());
			}
		}

		@Override
		public void receiveError(DispatchErrorEvent errorEvent,
				DispatchLayerCallback callback, Object state) {
			if (!errorEvent.isActive()) {
				return;
			}
			errorEventReceived(errorEvent);
			if (errorEvent.getIndex().length == 0) {
				// System.out.println(" - sent purge");
				sendCachePurge(errorEvent.getOwningProcess(), errorEvent
						.getContext());
			}
		}

		@Override
		public void receiveResultCompletion(
				DispatchCompletionEvent completionEvent,
				DispatchLayerCallback callback, Object state) {
			if (!completionEvent.isActive()) {
				return;
			}
			Completion c = new Completion(completionEvent.getOwningProcess(),
					completionEvent.getIndex(), completionEvent.getContext());
			DispatchStackImpl.this.pushEvent(c);
			if (c.isFinal()) {
				sendCachePurge(c.getOwningProcess(), c.getContext());
			}
		}

		private void sendCachePurge(ProcessIdentifier owningProcess,
				InvocationContext context) {
			for (DispatchLayer<?, ?> layer : dispatchLayers) {
				layer.finishedWith(owningProcess);
			}
			DispatchStackImpl.this.finishedWith(owningProcess, context);
		}

		public void configure(Object config) {
			// 
		}

		public Object getConfiguration() {
			return null;
		}

		public Object createNewStateModel(Processor p) {
			return null;
		}

		public DispatchLayerStateScoping getStateScope() {
			return DispatchLayerStateScoping.NONE;
		}
	}

}
