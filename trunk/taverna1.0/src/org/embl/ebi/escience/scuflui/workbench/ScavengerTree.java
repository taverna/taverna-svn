/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTreePopupHandler;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTreeRenderer;
import java.lang.String;



/**
 * A JTree subclass showing available processors from some
 * set of external libraries or searches. Nodes corresponding
 * to a single potential processor instance should contain
 * a user object implementing ProcessorFactory.
 * @author Tom Oinn
 */
public class ScavengerTree extends JTree 
    implements ScuflUIComponent {
    
    /** 
     * The model that this scavenger will create processor for 
     */
    protected ScuflModel model = null;
    
    /**
     * The root node
     */
    DefaultMutableTreeNode root = null;

    /**
     * The tree model
     */
    DefaultTreeModel treeModel = null;

    /**
     * A private count to avoid name duplication on created nodes
     */
    private int count = 0;
    
    /**
     * Get the next available count and increment the counter
     */
    public int getNextCount() {
	return count++;
    }

    /**
     * Create a new scavenger tree
     */
    public ScavengerTree() {
	super();
	root = new DefaultMutableTreeNode("Available Processors");
	treeModel = (DefaultTreeModel)this.getModel();
	treeModel.setRoot(this.root);
	putClientProperty("JTree.lineStyle","Angled");
	getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	ScavengerTreeRenderer renderer = new ScavengerTreeRenderer();
	this.setCellRenderer(renderer);
	this.addMouseListener(new ScavengerTreePopupHandler(this));
    }

    /**
     * Add a new scavenger to the tree, firing appropriate
     * model events as we do.
     */
    public void addScavenger(Scavenger theScavenger) {
	this.treeModel.insertNodeInto(theScavenger, 
				      (MutableTreeNode)this.treeModel.getRoot(),
				      this.treeModel.getChildCount(this.treeModel.getRoot()));
	// Set the visibility sensibly so that the root node
	// is expanded and visible
	TreePath path = new TreePath(this.root);
	expandPath(path);
    }
    
    /**
     * Listen for model bind requests to set the internal
     * ScuflModel field
     */
    public void attachToModel(ScuflModel theModel) {
	this.model = theModel;
    }

    /**
     * When unbound from a model, set internal model field
     * to null
     */
    public void detachFromModel() {
	this.model = null;
    }

    /**
     * Return an apppropriate title for windows
     */
    public String getName() {
	return "Available services";
    }

}
    
