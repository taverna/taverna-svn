/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.embl.ebi.escience.scufl.*;
import java.util.*;
import java.util.prefs.*;
import java.io.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class ExtendedJTree extends JTree {

    private String pattern = "";

    /**
     * Traverse up the component heirarchy to find a Frame instance and return
     * the first one we can see. Returns null if there's no Frame in the parent
     * list for this component
     */
    public Frame getContainingFrame() {
	Container result = getParent();
	while (result != null && 
	       result instanceof Frame == false) {
	    result = result.getParent();
	}
	return (Frame)result;
    }
    
    /**
     * Set the pattern to highlight
     */
    public void setPattern(String pattern) {
	this.pattern = (pattern == null)?"":pattern;
	// If there's a colouring renderer defined then use it
	// to highlight the term selected.
	if (getCellRenderer() instanceof NodeColouringRenderer) {
	    NodeColouringRenderer renderer = (NodeColouringRenderer)getCellRenderer();
	    if (pattern.equals("")) {
		renderer.setPattern(null);
	    }
	    else {
		renderer.setPattern(".*"+pattern.toLowerCase()+".*");
	    }
	    repaint();
	}
    }

    /**
     * Expand or collapse all nodes, expand if the flag is true and
     * collapse otherwise
     */
    public void setExpansion(boolean expand) {
	synchronized (getModel()) {
	    boolean scrolling = getScrollsOnExpand();
	    setScrollsOnExpand(false);
	    //setRowHeight(-1);
	    if (getCellRenderer() instanceof NodeColouringRenderer) {
		((NodeColouringRenderer)getCellRenderer()).setPlain(true);
	    }
	    TreeNode root = (TreeNode)getModel().getRoot();
	    if (expand) {
		// If the parent is a scrollpane then remove the component
		// and add it after we've finished resizing everything
		//Component c = getParent();
		//System.out.println(c.getClass().toString());
		//if (c instanceof JViewport) {
		//    ((JViewport)c).remove(this);
		//}
		expandAll(this, new TreePath(root));
		//if (c instanceof JViewport) {
		//    ((JViewport)c).add(this);
		    //c.revalidate();
		//}
	    }
	    else {
		collapseAll(this, new TreePath(root));
	    }
	    // Reinstate the old renderer and repaint the tree
	    if (getCellRenderer() instanceof NodeColouringRenderer) {
		((NodeColouringRenderer)getCellRenderer()).setPlain(false);
		repaint();
	    }
	    //setRowHeight(-1);
	    setScrollsOnExpand(scrolling);
	    //repaint();
	}
    }
    private static void collapseAll(JTree tree, TreePath parent) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                collapseAll(tree, path);
            }
        }
	tree.collapsePath(parent);
    }

    private static void expandAll(JTree tree, TreePath parent) {
	TreeNode node = (TreeNode)parent.getLastPathComponent();
	if (node.isLeaf() && tree.isVisible(parent) == false) {
	    tree.makeVisible(parent);
	}
	else {
	    for (Enumeration en = node.children(); en.hasMoreElements(); ) {
		TreeNode child = (TreeNode)en.nextElement();
		expandAll(tree, parent.pathByAddingChild(child));
	    }
	}
    }
    /**
     // Check whether all children are leaf nodes, if so then we expand this
     // path, if not then recurse into all children.
     boolean allLeaves = true;
     for (Enumeration en = node.children(); en.hasMoreElements() && allLeaves; ) {
     TreeNode child = (TreeNode)en.nextElement();
     if (child.isLeaf() == false) {
     allLeaves = false;
     }
     }
     if (allLeaves) {
     tree.expandPath(parent);
     }
     else {
     for (Enumeration en = node.children(); en.hasMoreElements(); ) {
     TreeNode child = (TreeNode)en.nextElement();
     if (child.isLeaf() == false) {
     expandAll(tree, parent.pathByAddingChild(child));
     }
     }
     }
     }
    */

    public void jumpToAndHighlight() {
	// Set the colouring rule
	setPattern(pattern);
	// Collapse everything together before exploding the matching nodes
	setExpansion(false);
	DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
	DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot(); 
	Enumeration en = rootNode.depthFirstEnumeration();
	while (en.hasMoreElements()) {
	    DefaultMutableTreeNode theNode = (DefaultMutableTreeNode)en.nextElement();
	    if (theNode.getUserObject().toString().toLowerCase().matches(".*"+pattern+".*")) {
		TreePath path = new TreePath(treeModel.getPathToRoot(theNode));
		makeVisible(path);
	    }
	}
    }
    

}
