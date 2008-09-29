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
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;

// Utility Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTreePopupHandler;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTreeRenderer;
import org.embl.ebi.escience.scuflui.workbench.WSDLBasedScavenger;
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
     * A list of the names of all the scavengers contained within this tree
     */
    ArrayList scavengerList = null;

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
	scavengerList = new ArrayList();
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
     * Examine the model, create any scavengers that would have been required
     * to populate the model with its existing processors. Currently only bothers
     * trying to find WSDL based scavengers
     */
    public void addScavengersFromModel() 
	throws ScavengerCreationException {
	if (this.model != null) {
	    Map wsdlLocations = new HashMap();
	    Processor[] p = model.getProcessors();
	    for (int i = 0; i < p.length; i++) {
		// If the processor is a WSDLBasedProcessor then get
		// the wsdl location and add it to the map.
		if (p[i] instanceof WSDLBasedProcessor) {
		    String wsdlLocation = ((WSDLBasedProcessor)p[i]).getWSDLLocation();
		    wsdlLocations.put(wsdlLocation,null);
		}
	    }
	    // Now iterate over all the wsdl locations found and
	    // create new WSDL scavengers, adding them to the 
	    // scavenger tree.
	    for (Iterator i = wsdlLocations.keySet().iterator(); i.hasNext(); ) {
		String wsdlLocation = (String)i.next();
		addScavenger(new WSDLBasedScavenger(wsdlLocation));
	    }
	}
    }

    /**
     * Add a new scavenger to the tree, firing appropriate
     * model events as we do.
     */
    public void addScavenger(Scavenger theScavenger) {
	// Check to see we don't already have a scavenger with this name
	String newName = theScavenger.getUserObject().toString();
	for (Iterator i = scavengerList.iterator(); i.hasNext(); ) {
	    String name = (String)i.next();
	    if (name.equals(newName)) {
		// Exit if we already have a scavenger by that name
		return;
	    }
	}
	this.scavengerList.add(theScavenger.getUserObject().toString());
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
    
