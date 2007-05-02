package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.TreeNode;

import org.jdom.Element;

import net.sf.taverna.t2.cloudone.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.ContextManager;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.iteration.AbstractIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.PrefixDotProduct;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;

/**
 * A single layer of iteration strategy, consuming individual named inputs and
 * combining these into Job objects to be consumed by the dispatch stack
 * 
 * @author Tom Oinn
 * 
 */
public class IterationStrategyImpl implements IterationStrategy {

	Set<NamedInputPortNode> inputs;

	private boolean wrapping = false;

	protected IterationStrategyStackImpl stack = null;

	private TerminalNode terminal = new TerminalNode();

	class TerminalNode extends AbstractIterationStrategyNode {

		public void receiveCompletion(int inputIndex, Completion completion) {
			if (wrapping) {
				pushEvent(completion.popIndex());
			} else {
				pushEvent(completion);
			}

		}

		public void receiveJob(int inputIndex, Job newJob) {
			if (wrapping) {
				pushEvent(newJob.popIndex());
			} else {
				pushEvent(newJob);
			}
		}

		public void receiveBypassCompletion(Completion completion) {
			pushEvent(completion);
		}

		private void pushEvent(Event e) {
			// System.out.println("Tnode : "+e);
			if (stack != null) {
				IterationStrategyImpl below = stack
						.layerBelow(IterationStrategyImpl.this);
				if (below == null) {
					stack.receiveEventFromStrategy(e);
				} else {
					below.receiveEvent(e);
				}
			}
		}

	}

	public IterationStrategyImpl() {
		this.inputs = new HashSet<NamedInputPortNode>();
	}

	public TerminalNode getTerminal() {
		return this.terminal;
	}

	/**
	 * Get the XML element defining the state of this iteration strategy
	 * 
	 * @return
	 */
	protected Element asXML() {
		Element strategyElement = new Element("strategy");
		if (terminal.getChildCount() > 0) {
			AbstractIterationStrategyNode node = (AbstractIterationStrategyNode) (terminal
					.getChildAt(0));
			strategyElement.addContent(elementForNode(node));
		}
		return strategyElement;
	}

	private static Element elementForNode(AbstractIterationStrategyNode node) {
		Element nodeElement = null;
		if (node instanceof DotProduct) {
			nodeElement = new Element("dot");
		} else if (node instanceof CrossProduct) {
			nodeElement = new Element("cross");
		} else if (node instanceof PrefixDotProduct) {
			nodeElement = new Element("prefix");
		} else if (node instanceof NamedInputPortNode) {
			NamedInputPortNode nipn = (NamedInputPortNode) node;
			nodeElement = new Element("port");
			nodeElement.setAttribute("name", nipn.getPortName());
			nodeElement.setAttribute("depth", nipn.getCardinality() + "");
		}
		Enumeration children = node.children();
		while (children.hasMoreElements()) {
			TreeNode tn = (TreeNode)children.nextElement();
			nodeElement
			.addContent(elementForNode((AbstractIterationStrategyNode) tn));
		}
		return nodeElement;
	}

	/**
	 * Configure from an XML element
	 * 
	 * @param strategyElement
	 */
	protected void configureFromXML(Element strategyElement) {
		inputs.clear();
		terminal.clear();
		if (strategyElement.getChildren().isEmpty() == false) {
			nodeForElement((Element) strategyElement.getChildren().get(0))
					.setParent(terminal);
		}
	}

	private static AbstractIterationStrategyNode nodeForElement(Element e) {
		AbstractIterationStrategyNode node = null;
		String eName = e.getName();
		if (eName.equals("dot")) {
			node = new DotProduct();
		} else if (eName.equals("cross")) {
			node = new CrossProduct();
		} else if (eName.equals("prefix")) {
			node = new PrefixDotProduct();
		} else if (eName.equals("port")) {
			String portName = e.getAttributeValue("name");
			int portDepth = Integer.parseInt(e.getAttributeValue("depth"));
			node = new NamedInputPortNode(portName, portDepth);
		}
		for (Object child : e.getChildren()) {
			Element childElement = (Element) child;
			nodeForElement(childElement).setParent(node);
		}
		return node;
	}

