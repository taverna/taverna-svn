/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import org.embl.ebi.escience.scufl.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;

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
	this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	this.putClientProperty("JTree.lineStyle","Angled");
	ScuflModelExplorerRenderer renderer = new ScuflModelExplorerRenderer();
	this.setCellRenderer(renderer);
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
class ScuflModelExplorerRenderer extends DefaultTreeCellRenderer {
    
    public ScuflModelExplorerRenderer() {
	super();
    }
    
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
		setText("<html><font color=\"#00aa00\">[WSDL]:"+((Processor)userObject).getName()+"</font></html>");
	    }
	    else if (userObject instanceof TalismanProcessor) {
		setText("<html><font color=\"#aa00aa\">[Talisman]:"+((Processor)userObject).getName()+"</font></html>");
	    }
	    else if (userObject instanceof SoaplabProcessor) {
		setText("<html><font color=\"#aaaa00\">[Soaplab]:"+((Processor)userObject).getName()+"</font></html>");
	    }
	}
	else if (userObject instanceof Port) {
	    if (userObject instanceof InputPort) {
		setText("<html><font color=\"#aa0000\">"+((Port)userObject).getName()+"</font></html>");
	    }
	    else if (userObject instanceof OutputPort) {
		setText("<html><font color=\"#0000aa\">"+((Port)userObject).getName()+"</font></html>");
	    }
	}
	else if (userObject instanceof DataConstraint) {
	    setText("<html><font color=\"#666666\">"+((DataConstraint)userObject).getName()+"</font></html>");
	}

	return this;
    }



}
