package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityInputPortImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityOutputPortImpl;

/**
 * Workflow Explorer tree model. The tree root has four children nodes,
 * representing the workflow inputs, outputs, processors and datalinks.
 * A processor node can contain a nested workflow, in which case the whole tree 
 * structure is repeated with the processor node as root for the 
 * nested workflow sub-tree. Alternatively, it can be a simple processor,
 * having only the processor's input and output ports as children. Input, 
 * output and datalink nodes do not have children.
 * 
 * @author Alex Nenadic
 *
 */
public class WorkflowExplorerTreeModel extends DefaultTreeModel{

	private static final long serialVersionUID = -2327461863858923772L;
	
	public static final String INPUTS = "Inputs";
	public static final String OUTPUTS = "Outputs";
	public static final String PROCESSORS = "Processors";
	public static final String DATALINKS = "Data links";

	/*	The workflow that this class models. */
	//private Dataflow workflow;
	
	/* Root of the tree. */
	private DefaultMutableTreeNode rootNode;
	
	public WorkflowExplorerTreeModel(Dataflow df){
		
		super(new DefaultMutableTreeNode(df.getLocalName())); // root node gets named after the workflow name		
		rootNode = (DefaultMutableTreeNode) this.getRoot();
		createTree(df, rootNode);			
	}
	
	/**
	 * Creates the tree model from a given workflow, for a given tree root.
	 */
	private void createTree(Dataflow df, DefaultMutableTreeNode root) {

		// Create the four main node groups - inputs, outputs, 
		// processors and datalinks.
		DefaultMutableTreeNode inputs = new DefaultMutableTreeNode(INPUTS);
		DefaultMutableTreeNode outputs = new DefaultMutableTreeNode(OUTPUTS);
		DefaultMutableTreeNode processors = new DefaultMutableTreeNode(PROCESSORS);
		DefaultMutableTreeNode datalinks = new DefaultMutableTreeNode(DATALINKS);
		
		// Attach them to the root of the tree
		root.add(inputs);
		root.add(outputs);
		root.add(processors);
		root.add(datalinks);

		// Populate the workflow's inputs.
		List<? extends DataflowInputPort> inputsList = (List<? extends DataflowInputPort>) df.getInputPorts();
		if (inputsList != null) {
			for (DataflowInputPort dataflowInput : inputsList) {
				inputs.add(new DefaultMutableTreeNode(dataflowInput));
			}
		}
		
		// Populate the workflow's outputs.
		List<? extends DataflowOutputPort> outputsList = (List<? extends DataflowOutputPort>) df.getOutputPorts();
		if (outputsList != null) {
			for (DataflowOutputPort dataflowOutput : outputsList) {
				outputs.add(new DefaultMutableTreeNode(dataflowOutput));
			}
		}
		
		// Populate the workflow's processors (which in turn can contain a nested workflow).
		List<? extends Processor> processorsList = (List<? extends Processor>) df.getProcessors();
		if (!processorsList.isEmpty()) {
			for (Processor processor : processorsList){
				DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(
						processor);
				processors.add(processorNode);
				
				// Nested workflow case
				if (Tools.containsNestedWorkflow(processor)){
					
					Dataflow nestedWorkflow = ((NestedDataflow) processor.getActivityList().get(0)).getNestedDataflow();
					
					// Processor node is the root of the new nested tree
					createTree(nestedWorkflow, processorNode);
				}
				else{
					// A processor node can have children (e.g. input and output ports of its associated activity/activities).
					// Currently we just look at the first activity in the list.
					for (ActivityInputPort inputPort : processor.getActivityList().get(0).getInputPorts()){
						processorNode.add(new DefaultMutableTreeNode(inputPort));
					}
					
					for (OutputPort outputPort : processor.getActivityList().get(0).getOutputPorts()){
						processorNode.add(new DefaultMutableTreeNode(outputPort));
					}
				}
			}
		}
		
		// Populate the workflow's data links.
		List<? extends Datalink> datalinksList = (List<? extends Datalink>) df.getLinks();
		if (!datalinksList.isEmpty()) {
			for (Datalink datalink: datalinksList) {
				datalinks.add(new DefaultMutableTreeNode(datalink));
			}
		}
		
	}
	
