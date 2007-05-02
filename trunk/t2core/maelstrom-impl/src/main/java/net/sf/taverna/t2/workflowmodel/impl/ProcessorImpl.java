package net.sf.taverna.t2.workflowmodel.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.FilteringInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;
import net.sf.taverna.t2.workflowmodel.processor.service.ServiceConfigurationException;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Implementation of Processor
 * 
 * @author Tom Oinn
 * 
 */
public final class ProcessorImpl implements Processor {

	protected List<ProcessorInputPortImpl> inputPorts = new ArrayList<ProcessorInputPortImpl>();

	protected List<ProcessorOutputPortImpl> outputPorts = new ArrayList<ProcessorOutputPortImpl>();

	protected List<Service<?>> serviceList = new ArrayList<Service<?>>();

	protected AbstractCrystalizer crystalizer;

	protected DispatchStackImpl dispatchStack;

	protected IterationStrategyStackImpl iterationStack;

	private static int pNameCounter = 0;

	private String name;

	/**
	 * Create a new processor implementation with default blank iteration
	 * strategy and dispatch stack
	 * 
	 */
	
	@SuppressWarnings("unchecked")
	public ProcessorImpl() {

		// Set a default name
		name = "UnnamedProcessor" + (pNameCounter++);

		// Create iteration stack, configure it to send jobs and completion
		// events to the dispatch stack.
		iterationStack = new IterationStrategyStackImpl() {
			protected void receiveEventFromStrategy(Event e) {
				dispatchStack.receiveEvent(e);
			}
		};
		iterationStack.addStrategy(new IterationStrategyImpl());

		// Configure dispatch stack to push output events to the crystalizer
		List serviceList = this.serviceList;
		dispatchStack = new DispatchStackImpl(serviceList) {
			protected void pushEvent(Event e) {
				crystalizer.receiveEvent(e);
			}
		};

		// Configure crystalizer to send realized events to the output ports
		crystalizer = new ProcessorCrystalizerImpl(this);

	}

	
	
	/* Serialization handling */
	
	public Element asXML() throws JDOMException, IOException {
		Element e = new Element("processor");
		e.setAttribute("name", name);
		Element ipElement = new Element("inputs");
		for (InputPort ip : inputPorts) {
			Element port = new Element("port");
			port.setAttribute("name", ip.getName());
			port.setAttribute("depth", ip.getDepth() + "");
			ipElement.addContent(port);
		}
		e.addContent(ipElement);
		Element opElement = new Element("outputs");
		for (OutputPort op : outputPorts) {
			Element port = new Element("port");
			port.setAttribute("name", op.getName());
			port.setAttribute("depth", op.getDepth() + "");
			port.setAttribute("grain", op.getGranularDepth() + "");
			opElement.addContent(port);
		}
		e.addContent(opElement);
		e.addContent(iterationStack.asXML());
		e.addContent(dispatchStack.asXML());
		Element servicesElement = new Element("services");
		for (Service s : serviceList) {
			Element serviceElement = Tools.serviceAsXML(s);
			servicesElement.addContent(serviceElement);
		}
		e.addContent(servicesElement);
		return e;
	}

	@SuppressWarnings("unchecked")
	public void configureFromElement(Element e)
			throws ArtifactNotFoundException, ArtifactStateException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, ServiceConfigurationException {
		setName(e.getAttributeValue("name"));
		Element ipElement = e.getChild("inputs");
		for (Element portElement : (List<Element>) ipElement
				.getChildren("port")) {
			inputPorts.add(new ProcessorInputPortImpl(this, portElement
					.getAttributeValue("name"), Integer.parseInt(portElement
					.getAttributeValue("depth"))));
		}
		Element opElement = e.getChild("outputs");
		for (Element portElement : (List<Element>) opElement
				.getChildren("port")) {
			outputPorts.add(new ProcessorOutputPortImpl(portElement
					.getAttributeValue("name"), Integer.parseInt(portElement
					.getAttributeValue("depth")), Integer.parseInt(portElement
					.getAttributeValue("grain"))));
		}
		dispatchStack.configureFromElement(e.getChild("dispatch"));
		iterationStack.configureFromElement(e.getChild("iteration"));
		serviceList.clear();
		for (Element serviceElement : (List<Element>) e.getChild("services")
				.getChildren("service")) {
			serviceList.add(Tools.buildService(serviceElement));
		}
	}

	
	
	/* Utility methods */
		
	// Used as temp caches for output and input names to port instances
	private Map<String, ProcessorInputPortImpl> inputPortNameCache = new HashMap<String, ProcessorInputPortImpl>();
	protected ProcessorInputPortImpl getInputPortWithName(String name) {
		synchronized (inputPortNameCache) {
			if (inputPortNameCache.isEmpty()) {
				for (ProcessorInputPortImpl p : inputPorts) {
					String portName = p.getName();
					inputPortNameCache.put(portName, p);
				}
			}
		}
		return inputPortNameCache.get(name);
	}

	// Used as temp caches for output and input names to port instances
	private Map<String, ProcessorOutputPortImpl> outputPortNameCache = new HashMap<String, ProcessorOutputPortImpl>();
	protected ProcessorOutputPortImpl getOutputPortWithName(String name) {
		synchronized (outputPortNameCache) {
			if (outputPortNameCache.isEmpty()) {
				for (ProcessorOutputPortImpl p : outputPorts) {
					String portName = p.getName();
					outputPortNameCache.put(portName, p);
				}
			}
		}
		return outputPortNameCache.get(name);
	}

	
	
	/* Implementations of Processor interface */
		
	public DispatchStackImpl getDispatchStack() {
		return dispatchStack;
	}

	public IterationStrategyStackImpl getIterationStrategy() {
		return iterationStack;
	}

	public List<? extends FilteringInputPort> getInputPorts() {
		return inputPorts;
	}

	public List<? extends OutputPort> getOutputPorts() {
		return outputPorts;
	}

	public List<Service<?>> getServiceList() {
		return this.serviceList;
	}

	protected void setName(String newName) {
		this.name = newName;
	}

	public String getLocalName() {
		return this.name;
	}

}
