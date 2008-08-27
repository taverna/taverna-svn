package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;

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
	
	/* Manager of events (edits) on the dataflow. */
	private static EditManager editManager = EditManager.getInstance();
	
	/* Observer of events on the edit manager, such as modifications on an opened dataflow.*/
	private final EditManagerObserver editManagerObserver = new EditManagerObserver();

	public WorkflowExplorerTreeModel(Dataflow df){
		
		super(new DefaultMutableTreeNode(df.getLocalName())); // root node gets named after the dataflow name
		
		this.dataflow=df;
		
		// Attach the main 4 node groups to the root of the tree
		((DefaultMutableTreeNode) getRoot()).add(inputsRootNode);
		((DefaultMutableTreeNode) getRoot()).add(outputsRootNode);
		((DefaultMutableTreeNode) getRoot()).add(processorsRootNode);
		((DefaultMutableTreeNode) getRoot()).add(datalinksRootNode);

		// Populate the tree model nodes with the data from the dataflow
		updateInputsNode();
		updateOutputsNode();
		updateProcessorsNode();
		updateDatalinksNode();
				
		editManager.addObserver(editManagerObserver);

	}
	
	/** 
	 * Populates the nodes containing the dataflow's inputs.
	 */
	private void updateInputsNode(){
		
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
	private void updateOutputsNode(){

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
	private void updateProcessorsNode() {

		List<? extends Processor> processorsList = (List<? extends Processor>) dataflow
				.getProcessors();
		if (processorsList != null) {
			for (Iterator<? extends Processor> it = (Iterator<? extends Processor>) processorsList
					.iterator();it.hasNext(); ) {
				Processor processor = (Processor) it.next();
				DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(
						processor);
				processorsRootNode.add(processorNode);

				// A processor node can have children (e.g. input and output ports)
				List<? extends ProcessorInputPort> proc_inputsList = (List<? extends ProcessorInputPort>) processor
						.getInputPorts();
				List<? extends ProcessorOutputPort> proc_outputsList = (List<? extends ProcessorOutputPort>) processor
						.getOutputPorts();
				for (Iterator<? extends ProcessorInputPort> port_it = (Iterator<? extends ProcessorInputPort>) proc_inputsList
						.iterator();port_it.hasNext(); ) {
					processorNode.add(new DefaultMutableTreeNode(((ProcessorInputPort) port_it.next())));
				}
			
				for (Iterator<? extends ProcessorOutputPort> port_it = (Iterator<? extends ProcessorOutputPort>) proc_outputsList
						.iterator();port_it.hasNext(); ) {
					processorNode.add(new DefaultMutableTreeNode(((ProcessorOutputPort) port_it.next())));
					
				}
			}
		}
	}
	
	/** 
	 * Populates the nodes containing the dataflow's data links.
	 */
	private void updateDatalinksNode(){

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
	 * Observes edit events on a dataflow, such as
	 * adding a new processor or a port.
	 */
	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {

		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			// TODO Auto-generated method stub
			
		}
	}

}
