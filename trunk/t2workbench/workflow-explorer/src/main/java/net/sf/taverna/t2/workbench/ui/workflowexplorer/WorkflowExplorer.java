package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.ui.menu.impl.ContextMenuFactory;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;


/**
 * Workflow Explorer provides a context sensitive tree view of a workflow
 * (showing its inputs, outputs, processors, datalinks, etc.). Selection of a node in
 * the Model Explorer tree leads to context sensitive options appearing in a
 * pop-up menu.
 * 
 * @author Alex Nenadic
 * 
 */
public class WorkflowExplorer extends JPanel implements UIComponentSPI {

	private static final long serialVersionUID = 586317170453993670L;
	
	private static Logger logger = Logger.getLogger(WorkflowExplorer.class);
	
	/* The currently selected workflow (to be displayed in the Workflow Explorer). */
	private Dataflow workflow;

	/* The tree represenatation of the currenlty selected workflow. */
	private JTree ameTree;

	/* Current workflow's selection model event observer.*/
	private Observer<DataflowSelectionMessage> workflowSelectionListener = new DataflowSelectionListener();

	/* Scroll pane containing the workflow tree. */
	private JScrollPane jspTree;
	
	/* WorkflowExplorer singleton. */
	private static WorkflowExplorer INSTANCE;

	/**
	 * Returns a WorkflowExplorer instance.
	 */
	public static WorkflowExplorer getInstance()  {
		synchronized (WorkflowExplorer.class) {
			if (INSTANCE == null)
				INSTANCE = new WorkflowExplorer();
		}
		return INSTANCE;
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Workflow Explorer";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
	}

	public void onDispose() {
		// TODO Auto-generated method stub
	}

	/**
	 * Constructs the Workflow Explorer.
	 */
	public WorkflowExplorer() {

		// Create a tree that will represent a view over the current workflow
		// Initially, there is no workflow opened, so we create an empty tree,
		// but immediatelly after all visual components of the Workbench are 
		// created (including Workflow Explorer) a new empty workflow is 
		// created, which is represented with a NON-empty JTree with four nodes 
		// (Inputs, Outputs, Processors, and Data links) that themselves have no
		// children.
		ameTree = new JTree(new DefaultMutableTreeNode("No workflow available"));
		
		// Start observing when current workflow is changed (e.g. new workflow opened or 
		// switched between opened workflows). Note that closing a workflow causes either a switch
		// to another opened workflow or, if it was the last one, opening of a new empty workflow
		ModelMap.getInstance().addObserver(new Observer<ModelMap.ModelMapEvent>() {
			public void notify(Observable<ModelMapEvent> sender, final ModelMapEvent message) {
				if (message.getModelName().equals(ModelMapConstants.CURRENT_DATAFLOW)) {
					if (message.getNewModel() instanceof Dataflow) {
						
						// Remove the workflow selection model listener from the previous (if any) 
						// and add to the new workflow (if any)
						Dataflow oldFlow = (Dataflow) message.getOldModel();
						Dataflow newFlow = (Dataflow) message.getNewModel();
						if (oldFlow != null) {
							DataflowSelectionManager
							.getInstance().getDataflowSelectionModel(oldFlow)
									.removeObserver(workflowSelectionListener);
						}

						if (newFlow != null) {
							DataflowSelectionManager
							.getInstance().getDataflowSelectionModel(newFlow)
									.addObserver(workflowSelectionListener);
						}
												
						// Create a new thread to prevent drawing the current workflow tree
						// to take over completely
						new Thread("Workflow Explorer - model map message: current workflow changed.") {
							@Override
							public void run() {
								createWorkflowTree((Dataflow) message.getNewModel());
							}
						}.start();
					}
				}
			}
		});
		
		// Start observing events on Edit Manager when current workflow is edited 
		// (e.g. a node added, deleted or updated)
		EditManager.getInstance().addObserver(new Observer<EditManagerEvent>() {
			public void notify(Observable<EditManagerEvent> sender,
					final EditManagerEvent message) throws Exception {
				if (message instanceof AbstractDataflowEditEvent) {
					AbstractDataflowEditEvent dataflowEditEvent = (AbstractDataflowEditEvent) message;
					logger.info("WorkflowExplorer: workflow edited.");
					//React to edits in the current workflow
					if (dataflowEditEvent.getDataFlow() == workflow) {
						// Create a new thread to prevent drawing the workflow tree
						// to take over completely
						new Thread("Workflow Explorer - edit manager message:  current workflow edited.") {
							@Override
							public void run() {
								// Create a new tree to reflect the changes to the current tree
								createWorkflowTree(((AbstractDataflowEditEvent) message).getDataFlow());
							}
						}.start();
					}
				}
			}
		});
		
			
		// Draw visual components
		initComponents();
	}