	/**
	 * Receive a single job from an upstream IterationStrategyImpl in the stack.
	 * This job will have one or more data parts where the cardinality doesn't
	 * match that defined by the NamedInputPortNode and will need to be split up
	 * appropriately
	 * 
	 * @param j
	 */
	protected void receiveEvent(Event e) {
		// If we ever get this method called we know we're not the top layer in
		// the dispatch stack and that we need to perform wrap / unwrap of data
		// as it comes in. This boolean flag informs the behaviour of the
		// terminal
		// node in the strategy.
		wrapping = true;
		// If this is a Job object then we'll need to split it up and push it
		// through the iteration system to get multiple child jobs followed by a
		// completion event otherwise we can just push the completion event all
		// the way through the system.
		if (e instanceof Job) {
			Job j = ((Job) e).pushIndex();
			// Now have to split this job up into a number of distinct events!
			String owningProcess = j.getOwningProcess();
			for (String portName : j.getData().keySet()) {
				EntityIdentifier dataRef = j.getData().get(portName);
				DataManager manager = ContextManager.getDataManager(e
						.getOwningProcess());
				NamedInputPortNode ipn = nodeForName(portName);
				int desiredDepth = ipn.getCardinality();
				Iterator<ContextualizedIdentifier> ids = manager.traverse(
						dataRef, desiredDepth);
				while (ids.hasNext()) {
					ContextualizedIdentifier ci = ids.next();
					int[] indexArray = ci.getIndex();
					EntityIdentifier childDataRef = ci.getDataRef();
					receiveData(portName, owningProcess, indexArray,
							childDataRef);
				}
				receiveCompletion(portName, owningProcess, new int[] {});
			}
		}
		// Event was a completion event, push it through unmodified to the
		// terminal node. Intermediate completion events from the split of an
		// input Job into multiple events through data structure traversal are
		// unwrapped but as this one is never wrapped in the first place we need
		// a way to mark it as such, the call to the bypass method achieves this
		else {
			terminal.receiveBypassCompletion((Completion) e);
		}
	}

	/**
	 * Receive a single data event from an upstream process. This method is only
	 * ever called on the first layer in the IterationStrategyStackImpl, other
	 * layers are passed entire Job objects
	 * 
	 * @param inputPortName
	 * @param owningProcess
	 * @param indexArray
	 * @param dataReference
	 * @throws WorkflowStructureException
	 */
	public void receiveData(String inputPortName, String owningProcess,
			int[] indexArray, EntityIdentifier dataReference)
			throws WorkflowStructureException {
		Map<String, EntityIdentifier> dataMap = new HashMap<String, EntityIdentifier>();
		dataMap.put(inputPortName, dataReference);
		Job newJob = new Job(owningProcess, indexArray, dataMap);
		nodeForName(inputPortName).receiveJob(0, newJob);
	}

	public void receiveCompletion(String inputPortName, String owningProcess,
			int[] completionArray) throws WorkflowStructureException {
		nodeForName(inputPortName).receiveCompletion(0,
				new Completion(owningProcess, completionArray));
	}

	public void addInput(NamedInputPortNode nipn) {
		synchronized (inputs) {
			this.inputs.add(nipn);
		}
	}

	public void removeInput(NamedInputPortNode nipn) {
		synchronized (inputs) {
			this.inputs.remove(nipn);
		}
	}

	void removeInputByName(String name) {
		synchronized (inputs) {
			NamedInputPortNode removeMe = null;
			for (NamedInputPortNode nipn : inputs) {
				if (nipn.getPortName().equals(name)) {
					removeMe = nipn;
				}
			}
			if (removeMe != null) {
				this.inputs.remove(removeMe);
			}
		}
	}

	private NamedInputPortNode nodeForName(String portName)
			throws WorkflowStructureException {
		for (NamedInputPortNode node : inputs) {
			if (node.getPortName().equals(portName)) {
				return node;
			}
		}
		throw new WorkflowStructureException("No port found with name '"
				+ portName + "'");
	}

	public void setIterationStrategyStack(IterationStrategyStackImpl stack) {
		this.stack = stack;
	}

	/**
	 * Connect up a new named input port node to the first child of the terminal
	 * node. If the terminal node doesn't have any children then create a new
	 * cross product node, connect it to the terminal and connect the new input
	 * port node to the cross product (saneish default behaviour)
	 * 
	 * @param nipn
	 */
	public synchronized void connectDefault(NamedInputPortNode nipn) {
		if (terminal.getChildCount() == 0) {
			CrossProduct cp = new CrossProduct();
			cp.setParent(terminal);
			nipn.setParent(cp);
		}
		else {
			AbstractIterationStrategyNode node = (AbstractIterationStrategyNode)(terminal.getChildAt(0));
			nipn.setParent(node);
		}
	}

}
