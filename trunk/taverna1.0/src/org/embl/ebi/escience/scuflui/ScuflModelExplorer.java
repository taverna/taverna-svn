/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.*;
import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.Enumeration;

import org.embl.ebi.escience.scuflui.NoContextMenuFoundException;
import org.embl.ebi.escience.scuflui.ScuflContextMenuFactory;
import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.Object;



/**
 * A swing component that provides an expandable
 * tree view of the constituent components of a
 * ScuflModel instance.
 * @author Tom Oinn
 */
public class ScuflModelExplorer extends JTree implements ScuflModelEventListener {
    
    // The root of the tree we're intended to display
    private DefaultMutableTreeNode root = null;

    // The ScuflModel that this is a view / controller over
    private ScuflModel model = null;

    // The tree model that contains the root element
    private DefaultTreeModel theTreeModel = null;

    /**
     * Default constructor, creates a new ScuflModelExplorer that
     * is not bound to any ScuflModel instance. Use the attachToModel
     * method to actually show data in this component.
     */
    public ScuflModelExplorer() {
	super();
	this.root = new DefaultMutableTreeNode("No Scufl Model!");
	DefaultTreeModel model = (DefaultTreeModel)this.getModel();
	model.setRoot(this.root);
	theTreeModel = model;
	// Only allow single selection (not really important but...)
	this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	// Attach the popup menu generator to the tree
	this.addMouseListener(new ScuflModelExplorerPopupHandler(this));
	// Show lines in the tree diagram
	this.putClientProperty("JTree.lineStyle","Angled");
	ScuflModelExplorerRenderer renderer = new ScuflModelExplorerRenderer();
	this.setCellRenderer(renderer);
    }
    
    /**
     * Set the default expansion state, with all processors, data
     * constraints and workflow source and sink ports show, but
     * nothing else.
     */
    public void setDefaultExpansionState() {
	expandAll(this, new TreePath(this.root), true);
    }
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
	// Ignores nodes who's userObject is a Processor type to
	// avoid overloading the UI with nodes at startup.
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0 && (((DefaultMutableTreeNode)node).getUserObject() instanceof Processor == false)) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
		if (((DefaultMutableTreeNode)n).getUserObject() instanceof Processor) {
		}
		else {
		    expandAll(tree, path, expand);
		}
	    }
        }
	// Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }


    /**
     * Bind this view onto a ScuflModel instance, this
     * registers the view to receive events and thus keep
     * up to date.
     */
    public void attachToModel(ScuflModel theModel) {
	this.model = theModel;
	theModel.addListener(this);
	regenerateTreeModel();
	setDefaultExpansionState();
    }

    /**
     * Unbind from the current model, does nothing if 
     * we're not bound to a model.
     */
    public void detachFromModel() {
	if (this.model != null) {
	    this.model.removeListener(this);
	    this.model = null;
	    this.root = new DefaultMutableTreeNode("No Scufl Model!");
	    DefaultTreeModel model = (DefaultTreeModel)this.getModel();
	    model.setRoot(this.root);
	}
    }
    
    /**
     * Handle events from the model in order to keep up 
     * to date with any changes in state.
     */    
    public void receiveModelEvent(ScuflModelEvent event) {
	regenerateTreeModel();
	// Send an event to the tree model that we've changed the
	// structure of the nodes
	theTreeModel.nodeStructureChanged(this.root);
	setDefaultExpansionState();
    }
    
    // 0 = idle, 1 = updating, 2 = updating but needs to again
    private int regenerationStatus = 0;
    
    /**
     * Update the tree structure to match that of the ScuflModel
     */
    private void regenerateTreeModel() {
	if (this.model != null) {
	    if (this.regenerationStatus == 0) {
		// Set flag to say we're working, clearing
		// any indications that processing is required.
		this.regenerationStatus = 1;
		while (this.regenerationStatus != 0) {


		    // Remove all existing children of the root node.
		    this.root.removeAllChildren();
		    // Set the root node to saying that there is a model.
		    this.root.setUserObject("Scufl Model");

		    // Create a new node for workflow inputs
		    DefaultMutableTreeNode inputs = new DefaultMutableTreeNode("Workflow inputs");
		    this.root.add(inputs);
		    Port[] inputPorts = model.getWorkflowSourcePorts();
		    for (int i = 0; i < inputPorts.length; i++) {
			DefaultMutableTreeNode inode = new DefaultMutableTreeNode(inputPorts[i]);
			inputs.add(inode);
		    }
		     // Create a new node for workflow outputs
		    DefaultMutableTreeNode outputs = new DefaultMutableTreeNode("Workflow outputs");
		    this.root.add(outputs);
		    Port[] outputPorts = model.getWorkflowSinkPorts();
		    for (int i = 0; i < outputPorts.length; i++) {
			DefaultMutableTreeNode onode = new DefaultMutableTreeNode(outputPorts[i]);
			outputs.add(onode);
		    }

		    // Create a new node for processors.
		    DefaultMutableTreeNode processors = new DefaultMutableTreeNode("Processors");
		    this.root.add(processors);
		    // Populate from the processor list
		    Processor[] p = model.getProcessors();
		    for (int i = 0; i<p.length; i++) {
			DefaultMutableTreeNode pnode = new DefaultMutableTreeNode(p[i]);
			processors.add(pnode);
			// For each processor, add the port list
			Port[] ports = p[i].getPorts();
			for (int j = 0; j<ports.length; j++) {
			    DefaultMutableTreeNode portnode = new DefaultMutableTreeNode(ports[j]);
			    pnode.add(portnode);
			}
		    }	
	    
		    // Create a new node for data links
		    DefaultMutableTreeNode datalinks = new DefaultMutableTreeNode("Data links");
		    this.root.add(datalinks);
		    // Populate from the list of data links
		    DataConstraint[] dc = model.getDataConstraints();
		    for (int i = 0; i<dc.length; i++) {
			DefaultMutableTreeNode dcnode = new DefaultMutableTreeNode(dc[i]);
			datalinks.add(dcnode);
		    }


		    // If the status has been set to '2' while we were running
		    // then go around again. If it's still '1' we can exit safely
		    // as nothing else wants to update the state. Cheap way of doing
		    // update controls :)
		    if (this.regenerationStatus == 1) {
			this.regenerationStatus = 0;
		    }
		}
		return;
	    }
	    else if (this.regenerationStatus == 1) {
		// flag that the regeneration process should be called again.
		this.regenerationStatus = 2;
	    }
	    
	}
    }
    
}
/**
 * A cell renderer that paints the appropriate icons depending on the
 * component of the model being displayed.
 * @author Tom Oinn
 */
