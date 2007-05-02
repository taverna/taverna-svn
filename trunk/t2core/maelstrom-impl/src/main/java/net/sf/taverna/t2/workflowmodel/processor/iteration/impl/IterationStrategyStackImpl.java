package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;

/**
 * Contains an ordered list of IterationStrategyImpl objects. The top of the list is
 * fed data directly, all other nodes are fed complete Job objects and
 * Completion events from the layer above. The bottom layer pushes events onto
 * the processor event queue to be consumed by the dispatch stack.
 * 
 * @author Tom Oinn
 * 
 */
public class IterationStrategyStackImpl implements IterationStrategyStack {

	private List<IterationStrategyImpl> strategies = new ArrayList<IterationStrategyImpl>();

	public void addStrategy(IterationStrategy is) {
		if (is instanceof IterationStrategyImpl) {
			IterationStrategyImpl isi = (IterationStrategyImpl)is;
			strategies.add(isi);
			isi.setIterationStrategyStack(this);
		}
		else {
			throw new WorkflowStructureException("IterationStrategyStackImpl can only hold IterationStrategyImpl objects");
		}
	}

	public List<IterationStrategyImpl> getStrategies() {
		return this.strategies;
	}
	
	public void receiveData(String inputPortName, String owningProcess,
			int[] indexArray, EntityIdentifier dataReference) {
		if (!strategies.isEmpty()) {
			strategies.get(0).receiveData(inputPortName, owningProcess,
					indexArray, dataReference);
		}
	}

	public void receiveCompletion(String inputPortName, String owningProcess,
			int[] completionArray) {
		if (!strategies.isEmpty()) {
			strategies.get(0).receiveCompletion(inputPortName, owningProcess, completionArray);
		}
	}

	public Element asXML() {
		Element strategyStackElement = new Element("iteration");
		for (IterationStrategyImpl is : strategies) {
			strategyStackElement.addContent(is.asXML());
		}
		return strategyStackElement;
	}
	public void configureFromElement(Element e) {
		strategies.clear();
		for (Object child : e.getChildren("strategy")) {
			Element strategyElement = (Element)child;
			IterationStrategyImpl strategy = new IterationStrategyImpl();
			strategy.configureFromXML(strategyElement);
			addStrategy(strategy);
		}
	}
	
	/**
	 * Return the layer below the specified one, or null if there is no lower
	 * layer
	 * 
	 * @return
	 */
	protected IterationStrategyImpl layerBelow(IterationStrategyImpl that) {
		int index = strategies.indexOf(that);
		if (index == (strategies.size() - 1)) {
			return null;
		} else {
			return strategies.get(index + 1);
		}
	}

	/**
	 * Called by the final iteration strategy to push events onto the
	 * dispatcher's queue
	 * 
	 * @param e
	 */
	protected void receiveEventFromStrategy(Event e) {
		// TODO - push events onto dispatch queue
	}

}
