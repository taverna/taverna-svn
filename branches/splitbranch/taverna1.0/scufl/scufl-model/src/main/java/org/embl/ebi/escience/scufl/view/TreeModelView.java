/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.MinorScuflModelEvent;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;

/**
 * A TreeModel implementation used to represent a ScuflModel as a tree.
 * 
 * @author Tom Oinn
 */
public class TreeModelView extends DefaultTreeModel implements
		ScuflModelEventListener {

	private ScuflModel workflow = null;

	private MutableTreeNode inputRootNode, outputRootNode, processorRootNode,
			datalinkRootNode, constraintRootNode;

	/**
	 * Create a new TreeModel implementation
	 */
	public TreeModelView() {
		super(new DefaultMutableTreeNode("No model available"));
	}

	/**
	 * Return the workflow this view is bound to
	 */
	public ScuflModel getModel() {
		return this.workflow;
	}

	/**
	 * Bind to a ScuflModel
	 */
	public void attachToModel(ScuflModel theModel) {
		this.workflow = theModel;
		theModel.addListener(this);
		getRootNode().setUserObject("Workflow model");
		generateInitialModel();
	}

	/**
	 * Unbind from the ScuflModel
	 */
	public void detachFromModel() {
		if (this.workflow != null) {
			this.workflow.removeListener(this);
			this.workflow = null;
			getRootNode().setUserObject("No model available");
			clearNode(getRootNode());
		}
	}

	/**
	 * Handle a model change event
	 */
	public synchronized void receiveModelEvent(ScuflModelEvent event) {
		Object source = event.getSource();
		if (source instanceof Processor) {
			// Check that the event didn't originate from the
			// source and sink port holder
			if ((Processor) source == workflow.getWorkflowSourceProcessor()) {
				generateInputs();
				return;
			} else if ((Processor) source == workflow
					.getWorkflowSinkProcessor()) {
				generateOutputs();
				return;
			} else {
				if (event instanceof MinorScuflModelEvent == false) {
					generateProcessors();
				}
				return;
			}
		} else if (source instanceof ConcurrencyConstraint) {
			generateConstraints();
			return;
		} else if (source instanceof DataConstraint) {
			generateLinks();
			return;
		} else if (source instanceof InputPort) {
			// Default value change
			generateProcessors();
			return;
		}
		generateInputs();
		generateOutputs();
		generateProcessors();
		generateLinks();
		generateConstraints();
	}

	/**
	 * Returns the root of the model as a mutable tree node
	 */
	private DefaultMutableTreeNode getRootNode() {
		return (DefaultMutableTreeNode) getRoot();
	}

	/**
	 * Clear the model and regenerate
	 */
	private synchronized void generateInitialModel() {
		clearNode(getRootNode());
		inputRootNode = new DefaultMutableTreeNode("Workflow inputs");
		outputRootNode = new DefaultMutableTreeNode("Workflow outputs");
		processorRootNode = new DefaultMutableTreeNode("Processors");
		datalinkRootNode = new DefaultMutableTreeNode("Data links");
		constraintRootNode = new DefaultMutableTreeNode("Control links");
		insertNodeInto(inputRootNode, getRootNode(), 0);
		insertNodeInto(outputRootNode, getRootNode(), 1);
		insertNodeInto(processorRootNode, getRootNode(), 2);
		insertNodeInto(datalinkRootNode, getRootNode(), 3);
		insertNodeInto(constraintRootNode, getRootNode(), 4);
		generateInputs();
		generateOutputs();
		generateProcessors();
		generateLinks();
		generateConstraints();
	}

	/**
	 * Clear the workflow input list and regenerate
	 */
	private synchronized void generateInputs() {
		Port[] inputPorts = workflow.getWorkflowSourcePorts();
		updatePortList(inputRootNode, inputPorts, OutputPort.class);
	}

	/**
	 * Clear the workflow output list and regenerate
	 */
	private synchronized void generateOutputs() {
		Port[] outputPorts = workflow.getWorkflowSinkPorts();
		updatePortList(outputRootNode, outputPorts, InputPort.class);
	}

	private void updatePortList(MutableTreeNode parent, Port[] ports,
			Class replacingClass) {
		Set portNames = new HashSet();
		int lastPortIndex = 0;
		for (int i = 0; i < ports.length; i++) {
			portNames.add(ports[i].getName());
		}
		// Remove any output ports that no longer exist
		Set nodesToRemove = new HashSet();
		int tmp = 1;
		for (Enumeration i = parent.children(); i.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) i
					.nextElement();
			if (replacingClass.isInstance(node.getUserObject())) {			
				Port p = (Port) node.getUserObject();
				lastPortIndex = tmp;
				if (portNames.contains(p.getName()) == false) {
					nodesToRemove.add(node);
				} else {
					portNames.remove(p.getName());
				}
			}
			tmp++;
		}
		for (Iterator i = nodesToRemove.iterator(); i.hasNext();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) i.next();
			removeNodeFromParent(node);
			lastPortIndex--;
		}
		// Add any output ports that weren't there before
		for (int i = 0; i < ports.length; i++) {
			if (portNames.contains(ports[i].getName())) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
						ports[i]);
				insertNodeInto(newNode, parent, lastPortIndex++);
			}
		}
	}

	private void updateProcessorList(MutableTreeNode parent,
			Processor[] processors) {
		Set processorNames = new HashSet();
		int lastProcessorIndex = 0;
		for (int i = 0; i < processors.length; i++) {
			processorNames.add(processors[i].getName());
		}
		// Remove any output ports that no longer exist
		Set nodesToRemove = new HashSet();
		int tmp = 1;
		for (Enumeration i = parent.children(); i.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) i
					.nextElement();
			if (node.getUserObject() instanceof Processor) {
				Processor p = (Processor) node.getUserObject();
				lastProcessorIndex = tmp;
				if (processorNames.contains(p.getName()) == false) {
					nodesToRemove.add(node);
				} else {
					processorNames.remove(p.getName());
				}
			}
			tmp++;
		}
		for (Iterator i = nodesToRemove.iterator(); i.hasNext();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) i.next();
			removeNodeFromParent(node);
			lastProcessorIndex--;
		}
		// Add processor nodes that weren't there before
		for (int i = 0; i < processors.length; i++) {
			if (processorNames.contains(processors[i].getName())) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
						processors[i]);
				insertNodeInto(newNode, parent, lastProcessorIndex++);
			}
		}
		for (Enumeration i = parent.children(); i.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) i
					.nextElement();
			if (node.getUserObject() instanceof Processor) {
				refreshProcessorNode(node);
			}
		}
	}

	/**
	 * Clear the data link list and regenerate
	 */
	private synchronized void generateLinks() {
		clearNode(datalinkRootNode);
		DataConstraint[] links = workflow.getDataConstraints();
		for (int i = 0; i < links.length; i++) {
			insertNodeInto(new DefaultMutableTreeNode(links[i]),
					datalinkRootNode, i);
		}
	}

	/**
	 * Clear the coordination constraint list and regenerate
	 */
	private synchronized void generateConstraints() {
		clearNode(constraintRootNode);
		ConcurrencyConstraint[] controls = workflow.getConcurrencyConstraints();
		for (int i = 0; i < controls.length; i++) {
			insertNodeInto(new DefaultMutableTreeNode(controls[i]),
					constraintRootNode, i);
		}
	}

	/**
	 * Clear the processor list and regenerate
	 */
	private synchronized void generateProcessors() {
		Processor[] processors = workflow.getProcessors();
		updateProcessorList(processorRootNode, processors);
	}

	/**
	 * Refresh the given processor node from the workflow model
	 */
	private synchronized void refreshProcessorNode(
			DefaultMutableTreeNode processorNode) {
		Processor processor = null;
		if (processorNode.getUserObject() instanceof Processor) {
			processor = (Processor) processorNode.getUserObject();
		} else if (processorNode.getUserObject() instanceof AlternateProcessor) {
			processor = ((AlternateProcessor) processorNode.getUserObject())
					.getProcessor();
		} else {
			return;
		}
		InputPort[] inputs = processor.getInputPorts();
		OutputPort[] outputs = processor.getOutputPorts();
		updatePortList(processorNode, outputs, OutputPort.class);
		updatePortList(processorNode, inputs, InputPort.class);
		// Do alternates if there are any
		Set existingAlternates = new HashSet();
		AlternateProcessor ap[] = processor.getAlternatesArray();
		for (int i = 0; i < ap.length; i++) {
			existingAlternates.add(ap[i]);
		}
		Set nodesToRemove = new HashSet();
		for (Enumeration e = processorNode.children(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
					.nextElement();
			if (node.getUserObject() instanceof AlternateProcessor) {
				if (existingAlternates.contains(node.getUserObject()) == false) {
					nodesToRemove.add(node);
				} else {
					existingAlternates.remove(node.getUserObject());
				}
			}
		}
		for (Iterator i = nodesToRemove.iterator(); i.hasNext();) {
			removeNodeFromParent((DefaultMutableTreeNode) i.next());
		}
		for (Iterator i = existingAlternates.iterator(); i.hasNext();) {
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(i
					.next());
			refreshProcessorNode(newNode);
			insertNodeInto(newNode, processorNode, processorNode
					.getChildCount());
		}
	}

	/**
	 * Remove all child nodes of a MutableTreeNode within this TreeModel
	 */
	private synchronized void clearNode(MutableTreeNode parent) {
		while (parent.getChildCount() > 0) {
			removeNodeFromParent((MutableTreeNode) parent.getChildAt(0));
		}
		nodeChanged(parent);
	}

}