class ScuflModelExplorerRenderer extends DefaultTreeCellRenderer {
    
    static ImageIcon wsdlicon, soaplabicon, talismanicon, inputicon, outputicon, inputporticon, outputporticon, datalinkicon;
    
    static {
	// Load the image files found in this package into the class.
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflModelExplorerRenderer");
	    wsdlicon = new ImageIcon(c.getResource("wsdl.gif"));
	    talismanicon = new ImageIcon(c.getResource("talisman.gif"));
	    soaplabicon = new ImageIcon(c.getResource("soaplab.gif"));
	    inputporticon = new ImageIcon(c.getResource("inputport.gif"));
	    outputporticon = new ImageIcon(c.getResource("outputport.gif"));
	    datalinkicon = new ImageIcon(c.getResource("datalink.gif"));
	    inputicon = new ImageIcon(c.getResource("input.gif"));
	    outputicon = new ImageIcon(c.getResource("output.gif"));
	}
	catch (ClassNotFoundException cnfe) {
	    //
	}
    }
    
    /**
     * Return a custom renderer to draw the cell correctly for each node type
     */
    public Component getTreeCellRendererComponent(JTree tree,
						  Object value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus) {
	super.getTreeCellRendererComponent(tree, value, sel,
					   expanded, leaf, row,
					   hasFocus);
	Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
	if (userObject instanceof Processor) {
	    if (userObject instanceof WSDLBasedProcessor) {
		setIcon(wsdlicon);
	    }
	    else if (userObject instanceof TalismanProcessor) {
		setIcon(talismanicon);
	    }
	    else if (userObject instanceof SoaplabProcessor) {
		setIcon(soaplabicon);
	    }
	}
	else if (userObject instanceof Port) {
	    Port thePort = (Port)userObject;
	    Processor theProcessor = thePort.getProcessor();
	    ScuflModel model = theProcessor.getModel();
	    if (theProcessor == model.getWorkflowSourceProcessor()) {
		// Workflow source port
		setIcon(inputicon);
	    }
	    else if (theProcessor == model.getWorkflowSinkProcessor()) {
		// Workflow sink port
		setIcon(outputicon);
	    }
	    else {
		// Normal port
		if (thePort instanceof InputPort) {
		    setIcon(inputporticon);
		}
		else if (thePort instanceof OutputPort) {
		    setIcon(outputporticon);
		}
	    }
	}
	else if (userObject instanceof DataConstraint) {
	    setIcon(datalinkicon);
	}
	return this;
    }
}
/**
 * A class to handle popup menus on nodes on the tree
 * @author Tom Oinn
 */
class ScuflModelExplorerPopupHandler extends MouseAdapter {
    
    private ScuflModelExplorer explorer;
    
    public ScuflModelExplorerPopupHandler(ScuflModelExplorer theExplorer) {
	this.explorer = theExplorer;
    }
   
    /**
     * Handle the mouse pressed event in case this is the platform
     * specific trigger for a popup menu
     */
    public void mousePressed(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    doEvent(e);
	}
    }
    
    /**
     * Similarly handle the mouse released event
     */
    public void mouseReleased(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    doEvent(e);
	}
    }

    /**
     * If the event was a trigger for a popup then use the ScuflContextMenuFactory
     * to find a suitable JPopupMenu for the node that was clicked on and display
     * it. If we couldn't find anything suitable do nothing, all it means is that
     * there wasn't a menu available for that type of node.
     */
    void doEvent(MouseEvent e) {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)(explorer.getPathForLocation(e.getX(), e.getY()).getLastPathComponent());
	Object scuflObject = node.getUserObject();
	if (scuflObject != null) {
	    try {
		JPopupMenu theMenu = ScuflContextMenuFactory.getMenuForObject(scuflObject);
		theMenu.show(explorer, e.getX(), e.getY());
	    }
	    catch (NoContextMenuFoundException ncmfe) {
		// just means that there wasn't a suitable menu for the selected node.
	    }
	}
    }

}
