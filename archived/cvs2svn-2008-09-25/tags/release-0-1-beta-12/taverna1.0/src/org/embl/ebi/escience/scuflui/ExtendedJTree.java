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
	    TreeNode root = (TreeNode)getModel().getRoot();
	    expandAll(this, new TreePath(root), expand);
	}
    }
    private static void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
	// Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

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