	/**
	 * Lays out the swing components.
	 */
	public void initComponents() {
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(10,10,10,10)));
		
		// Title
		JLabel jlTitle = new JLabel("Workflow Explorer");
		jlTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		jlTitle.setLabelFor(jspTree);
		jlTitle.setIcon(WorkbenchIcons.workflowExplorerIcon);
		add(jlTitle);

		add(Box.createRigidArea(new Dimension(0,5)));
		
		// Tree scroll pane
		jspTree = new JScrollPane(ameTree);
		jspTree.setBorder(new EtchedBorder());
		add(jspTree);

	}
	
	/**
	 * Gets called when current workflow is changed either because a new one is 
	 * opened or due to switching between opened workflows or because it is edited
	 * to (re)create and (re)display the tree view of the workflow.
	 */
	public void createWorkflowTree(Dataflow df) {
						
		if (df != null){
			// Set the current workflow
			workflow = FileManager.getInstance().getCurrentDataflow();
				
			// Create a new tree model and populate it with the new workflow's data
			WorkflowExplorerTreeModel ameTreeModel = new WorkflowExplorerTreeModel(workflow);		
			ameTree = new JTree(ameTreeModel);	
			ameTree.setEditable(false);
			ameTree.setExpandsSelectedPaths(true);
			ameTree.setDragEnabled(false);
			ameTree.setScrollsOnExpand(false);
			ameTree.setCellRenderer(new WorkflowExplorerTreeCellRenderer());
			ameTree.addMouseListener(new MouseAdapter(){
				
				public void mouseClicked(MouseEvent evt){
					
					 // Discover the tree row that was clicked on
					int selRow = ameTree.getRowForLocation(evt.getX(), evt.getY());
					if (selRow != -1) {
						// Get the selection path for the row
						TreePath selectionPath = ameTree.getPathForLocation(evt.getX(), evt.getY());
						if (selectionPath != null){
							// Get the selected node
							DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
							
							// For both left and right click - add the workflow object to selection model
							// This will cause the node to become selected (in the selection listener's code)
							DataflowSelectionModel selectionModel = DataflowSelectionManager
							.getInstance().getDataflowSelectionModel(workflow);
							
							// If the node that was clicked on was inputs, outputs, processors or datalinks
							// root nodes than just make it selected and clear the selection model
							if(selectedNode.isRoot() ||
									selectedNode.getUserObject().toString().equals(WorkflowExplorerTreeModel.INPUTS) || 
									selectedNode.getUserObject().toString().equals(WorkflowExplorerTreeModel.OUTPUTS) ||
									selectedNode.getUserObject().toString().equals(WorkflowExplorerTreeModel.PROCESSORS) ||
									selectedNode.getUserObject().toString().equals(WorkflowExplorerTreeModel.DATALINKS)){
								selectionModel.clearSelection();
								ameTree.setSelectionPath(selectionPath);
							}
							else{
								selectionModel.addSelection(selectedNode.getUserObject());
								
								// If this was a right click - show a pop-up menu as well if there is one defined
								if (evt.getButton() == MouseEvent.BUTTON3) {

									// Show a contextual pop-up menu
									JPopupMenu menu = ContextMenuFactory.getContextMenu(workflow,
											selectedNode.getUserObject(), ameTree);
									menu.show(evt.getComponent(), evt.getX(), evt.getY());
								}
							}
						}
					}
				}
			});
			
			// Select the nodes that should be selected (if any)
			setSelectedNodes();

			// Expand the tree
			expandAll();
			
			// Update the viewport of the scroll pane
			jspTree.setViewportView(ameTree);
			
			jspTree.revalidate();
			jspTree.repaint();
		}
	}

	/**
	 * Sets the currently selected node(s) based on the workflow selection model, i.e.
	 * the node(s) currently selected in the workflow graph view also become selected 
	 * in the tree view.
	 */
	private void setSelectedNodes() {
		
		DataflowSelectionModel selectionModel = DataflowSelectionManager	
		.getInstance().getDataflowSelectionModel(workflow);
		
		// List of all selected objects in the graph view
		Set<Object> selection = selectionModel.getSelection();
		if (!selection.isEmpty()){
			// Selection path(s) - can be multiple if more objects are selected
			int i = selection.size();
			TreePath[] paths = new TreePath[i];
			
			for (Iterator<Object> iterator = selection.iterator(); iterator.hasNext(); ){
				TreePath path = WorkflowExplorerTreeModel.getPathForObject(iterator.next(), (DefaultMutableTreeNode) ameTree.getModel().getRoot());
				paths[--i] = path;
			}
			ameTree.setSelectionPaths(paths);
		}
	}

	/**
	 * Expands all nodes in the workflow tree that have children.
	 */
	private void expandAll() {
		
	    int row = 0;
	    while (row < ameTree.getRowCount()) {
	    	ameTree.expandRow(row);
	      row++;
	     }
	}

	/**
	 * Observes events on workflow Selection Manager, i.e. when a workflow 
	 * node is selected in the graph view.
	 */
	private final class DataflowSelectionListener implements
			Observer<DataflowSelectionMessage> {

		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {
			setSelectedNodes();
		}

	}
	
}
