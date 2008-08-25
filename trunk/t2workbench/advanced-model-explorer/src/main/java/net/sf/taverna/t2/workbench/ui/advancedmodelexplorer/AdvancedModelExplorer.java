package net.sf.taverna.t2.workbench.ui.advancedmodelexplorer;

import java.awt.Color;
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
import javax.swing.border.LineBorder;
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
 * Advanced Model Explorer provides a context sensitive tree view of a dataflow
 * (showing its inputs, outputs, processors, datalinks, etc.). Selection of a node in
 * the Model Explorer tree leads to context sensitive options appearing in a
 * pop-up menu.
 * 
 * @author Alex Nenadic
 * 
 */
public class AdvancedModelExplorer extends JPanel implements UIComponentSPI {

	private static final long serialVersionUID = 586317170453993670L;
	
	private static Logger logger = Logger.getLogger(AdvancedModelExplorer.class);

	/* Manager of currently opened dataflows in the workbench. */
	private static FileManager fileManager = FileManager.getInstance();
	
	/* Observer of events on the file manager, such as opening or closing of a dataflow.*/
	private final FileManagerObserver fileManagerObserver = new FileManagerObserver();
		
	/* The currently selected dataflow (to be displayed in the Advanced Model Explorer). */
	private Dataflow dataflow;

	/* The tree represenatation of the currenlty selected dataflow. */
	private JTree ameTree;
	
	/*
	 * Ordered list of all opened dataflows and their corresponding tree
	 * representations so we can go back and forth. The order corresponds to the
	 * order in which the dataflows were opened.
	 */
	private LinkedHashMap<Dataflow, JTree> openDataflows = new LinkedHashMap<Dataflow, JTree>();

	/* Scroll pane containing the dataflow tree. */
	private JScrollPane jspTree;
	
	/** AdvancedModelExplorer singleton */
	private static AdvancedModelExplorer INSTANCE;

	/**
	 * Returns an AdvancedModelExplorer instance.
	 */
	public static AdvancedModelExplorer getInstance()  {
		synchronized (AdvancedModelExplorer.class) {
			if (INSTANCE == null)
				INSTANCE = new AdvancedModelExplorer();
		}
		return INSTANCE;
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Advanced Model Explorer";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
	}

	public void onDispose() {
		// TODO Auto-generated method stub
	}

	/**
	 * Constructs the Advanced Model Explorer.
	 */
	public AdvancedModelExplorer() {

		// Create a tree that will represent a view over the current dataflow
		// Initially, there is no dataflow opened, so we create an empty tree,
		// but immediatelly after all visual components of the Workbench are 
		// created (including Advanced Model Explorer) a new empty dataflow is 
		// created, which is represented with a NON-empty JTree with four nodes 
		// (Inputs, Outputs, Processors, and Data links) that themselves have no
		// children.
		ameTree = new JTree(new DefaultMutableTreeNode("No dataflow available"));
		ameTree.setEditable(false);
		ameTree.setExpandsSelectedPaths(false);
		ameTree.setDragEnabled(false);
		ameTree.setScrollsOnExpand(false);
		ameTree.setCellRenderer(new AdvancedModelExplorerTreeCellRenderer());
		
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
		JLabel jlTitle = new JLabel("Advanced Model Explorer");
		jlTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		jlTitle.setLabelFor(jspTree);
		jlTitle.setIcon(WorkbenchIcons.advancedModelExplorerIcon);
		add(jlTitle);

		add(Box.createRigidArea(new Dimension(0,5)));
		
		// Tree scroll pane
		jspTree = new JScrollPane(ameTree);
		jspTree.setBorder(new EtchedBorder());
		add(jspTree);

	}
	
	/**
	 * Gets called when a new dataflow is opened or a dummy empty dataflow
	 * created (when there are no other opened dataflows) to create and display
	 * the tree view of the dataflow.
	 */
	public void openDataflow(Dataflow df) {
		
		// Note that in this case fileManager.getCurrentDataflow() == df
				
		// Get the freshly opened dataflow (it can be an empty dummy one)
		this.dataflow = df;
			
		// Create a new tree model and populate it with the new dataflow's data
		AdvancedModelExplorerTreeModel ameTreeModel = new AdvancedModelExplorerTreeModel(dataflow);		
		JTree tree = new JTree(ameTreeModel);	
		tree.setEditable(false);
		tree.setExpandsSelectedPaths(false);
		tree.setDragEnabled(false);
		tree.setScrollsOnExpand(false);
		tree.setCellRenderer(new AdvancedModelExplorerTreeCellRenderer());

		// Add to the list of opened dataflows
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
	 * Expands all nodes in the dataflow tree that have children.
	 */
	private void expandAll() {
		
	    int row = 0;
	    while (row < ameTree.getRowCount()) {
	    	ameTree.expandRow(row);
	      row++;
	     }
	}

	/**
	 * Gets called when the user switches to an already opened dataflow to
	 * display this dataflow's tree view.
	 */
	public void setCurrentDataflow(Dataflow df) {
		
		// Note that in this case fileManager.getCurrentDataflow() == df

		// Get the newly selected dataflow
		this.dataflow = df;
		
		// The current tree becomes the tree of the newly selected workflow
		ameTree = openDataflows.get(dataflow); 
				
		// Update the viewport of the scroll pane
		jspTree.setViewportView(ameTree);
		
		jspTree.revalidate();
		jspTree.repaint();
		
	}

	/**
	 * Gets called when a dataflow is closed to remove its tree from the Advanced Model
	 * Explorer's view.
	 */
	public void closeDataflow(Dataflow df) {
		
		// Note that in this case fileManager.getCurrentDataflow() != df
		// as current dataflow has already been changed by the FileManager
		
		// Just remove the dataflow from the list - the new dataflow in place 
		// of the closed one has already been displayed as the 
		// setCurrentDataflowEvent happended immediately before, 
		// so we not have to do anything here
			
		openDataflows.remove(df);
		
	}


	/**
	 * Observes events on FileManager, i.e. when a dataflow is opened or closed.
	 */
	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {

			if (message instanceof SetCurrentDataflowEvent){				
				if (fileManager.getOpenDataflows().size() == openDataflows.size()){	
					// This is either a case of changing between already opened dataflows
					// or closing a dataflow
					logger.info("AdvancedModelExplorer: Dataflow changed");
					setCurrentDataflow(((SetCurrentDataflowEvent) message).getDataflow());
				}
				else if (fileManager.getOpenDataflows().size() > openDataflows.size()){
					// This is a case of opening a new dataflow, in which case an 
					// OpenedDataflowEvent will follow to deal with it so we do nothing here
					logger.info("AdvancedModelExplorer: New dataflow opened");
					openDataflow(((SetCurrentDataflowEvent) message).getDataflow());	
				}
			}
			else if (message instanceof ClosedDataflowEvent){
				logger.info("AdvancedModelExplorer: Dataflow closed");
				closeDataflow(((ClosedDataflowEvent) message).getDataflow());
			}

		}
	}

}
