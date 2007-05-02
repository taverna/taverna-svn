package net.sf.taverna.t2.workflowmodel.processor.dispatch.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.NotifiableLayer;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * The dispatch stack is responsible for consuming a queue of jobs from the
 * iteration strategy and dispatching those jobs through a stack based control
 * flow to an appropriate invocation target. Conceptually the queue and
 * description of services enter the stack at the top, travel down to an
 * invocation layer at the bottom from which results, errors and completion
 * events rise back up to the top layer. Dispatch stack layers are stored as an
 * ordered list with index 0 being the top of the stack.
 * 
 * @author Tom Oinn
 * 
 */
public class DispatchStackImpl implements DispatchStack {

	private Map<String, BlockingQueue<Event>> queues;

	private List<Service> services;

	public Element asXML() throws JDOMException, IOException {
		Element stackElement = new Element("dispatch");
		for (DispatchLayer layer : dispatchLayers) {
			stackElement.addContent(Tools.dispatchLayerAsXML(layer));
		}
		return stackElement;
	}

	@SuppressWarnings("unchecked")
	public void configureFromElement(Element e)
			throws ArtifactNotFoundException, ArtifactStateException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {
		dispatchLayers.clear();
		for (Element layerElement : (List<Element>) e.getChildren("layer")) {
			DispatchLayer layer = Tools.buildDispatchLayer(layerElement);
			dispatchLayers.add(layer);
		}
	}

	private DispatchLayer topLayer = new AbstractDispatchLayer<Object>() {
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
		public void receiveError(String owningProcess, int[] errorIndex,
				String errorMessage, Throwable detail) {
			if (errorIndex.length == 0) {
				sendCachePurge(owningProcess);
			}
		}

		public void receiveResultCompletion(Completion c) {
			DispatchStackImpl.this.pushEvent(c);
			if (c.isFinal()) {
				sendCachePurge(c.getOwningProcess());
			}
		}

		private void sendCachePurge(String owningProcess) {
			for (DispatchLayer layer : dispatchLayers) {
				layer.finishedWith(owningProcess);
			}
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
	 * Called when an event (Completion or Job) hits the top of the dispatch
	 * stack and needs to be pushed out of the processor
	 * 
	 * @param e
	 */
	protected void pushEvent(Event e) {
		//
	}

	public DispatchStackImpl(List<Service> services) {
		this.services = services;
		queues = new HashMap<String, BlockingQueue<Event>>();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.service.dispatch.DispatchStack#receiveEvent(net.sf.taverna.t2.invocation.Event)
	 */
	@SuppressWarnings("unchecked")
	public void receiveEvent(Event e) {
		BlockingQueue<Event> queue = null;
		String owningProcess = e.getOwningProcess();
		synchronized (queues) {
			if (queues.containsKey(owningProcess) == false) {
				queue = new LinkedBlockingQueue<Event>();
				queues.put(owningProcess, queue);
				queue.add(e);
				dispatchLayers.get(0).receiveJobQueue(owningProcess, queue,
						(List<Service>)services);
			} else {
				queue = queues.get(e.getOwningProcess());
				queue.add(e);
				for (DispatchLayer layer : dispatchLayers) {
					if (layer instanceof NotifiableLayer) {
						((NotifiableLayer) layer).eventAdded(owningProcess);
					}
				}
			}
		}
	}

	private List<DispatchLayer> dispatchLayers = new ArrayList<DispatchLayer>();

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.service.dispatch.DispatchStack#getLayers()
	 */
	public List<DispatchLayer> getLayers() {
		return this.dispatchLayers;
	}

	public void addLayer(DispatchLayer newLayer) {
		dispatchLayers.add(newLayer);
		newLayer.setDispatchStack(this);
	}
	
	public void addLayer(DispatchLayer newLayer, int index) {
		dispatchLayers.add(index, newLayer);
		newLayer.setDispatchStack(this);
	}
	
	public int removeLayer(DispatchLayer layer) {
		int priorIndex = dispatchLayers.indexOf(layer);
		dispatchLayers.remove(layer);		
		return priorIndex;
	}

	/**
	 * Return the layer above (lower index!) the specified layer, or a reference
	 * to the internal top layer dispatch layer if there is no layer above the
	 * specified one. Remember - input data and services go down, results,
	 * errors and completion events bubble back up the dispatch stack.
	 * <p>
	 * The top layer within the dispatch stack is always invisible and is held
	 * within the DispatchStackImpl object itself, being used to route data out of
	 * the entire stack
	 * 
	 * @param layer
	 * @return
	 */
	public DispatchLayer layerAbove(DispatchLayer layer) {
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
	public DispatchLayer layerBelow(DispatchLayer layer) {
		int layerIndex = dispatchLayers.indexOf(layer);
		if (layerIndex < dispatchLayers.size() - 1) {
			return dispatchLayers.get(layerIndex + 1);
		} else {
			return null;
		}
	}

	

	

}
