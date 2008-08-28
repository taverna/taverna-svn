package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

public class WorkflowExplorerTreeModel extends DefaultTreeModel{

	private static final long serialVersionUID = -2327461863858923772L;
	
	/*	The dataflow that this class models. */
	private Dataflow dataflow;
	
	/* Main tree nodes - inputs, outputs, processors and data links. */
	private DefaultMutableTreeNode inputsRootNode = new DefaultMutableTreeNode(
	"Workflow inputs");
	private DefaultMutableTreeNode outputsRootNode = new DefaultMutableTreeNode(
	"Workflow outputs");
	private DefaultMutableTreeNode processorsRootNode = new DefaultMutableTreeNode(
	"Processors");
	private DefaultMutableTreeNode datalinksRootNode = new DefaultMutableTreeNode(
	"Data links");
	
	public WorkflowExplorerTreeModel(Dataflow df){
		
		super(new DefaultMutableTreeNode(df.getLocalName())); // root node gets named after the dataflow name
		
		this.dataflow=df;
		
		// Attach the main 4 node groups to the root of the tree
		((DefaultMutableTreeNode) getRoot()).add(inputsRootNode);
		((DefaultMutableTreeNode) getRoot()).add(outputsRootNode);
		((DefaultMutableTreeNode) getRoot()).add(processorsRootNode);
		((DefaultMutableTreeNode) getRoot()).add(datalinksRootNode);

		// Populate the tree model nodes with the data from the dataflow
		populateInputNodes();
		populateOutputNodes();
		populateProcessorNodes();
		populateDatalinkNodes();
				
	}
	
	/** 
	 * Populates the nodes containing the dataflow's inputs.
	 */
	private void populateInputNodes(){
		
		List<? extends DataflowInputPort> inputsList = (List<? extends DataflowInputPort>) dataflow
				.getInputPorts();
		if (inputsList != null) {
			for (Iterator<? extends DataflowInputPort> it = (Iterator<? extends DataflowInputPort>) inputsList
					.iterator();it.hasNext(); ) {
				inputsRootNode.add(new DefaultMutableTreeNode(((DataflowInputPort) it.next())));
			}
		}
	}
	
	/** 
	 * Populates the nodes containing the dataflow's outputs.
	 */
	private void populateOutputNodes(){

		List<? extends DataflowOutputPort> outputsList = (List<? extends DataflowOutputPort>) dataflow
				.getOutputPorts();
		if (outputsList != null) {
			for (Iterator<? extends DataflowOutputPort> it = (Iterator<? extends DataflowOutputPort>) outputsList
					.iterator();it.hasNext(); ) {
				outputsRootNode.add(new DefaultMutableTreeNode(((DataflowOutputPort) it.next())));
			}
		}
	}
	
	/** 
	 * Populates the nodes containing the dataflow's processors.
	 */
	private void populateProcessorNodes() {

		List<? extends Processor> processorsList = (List<? extends Processor>) dataflow
				.getProcessors();
		if (processorsList != null) {
			for (Iterator<? extends Processor> it = (Iterator<? extends Processor>) processorsList
					.iterator();it.hasNext(); ) {
				Processor processor = (Processor) it.next();
				DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(
						processor);
				processorsRootNode.add(processorNode);

				// A processor node can have children (e.g. input and output ports of its associated activity/activities)
				// Currently we just look at the first activity in the list.
				Set<ActivityInputPort> activityInputsList = processor.getActivityList().get(0).getInputPorts();
				Set<OutputPort> activityOutputsList = processor.getActivityList().get(0).getOutputPorts();
				for (Iterator<ActivityInputPort> activityInputsIterator = (Iterator<ActivityInputPort>) activityInputsList
						.iterator(); activityInputsIterator.hasNext(); ) {
					processorNode.add(new DefaultMutableTreeNode(((ActivityInputPort) activityInputsIterator.next())));
				}
				for (Iterator<OutputPort> activityOutputsIterator = (Iterator<OutputPort>) activityOutputsList
						.iterator(); activityOutputsIterator.hasNext(); ) {
					processorNode.add(new DefaultMutableTreeNode(((OutputPort) activityOutputsIterator.next())));
				}
			}
		}
	}
	
	/** 
	 * Populates the nodes containing the dataflow's data links.
	 */
	private void populateDatalinkNodes(){

		List<? extends Datalink> datalinksList = (List<? extends Datalink>) dataflow
				.getLinks();
		if (datalinksList != null) {
			for (Iterator<? extends Datalink> it = (Iterator<? extends Datalink>) datalinksList
					.iterator();it.hasNext(); ) {
				datalinksRootNode.add(new DefaultMutableTreeNode(((Datalink) it.next())));
			}
		}
	}


	/**
	 * Returns a path from the root to the node containing the object.
	 */
	public TreePath getPathForObject(Object userObject){
		
		if (userObject instanceof DataflowInputPort){
			for (int i = 0; i< inputsRootNode.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) inputsRootNode.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
		}
		else if (userObject instanceof DataflowOutputPort){
			for (int i = 0; i< outputsRootNode.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) outputsRootNode.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
		}
		else if (userObject instanceof Processor){
			for (int i = 0; i< processorsRootNode.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) processorsRootNode.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
		}
		else if (userObject instanceof ActivityInputPort){
			for (int i = 0; i< processorsRootNode.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) processorsRootNode.getChildAt(i);
				for (int j = 0; j < node.getChildCount(); j++){
					DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) node.getChildAt(j);
					if (port_node.getUserObject().equals(userObject)){
						return new TreePath(port_node.getPath());
					}
				}
			}
		}
		else if (userObject instanceof OutputPort){
			for (int i = 0; i< processorsRootNode.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) processorsRootNode.getChildAt(i);
				for (int j = 0; j < node.getChildCount(); j++){
					DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) node.getChildAt(j);
					if (port_node.getUserObject().equals(userObject)){
						return new TreePath(port_node.getPath());
					}
				}
			}
		}
		else if (userObject instanceof Datalink){
			for (int i = 0; i< datalinksRootNode.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) datalinksRootNode.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
		}
		
		return null; // should not happen really
	}
	
}
