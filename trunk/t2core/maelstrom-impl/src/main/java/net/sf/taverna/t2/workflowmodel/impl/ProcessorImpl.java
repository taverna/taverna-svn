package net.sf.taverna.t2.workflowmodel.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.impl.AbstractMutableAnnotatedThing;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationTypeMismatchException;
import net.sf.taverna.t2.workflowmodel.processor.iteration.MissingIterationInputException;
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
public final class ProcessorImpl extends AbstractMutableAnnotatedThing
		implements Processor {

	protected List<ConditionImpl> conditions = new ArrayList<ConditionImpl>();

	protected List<ConditionImpl> controlledConditions = new ArrayList<ConditionImpl>();

	protected List<ProcessorInputPortImpl> inputPorts = new ArrayList<ProcessorInputPortImpl>();

	protected List<ProcessorOutputPortImpl> outputPorts = new ArrayList<ProcessorOutputPortImpl>();

	protected List<Service<?>> serviceList = new ArrayList<Service<?>>();

	protected List<AbstractMutableAnnotatedThing> serviceAnnotations = new ArrayList<AbstractMutableAnnotatedThing>();

	protected AbstractCrystalizer crystalizer;

	protected DispatchStackImpl dispatchStack;

	protected IterationStrategyStackImpl iterationStack;

	private static int pNameCounter = 0;

	protected String name;
	
	public transient int resultWrappingDepth = -1;

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
	
	/**
	 * When called this method configures input port filters and the
	 * crystalizer, pushing cardinality information into outgoing datalinks.
	 * 
	 * @return true if the typecheck was successful
	 * @throws IterationTypeMismatchException
	 *             if the typing occured but didn't match because of an
	 *             iteration mismatch
	 */
	public boolean doTypeCheck() throws IterationTypeMismatchException {
		// Check whether all our input ports have inbound links
		Map<String, Integer> inputDepths = new HashMap<String, Integer>();
		for (ProcessorInputPortImpl input : inputPorts) {
			if (input.getIncomingLink() == null) {
				return false;
			} else {
				if (input.getIncomingLink().getResolvedDepth() == -1) {
					// Incoming link hasn't been resolved yet, can't do this
					// processor at the moment
					return false;
				}
				// Get the conceptual resolved depth of the datalink
				inputDepths.put(input.getName(), input.getIncomingLink()
						.getResolvedDepth());
				// Configure the filter with the finest grained item from the
				// link source
				input.setFilterDepth(input.getIncomingLink().getSource()
						.getGranularDepth());
			}
		}
		// Got here so we have all the inputs, now test whether the iteration
		// strategy typechecks correctly
		try {
			this.resultWrappingDepth = iterationStack
					.getIterationDepth(inputDepths);
			for (ProcessorOutputPortImpl output : outputPorts) {
				for (DatalinkImpl outgoingLink : output.outgoingLinks) {
					// Set the resolved depth on each output edge
					outgoingLink.setResolvedDepth(this.resultWrappingDepth
							+ output.getDepth());
				}
			}

		} catch (MissingIterationInputException e) {
			// This should never happen as we only get here if we've already
			// checked that all the inputs have been provided. If it does happen
			// we've got some deeper issues.
			e.printStackTrace();
			return false;
		}

		// If we get to here everything has been configured appropriately
		return true;
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
					.getAttributeValue("grain")), this));
		}
		dispatchStack.configureFromElement(e.getChild("dispatch"));
		iterationStack.configureFromElement(e.getChild("iteration"));
		serviceList.clear();
		for (Element serviceElement : (List<Element>) e.getChild("services")
				.getChildren("service")) {
			serviceList.add(Tools.buildService(serviceElement));
			serviceAnnotations.add(new AbstractMutableAnnotatedThing());
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

	public List<? extends ProcessorInputPort> getInputPorts() {
		return Collections.unmodifiableList(inputPorts);
	}

	public List<? extends ProcessorOutputPort> getOutputPorts() {
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

	public Annotated getAnnotationForService(Service<?> service) {
		int index = serviceList.indexOf(service);
		if (index >= 0) {
			return serviceAnnotations.get(index);
		}
		return null;
	}

}
