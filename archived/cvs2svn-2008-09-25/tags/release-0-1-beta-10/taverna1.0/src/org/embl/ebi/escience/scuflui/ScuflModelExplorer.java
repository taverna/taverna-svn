/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.view.*;


// Utility Imports
import java.util.Enumeration;

import org.embl.ebi.escience.scuflui.ScuflModelExplorerPopupHandler;
import org.embl.ebi.escience.scuflui.ScuflModelExplorerRenderer;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import java.lang.String;



/**
 * A swing component that provides an expandable
 * tree view of the constituent components of a
 * ScuflModel instance.
 * @author Tom Oinn
 */
public class ScuflModelExplorer extends JTree 
    implements ScuflModelEventListener,
	       ScuflUIComponent {
    
    // The ScuflModel that this is a view / controller over
    ScuflModel model = null;
    
    TreeModelView treeModel = new TreeModelView();
    
    /**
     * Default constructor, creates a new ScuflModelExplorer that
     * is not bound to any ScuflModel instance. Use the attachToModel
     * method to actually show data in this component.
     */
    public ScuflModelExplorer() {
	super();
	setModel(treeModel);
	// Only allow single selection (not really important but...)
	this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	// Attach the popup menu generator to the tree
	this.addMouseListener(new ScuflModelExplorerPopupHandler(this));
	// Show lines in the tree diagram
	this.putClientProperty("JTree.lineStyle","Angled");
	ScuflModelExplorerRenderer renderer = new ScuflModelExplorerRenderer();
	this.setCellRenderer(renderer);
	//this.addMouseMotionListener(new ScuflModelExplorerDragHandler(this));
	//this.setDragEnabled(true);
    }
    
    public javax.swing.ImageIcon getIcon() {
	return ScuflIcons.windowExplorer;
    }

    /**
     * Set the default expansion state, with all processors, data
     * constraints and workflow source and sink ports show, but
     * nothing else.
     */
    public void setDefaultExpansionState() {
	expandAll(this, new TreePath(this.treeModel.getRoot()), true);
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
	treeModel.attachToModel(theModel);
	theModel.addListener(this.treeModel);
	theModel.addListener(this);
	
	setDefaultExpansionState();
    }

    /**
     * Unbind from the current model, does nothing if 
     * we're not bound to a model.
     */
    public void detachFromModel() {
	if (this.model != null) {
	    this.model.removeListener(this);
	    this.model.removeListener(this.treeModel);
	}
    }
    
    /**
     * Handle events from the model in order to keep up 
     * to date with any changes in state.
     */    
    public void receiveModelEvent(ScuflModelEvent event) {
	setDefaultExpansionState();
    }
    
    /**
     * Return a preferred name for windows containing this component
     */
    public String getName() {
	return "Scufl Model Explorer";
    }
}
