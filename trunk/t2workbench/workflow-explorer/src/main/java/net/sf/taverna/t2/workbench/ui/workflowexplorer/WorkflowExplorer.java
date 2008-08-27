package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import java.awt.Component;
import java.awt.Dimension;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
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

	/* Manager of currently opened workflows in the workbench. */
	private static FileManager fileManager = FileManager.getInstance();
	
	/* Observer of events on the file manager, such as opening or closing of a workflow.*/
	private final FileManagerObserver fileManagerObserver = new FileManagerObserver();
		
	/* The currently selected workflow (to be displayed in the Workflow Explorer). */
	private Dataflow dataflow;

	/* The tree represenatation of the currenlty selected workflow. */
	private JTree ameTree;
	
	/*
	 * Ordered list of all opened workflows and their corresponding tree
	 * representations so we can go back and forth. The order corresponds to the
	 * order in which the workflows were opened.
	 */
	private LinkedHashMap<Dataflow, JTree> openDataflows = new LinkedHashMap<Dataflow, JTree>();

	/* Scroll pane containing the workflow tree. */
	private JScrollPane jspTree;
	
	/** WorkflowExplorer singleton */
	private static WorkflowExplorer INSTANCE;

	/**
	 * Returns an WorkflowExplorer instance.
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
		ameTree.setEditable(false);
		ameTree.setExpandsSelectedPaths(false);
		ameTree.setDragEnabled(false);
		ameTree.setScrollsOnExpand(false);
		ameTree.setCellRenderer(new WorkflowExplorerTreeCellRenderer());
		
		// Start observing events on the FileManager
		fileManager.addObserver(fileManagerObserver);
		
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
	 * Gets called when a new workflow is opened or a dummy empty workflow
	 * created (when there are no other opened workflows) to create and display
	 * the tree view of the workflow.
	 */
	public void openDataflow(Dataflow df) {
		
		// Note that in this case fileManager.getCurrentDataflow() == df
				
		// Get the freshly opened workflow (it can be an empty dummy one)
		this.dataflow = df;
			
		// Create a new tree model and populate it with the new workflow's data
		WorkflowExplorerTreeModel ameTreeModel = new WorkflowExplorerTreeModel(dataflow);		
		JTree tree = new JTree(ameTreeModel);	
		tree.setEditable(false);
		tree.setExpandsSelectedPaths(false);
		tree.setDragEnabled(false);
		tree.setScrollsOnExpand(false);
		tree.setCellRenderer(new WorkflowExplorerTreeCellRenderer());

		// Add to the list of opened workflows
		openDataflows.put(dataflow,tree);
		
		// Set the tree view
		ameTree = tree;
		// Expand the tree
		expandAll();
		
		// Update the viewport of the scroll pane
		jspTree.setViewportView(ameTree);
		
		jspTree.revalidate();
		jspTree.repaint();
		
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
	 * Gets called when the user switches to an already opened workflow to
	 * display this workflow's tree view.
	 */
	public void setCurrentDataflow(Dataflow df) {
		
		// Note that in this case fileManager.getCurrentDataflow() == df

		// Get the newly selected workflow

		this.dataflow = df;
		
		// The current tree becomes the tree of the newly selected workflow
		ameTree = openDataflows.get(dataflow); 
				
		// Update the viewport of the scroll pane
		jspTree.setViewportView(ameTree);
		
		jspTree.revalidate();
		jspTree.repaint();
		
	}

	/**
	 * Gets called when a workflow is closed to remove its tree from the Advanced Model
	 * Explorer's view.
	 */
	public void closeDataflow(Dataflow df) {
		
		// Note that in this case fileManager.getCurrentDataflow() != df
		// as current workflow has already been changed by the FileManager
		
		// Just remove the workflow from the list - the new workflow in place 
		// of the closed one has already been displayed as the 
		// setCurrentDataflowEvent happended immediately before, 
		// so we not have to do anything here
			
		openDataflows.remove(df);
		
	}


	/**
	 * Observes events on FileManager, i.e. when a workflow is opened or closed.
	 */
	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				final FileManagerEvent message) throws Exception {

			if (message instanceof SetCurrentDataflowEvent){				
				if (fileManager.getOpenDataflows().size() == openDataflows.size()){	
					// This is either a case of changing between already opened workflows
					// or closing a workflow
					logger.info("WorkflowExplorer: Dataflow changed");
					new Thread("Workflow Explorer: Set current dataflow") {
						@Override
						public void run() {
							setCurrentDataflow(((SetCurrentDataflowEvent) message).getDataflow());
						}
					}.start();
				}
				else if (fileManager.getOpenDataflows().size() > openDataflows.size()){
					// This is a case of opening a new workflow, in which case an 
					// OpenedDataflowEvent will follow to deal with it so we do nothing here
					logger.info("WorkflowExplorer: New workflow opened");
					new Thread("Workflow Explorer: new workflow opened") {
						@Override
						public void run() {
							openDataflow(((SetCurrentDataflowEvent) message).getDataflow());							
						}
					}.start();
				}
			}
			else if (message instanceof ClosedDataflowEvent){
				logger.info("WorkflowExplorer: Workflow closed");
				new Thread("Workflow Explorer: close dataflow") {
					@Override
					public void run() {
						closeDataflow(((ClosedDataflowEvent) message).getDataflow());						
					}
				}.start();
			}

		}
	}

}
