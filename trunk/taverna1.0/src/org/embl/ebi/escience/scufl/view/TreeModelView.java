/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import org.embl.ebi.escience.scufl.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
//import org.embl.ebi.escience.scufl.view.tree.*;

/**
 * A TreeModel implementation used to represent a ScuflModel
 * as a tree.
 * @author Tom Oinn
 */
public class TreeModelView extends DefaultTreeModel implements ScuflModelEventListener {
    
    private ScuflModel workflow = null;
    private MutableTreeNode inputRootNode, outputRootNode, processorRootNode, datalinkRootNode, constraintRootNode;
    
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
	    if ((Processor)source == workflow.getWorkflowSourceProcessor()) {
		generateInputs();
		return;
	    }
	    else if ((Processor)source == workflow.getWorkflowSinkProcessor()) {
		generateOutputs();
		return;
	    }
	    else {
		if (event instanceof MinorScuflModelEvent == false) {
		    updateProcessorNode((Processor)source);
		}
		return;
	    }
	}
	else if (source instanceof ConcurrencyConstraint) {
	    generateConstraints();
	    return;
	}
	else if (source instanceof DataConstraint) {
	    generateLinks();
	    return;
	}
	generateInitialModel();
    }

    /**
     * Returns the root of the model as a mutable
     * tree node
     */
    private DefaultMutableTreeNode getRootNode() {
	return (DefaultMutableTreeNode)getRoot();
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
	clearNode(inputRootNode);
	Port[] inputPorts = workflow.getWorkflowSourcePorts();
	for (int i = 0; i < inputPorts.length; i++) {
	    insertNodeInto(new DefaultMutableTreeNode(inputPorts[i]),
			   inputRootNode,
			   i);
	}
    }
    
    /**
     * Clear the workflow output list and regenerate
     */
    private synchronized void generateOutputs() {
	clearNode(outputRootNode);
	Port[] outputPorts = workflow.getWorkflowSinkPorts();
	for (int i = 0; i < outputPorts.length; i++) {
	    insertNodeInto(new DefaultMutableTreeNode(outputPorts[i]),
			   outputRootNode,
			   i);
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
			   datalinkRootNode,
			   i);
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
			   constraintRootNode,
			   i);
	}
    }

    /**
     * Clear the processor list and regenerate
     */
    private synchronized void generateProcessors() {
	clearNode(processorRootNode);
	Processor[] processors = workflow.getProcessors();
	for (int i = 0; i < processors.length; i++) {
	    updateProcessorNode(processors[i]);
	}
    }

    /**
     * Update the given processor, or create a node for it if it
     * doesn't already exist
     */
    private synchronized void updateProcessorNode(Processor p) {
	MutableTreeNode processorNode = null;
	for (int i = 0; i < processorRootNode.getChildCount() && processorNode == null; i++) {
	    if (((DefaultMutableTreeNode)processorRootNode.getChildAt(i)).getUserObject()==p) {
		processorNode = (MutableTreeNode)processorRootNode.getChildAt(i);
	    }
	}
	// If the processorNode is null then create a new one
	if (processorNode == null) {
	    processorNode = new DefaultMutableTreeNode(p);
	    insertNodeInto(processorNode, processorRootNode, processorRootNode.getChildCount());
	}
	// Clear the immediate children of this processor node
	// and regenerate it
	refreshProcessorNode((DefaultMutableTreeNode)processorNode);
    }
    
    /**
     * Refresh the given processor node from the workflow model
     */
    private synchronized void refreshProcessorNode(DefaultMutableTreeNode processorNode) {
	clearNode(processorNode);
	Processor processor = (Processor)processorNode.getUserObject();
	InputPort[] inputs = processor.getInputPorts();
	OutputPort[] outputs = processor.getOutputPorts();
	for (int j = 0; j < inputs.length; j++) {
	    insertNodeInto(new DefaultMutableTreeNode(inputs[j]),
			   processorNode,
			   j);
	}
	for (int j = 0; j < outputs.length; j++) {
	    insertNodeInto(new DefaultMutableTreeNode(outputs[j]),
			   processorNode,
			   j+inputs.length);
	}
	// Add the alternates
	AlternateProcessor[] alternates = processor.getAlternatesArray();
	for (int j = 0; j < alternates.length; j++) {
	    MutableTreeNode alternateNode = new DefaultMutableTreeNode(alternates[j]);
	    insertNodeInto(alternateNode, processorNode, j+inputs.length+outputs.length);
	    // Add alternate ports to the alternate
	    InputPort[] alternateInputs = alternates[j].getProcessor().getInputPorts();
	    OutputPort[] alternateOutputs = alternates[j].getProcessor().getOutputPorts();
	    for (int k = 0; k < alternateInputs.length; k++) {
		insertNodeInto(new DefaultMutableTreeNode(alternateInputs[k]),
			       alternateNode,
			       k);
	    }
	    for (int k = 0; k < alternateOutputs.length; k++) {
		insertNodeInto(new DefaultMutableTreeNode(alternateOutputs[k]),
			       alternateNode,
			       k+alternateInputs.length);
	    }
	}
    }

    /**
     * Remove all child nodes of a MutableTreeNode within this
     * TreeModel
     */
    private synchronized void clearNode(MutableTreeNode parent) {
	while (parent.getChildCount() > 0) {
	    removeNodeFromParent((MutableTreeNode)parent.getChildAt(0));
	}
    }

}
