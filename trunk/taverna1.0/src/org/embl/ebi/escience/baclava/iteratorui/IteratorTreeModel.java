/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.iteratorui;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.baclava.*;
import java.util.*;

/**
 * A TreeModel designed to contain iterator nodes
 * @author Tom Oinn
 */
public class IteratorTreeModel extends DefaultTreeModel {
    
    public IteratorTreeModel() {
	super(new JoinIteratorNode());
    }

    /**
     * Performs transformations on the tree, removing
     * redundant nodes.
     */
    public synchronized void normalize() {
	boolean finished = false;
	while (!finished) {
	    finished = true;
	    Enumeration e = ((DefaultMutableTreeNode)getRoot()).breadthFirstEnumeration();
	    while (e.hasMoreElements() && finished == true) {
		MutableTreeNode n = (MutableTreeNode)e.nextElement();
		// Check whether anything needs doing
		
		// Check for collation nodes with no children
		if (!(n.isLeaf()) && n.getParent()!=null && n.getChildCount() == 0) {
		    // Remove the node from its parent and set finished to false
		    removeNodeFromParent(n);
		    finished = false;
		}
		else if (!(n.isLeaf()) && n.getParent() != null && n.getChildCount() == 1) {
		    // Is a collation node with a single child, and therefore pointless.
		    // Replace it with the child node
		    MutableTreeNode child = (MutableTreeNode)n.getChildAt(0);
		    MutableTreeNode parent = (MutableTreeNode)n.getParent();
		    removeNodeFromParent(n);
		    insertNodeInto(child, parent, 0);
		    finished = false;
		}
		else if (n.getParent() == null && n.getChildCount() == 1) {
		    // Is the root node but with only one child, so must
		    // be a collation node and have no effect on the iterator
		    MutableTreeNode child = (MutableTreeNode)n.getChildAt(0);
		    removeNodeFromParent(child);
		    setRoot(child);
		    finished = false;
		}
	    }
	}
    }
}
