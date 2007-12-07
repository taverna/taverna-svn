package net.sf.taverna.t2.workflowmodel.processor.dispatch.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.invocation.IterationInternalEvent;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.NotifiableLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobQueueEvent;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

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
 * 
 */
public abstract class DispatchStackImpl extends
		AbstractAnnotatedThing<DispatchStack> implements DispatchStack {

	private static Logger logger = Logger.getLogger(DispatchStackImpl.class);

	private Map<String, BlockingQueue<IterationInternalEvent>> queues = new HashMap<String, BlockingQueue<IterationInternalEvent>>();

	private List<DispatchLayer<?>> dispatchLayers = new ArrayList<DispatchLayer<?>>();

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
	protected abstract void pushEvent(Event e);

	/**
	 * Called to determine whether all the preconditions for this dispatch stack
	 * are satisfied. Jobs with the given owningProcess are not processed by the
	 * dispatch stack until this returns true. Once it has returned true for a
	 * given owning process it must always return true, the precondition is not
	 * allowed to change from true back to false.
	 * 
	 * @param owningProcess
	 * @return whether all preconditions to invocation are satisfied.
	 */
	protected abstract boolean conditionsSatisfied(String owningProcess);

	/**
	 * Called when the specified owning process is finished with, that is to say
	 * all invocation has been performed and any layer state caches have been
	 * purged.
	 * 
	 * @param owningProcess
	 */
	protected abstract void finishedWith(String owningProcess);

	/**
	 * Defines the enclosing process name, usually Processor.getName() on the
	 * parent
	 */
	protected abstract String getProcessName();

	/**
	 * XML Serialization
	 * 
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public Element asXML() throws JDOMException, IOException {
		Element stackElement = new Element("dispatch");
		for (DispatchLayer<?> layer : dispatchLayers) {
			stackElement.addContent(Tools.dispatchLayerAsXML(layer));
		}
		return stackElement;
	}

	/**
	 * XML Deserialization
	 * 
	 * @param e
	 * @throws ArtifactNotFoundException
	 * @throws ArtifactStateException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public void configureFromElement(Element e)
			throws ArtifactNotFoundException, ArtifactStateException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		dispatchLayers.clear();
		for (Element layerElement : (List<Element>) e.getChildren("layer")) {
			DispatchLayer layer = Tools.buildDispatchLayer(layerElement);
			dispatchLayers.add(layer);
		}
	}

	private DispatchLayer<Object> topLayer = new AbstractDispatchLayer<Object>() {
		public void receiveResult(Job j) {
			DispatchStackImpl.this.pushEvent(j);
			if (j.getIndex().length == 0) {
				sendCachePurge(j.getOwningProcess());
			}
		}

		// TODO - implement top level error handling, if an error bubbles up to
		// the top layer of the dispatch stack it's trouble and probably fails
		// this process
		@Override
		public void receiveError(DispatchErrorEvent errorEvent) {
			System.out.println("Error : " + errorEvent.getOwningProcess() + " "
					+ errorEvent.getMessage() + " "
					+ Thread.currentThread().getName()
					+ queues.get(errorEvent.getOwningProcess()).size());
			logger.error("Error received in dispatch stack on owningProcess:"
					+ errorEvent.getOwningProcess() + ", msg:"
					+ errorEvent.getMessage(), errorEvent.getCause());
			if (errorEvent.getIndex().length == 0) {

				// System.out.println(" - sent purge");
				sendCachePurge(errorEvent.getOwningProcess());
			}
		}

		@Override
		public void receiveResultCompletion(
				DispatchCompletionEvent completionEvent) {
			Completion c = new Completion(completionEvent.getOwningProcess(),
					completionEvent.getIndex(), completionEvent.getContext());
			DispatchStackImpl.this.pushEvent(c);
			if (c.isFinal()) {
				sendCachePurge(c.getOwningProcess());
			}
		}

		private void sendCachePurge(String owningProcess) {
			for (DispatchLayer<?> layer : dispatchLayers) {
				layer.finishedWith(owningProcess);
			}
			DispatchStackImpl.this.finishedWith(owningProcess);
		}

		public void configure(Object config) {
			// TODO Auto-generated method stub

		}

		public Object getConfiguration() {
			// TODO Auto-generated method stub
			return null;
		}
	};

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
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	public void receiveEvent(IterationInternalEvent e) {
		BlockingQueue<IterationInternalEvent> queue = null;
		String owningProcess = e.getOwningProcess();
		synchronized (queues) {
			String enclosingProcess = owningProcess.substring(0, owningProcess
					.lastIndexOf(':'));
			if (queues.containsKey(owningProcess) == false) {
				queue = new LinkedBlockingQueue<IterationInternalEvent>();
				queues.put(owningProcess, queue);
				queue.add(e);

				// If all preconditions are satisfied push the queue to the
				// dispatch layer
				if (conditionsSatisfied(enclosingProcess)) {
					dispatchLayers.get(0).receiveJobQueue(
							new DispatchJobQueueEvent(owningProcess, e
									.getContext(), queue, getActivities()));
				}
			} else {
				queue = queues.get(owningProcess);
				queue.add(e);

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
	public void satisfyConditions(String enclosingProcess) {
		if (conditionsSatisfied(enclosingProcess)) {
			String owningProcess = enclosingProcess + ":" + getProcessName();
			synchronized (queues) {
				if (queues.containsKey(owningProcess)) {
					// At least one event has been received with this process ID
					// and
					// a queue exists for it.

					dispatchLayers.get(0)
							.receiveJobQueue(
									new DispatchJobQueueEvent(owningProcess,
											queues.get(owningProcess).peek()
													.getContext(), queues
													.get(owningProcess),
											getActivities()));
				} else {
					// Do nothing, if the conditions are satisfied before any
					// jobs
					// are received this mechanism is effectively redundant and
					// the
					// normal notification system for the events will let
					// everything
					// work through as per usual
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack#getLayers()
	 */
	public List<DispatchLayer<?>> getLayers() {
		return Collections.unmodifiableList(this.dispatchLayers);
	}

	public void addLayer(DispatchLayer<?> newLayer) {
		dispatchLayers.add(newLayer);
		newLayer.setDispatchStack(this);
	}

	public void addLayer(DispatchLayer<?> newLayer, int index) {
		dispatchLayers.add(index, newLayer);
		newLayer.setDispatchStack(this);
	}

	public int removeLayer(DispatchLayer<?> layer) {
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
	public DispatchLayer<?> layerAbove(DispatchLayer<?> layer) {
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
	public DispatchLayer<?> layerBelow(DispatchLayer<?> layer) {
		int layerIndex = dispatchLayers.indexOf(layer);
		if (layerIndex < dispatchLayers.size() - 1) {
			return dispatchLayers.get(layerIndex + 1);
		} else {
			return null;
		}
	}

}