	/**
	 * Returns a path from the root to the node containing the object.
	 */
	public static TreePath getPathForObject(Object userObject, DefaultMutableTreeNode root){
		
		if (userObject instanceof DataflowInputPort){
			// Get the root inputs node
			DefaultMutableTreeNode inputs = (DefaultMutableTreeNode) root.getChildAt(0);
			for (int i = 0; i< inputs.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) inputs.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}			
			// The node we are looking for must be under some nested workflow then
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2); // Root node for processors that contain the nested workflow node
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					TreePath tp = getPathForObject(userObject, processor);
					if (tp != null)
						return tp;
				}
			}
		}
		else if (userObject instanceof DataflowOutputPort){
			// Get the root outputs node
			DefaultMutableTreeNode outputs = (DefaultMutableTreeNode) root.getChildAt(1);
			for (int i = 0; i< outputs.getChildCount(); i++){ // loop through the outputs
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) outputs.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}			
			// The node we are looking for must be under some nested workflow then
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2); // Root node for processors that contain the nested workflow node
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					TreePath tp = getPathForObject(userObject, processor);
					if (tp != null)
						return tp;				}
			}
		}
		else if (userObject instanceof Processor){ // loop through the processors
			// Get the root procesors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = 0; i< processors.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) processors.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
			// The node we are looking for must be under some nested workflow then
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					TreePath tp = getPathForObject(userObject, processor);
					if (tp != null)
						return tp;				}
			}
		}
		else if (userObject instanceof ActivityInputPortImpl){
			// This is an input port of a processor (i.e. its associated activity)
			// Get the root procesors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);

			for (int i = processors.getChildCount() - 1; i >= 0 ; i--){
				// Looping backwards so that nested workflows are checked last
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){ // if this is nested workflow - descend into it
					TreePath tp = getPathForObject(userObject, processor);
					if ( tp != null){
						return tp;
					}
				}
				else { 
					// This is not a nested workflow, so loop 
					// thought the processor's input and output ports,
					// and see if there is a matching input port
					for (int j = 0; j < processor.getChildCount(); j++){
						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityInputPortImpl) && 
								(((ActivityInputPort) port_node.getUserObject()).equals(userObject))){
							return new TreePath(port_node.getPath());
						}
					}
				}
			}
		}
		else if (userObject instanceof ActivityOutputPortImpl){
			// This is an output port of a processor (i.e. its associated activity)
			// Get the root procesors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = processors.getChildCount() - 1; i >= 0 ; i--){
				// Looping backwards so that nested workflows are checked last
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){ // if this is nested workflow - descend into it
					TreePath tp = getPathForObject(userObject, processor);
					if ( tp != null){
						return tp;
					}
				}
				else { 
					// This is not a nested workflow, so loop 
					// thought the processor's input and output ports,
					// and see if there is a matching input port
					for (int j = 0; j < processor.getChildCount(); j++){

						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityOutputPortImpl) && 
								(((OutputPort) port_node.getUserObject()).equals(userObject))){
							return new TreePath(port_node.getPath());
						}
					}
				}
			}
		}
		else if (userObject instanceof Datalink){
			// Get the root datalinks node
			DefaultMutableTreeNode datalinks = (DefaultMutableTreeNode) root.getChildAt(3);
			for (int i = 0; i< datalinks.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) datalinks.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
			// The node we are looking for must be under some nested workflow then
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2); // Root node for processors that contain the nested workflow node
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					TreePath tp = getPathForObject(userObject, processor);
					if (tp != null)
						return tp;				
				}
			}
		}
		
		return null;
	}
	
}
