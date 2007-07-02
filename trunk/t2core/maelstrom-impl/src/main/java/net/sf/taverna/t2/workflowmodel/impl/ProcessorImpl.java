package net.sf.taverna.t2.workflowmodel.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.annotation.impl.AbstractMutableAnnotatedThing;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.FilteringInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;
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
public final class ProcessorImpl extends AbstractMutableAnnotatedThing implements Processor {

	protected List<ConditionImpl> conditions = new ArrayList<ConditionImpl>();

	protected List<ConditionImpl> controlledConditions = new ArrayList<ConditionImpl>();

	protected List<ProcessorInputPortImpl> inputPorts = new ArrayList<ProcessorInputPortImpl>();

	protected List<ProcessorOutputPortImpl> outputPorts = new ArrayList<ProcessorOutputPortImpl>();

	protected List<Service<?>> serviceList = new ArrayList<Service<?>>();

	protected AbstractCrystalizer crystalizer;

	protected DispatchStackImpl dispatchStack;

	protected IterationStrategyStackImpl iterationStack;

	private static int pNameCounter = 0;

	protected String name;

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
				// System.out.println("Sending event to dispatch stack "+e);
				dispatchStack.receiveEvent(e);
			}
		};
		iterationStack.addStrategy(new IterationStrategyImpl());

		// Configure dispatch stack to push output events to the crystalizer
		dispatchStack = new DispatchStackImpl() {

			protected String getProcessName() {
				return ProcessorImpl.this.name;
			}

			/**
			 * Called when an event bubbles out of the top of the dispatch
			 * stack. In this case we pass it into the crystalizer.
			 */
			@Override
			protected void pushEvent(Event e) {
				// System.out.println("Sending event to crystalizer : "+e);

				crystalizer.baseListDepth = getEmptyListDepth(e
						.getOwningProcess());
				crystalizer.receiveEvent(e);
			}

			/**
			 * Iterate over all the preconditions and return true if and only if
			 * all are satisfied for the given process identifier.
			 */
			@Override
			protected boolean conditionsSatisfied(String owningProcess) {
				for (Condition c : conditions) {
					if (c.isSatisfied(owningProcess) == false) {
						return false;
					}
				}
				return true;
			}

			@Override
			protected List<Service<?>> getServices() {
				return ProcessorImpl.this.getServiceList();
			}

			/**
			 * We've finished here, set the satisfied property on any controlled
			 * condition objects to true and notify the targets.
			 */
			@Override
			protected void finishedWith(String owningProcess) {
				if (controlledConditions.isEmpty() == false) {
					String enclosingProcess = owningProcess.substring(0,
							owningProcess.lastIndexOf(':'));
					for (ConditionImpl ci : controlledConditions) {
						ci.satisfy(enclosingProcess);
						ci.getTarget().getDispatchStack().satisfyConditions(
								enclosingProcess);
					}
				}

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

	public void fire(String enclosingProcess) {
		Job newJob = new Job(enclosingProcess + ":" + this.name, new int[0],
				new HashMap<String, EntityIdentifier>());
		dispatchStack.receiveEvent(newJob);
	}

	public List<? extends Condition> getPreconditionList() {
		return Collections.unmodifiableList(conditions);
	}

	public List<? extends Condition> getControlledPreconditionList() {
		return Collections.unmodifiableList(controlledConditions);
	}

	public DispatchStackImpl getDispatchStack() {
		return dispatchStack;
	}

	public IterationStrategyStackImpl getIterationStrategy() {
		return iterationStack;
	}

	public List<? extends FilteringInputPort> getInputPorts() {
		return Collections.unmodifiableList(inputPorts);
	}

	public List<? extends OutputPort> getOutputPorts() {
		return Collections.unmodifiableList(outputPorts);
	}

	public List<Service<?>> getServiceList() {
		return Collections.unmodifiableList(serviceList);
	}

	protected void setName(String newName) {
		this.name = newName;
	}

	public String getLocalName() {
		return this.name;
	}

	public int getEmptyListDepth(String owningProcess) {
		return this.iterationStack.getIterationDepth();
	}

	public void forgetDepthFor(String owningProcess) {
		// TODO Auto-generated method stub

	}

}
