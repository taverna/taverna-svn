/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.tools.apiconsumer;

import com.sun.javadoc.*;
import javax.swing.tree.*;
import java.util.*;

/**
 * A TreeModel of ClassDoc objects arranged
 * by package structure. The package structure
 * is collapsed down such that packages with only
 * one child node have that child node collapsed
 * into the parent in order to reduce the number
 * of extranous levels in the tree. Leaf nodes
 * contain ClassDoc objects. All nodes are instance
 * of DefaultMutableTreeNode.
 * @author Tom Oinn
 */
public class ClassTreeModel extends DefaultTreeModel {
    
    DefaultMutableTreeNode root;

    public ClassTreeModel(ClassDoc[] classes) {
	super(new DefaultMutableTreeNode("Classes"));
	root = (DefaultMutableTreeNode)getRoot();
	for (int i = 0; i < classes.length; i++) {
	    addNodeToTree(classes[i]);
	}
	collapseRedundantNodes();
	reload();
    }
    
    private void addNodeToTree(ClassDoc classdoc) {
	// Split the name up into an array of strings
	String[] nameParts = classdoc.qualifiedName().split("\\.");
	DefaultMutableTreeNode currentNode = root;
	for (int i = 0; i < nameParts.length; i++) {
	    String part = nameParts[i];
	    // Look for a child of the current node with the
	    // named part
	    Enumeration en = currentNode.children();
	    boolean foundNode = false;
	    while (en.hasMoreElements() && !foundNode) {
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)en.nextElement();
		Object o = childNode.getUserObject();
		if (o instanceof String) {
		    String name = (String)o;
		    if (part.equalsIgnoreCase(name)) {
			foundNode = true;
			currentNode = childNode;
		    }
		}
	    }
	    // If the node was found then fine, otherwise need
	    // to create a new one to represent the part.
	    if (!foundNode) {
		if (i == nameParts.length-1) {
		    // On the last part
		    currentNode.add(new DefaultMutableTreeNode(classdoc));
		}
		else {
		    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(part);
		    currentNode.add(newNode);
		    currentNode = newNode;
		}
	    }
	}
    }
    
    /**
     * Transform the tree to remove nodes that have non-leaf 
     * single children
     */
    private void collapseRedundantNodes() {
	boolean finished = false;
	while (!finished) {
	    // Iterate over all the nodes in the tree until we either run out
	    // in which case we're done, or we hit one that can be collapsed
	    Enumeration en = root.depthFirstEnumeration();
	    boolean nodeModified = false;
	    while (en.hasMoreElements() && !nodeModified) {
		DefaultMutableTreeNode node = 
		    (DefaultMutableTreeNode)en.nextElement();
		if (node.getChildCount() == 1 && node != root) {
		    // Node has only one child, if this child isn't
		    // a leaf node then we can collapse the two together
		    DefaultMutableTreeNode child = 
			(DefaultMutableTreeNode)node.getChildAt(0);
		    if (child.isLeaf() == false) {
			// Non leaf child, so concatenate the two nodes
			String myName = (String)node.getUserObject();
			String childName = (String)child.getUserObject();
			String newName = myName+"."+childName;
			// Copy all the child node's children to this node
			Enumeration grandchildren = child.children();
			int newIndex = 0;
			DefaultMutableTreeNode[] grandchildArray = new DefaultMutableTreeNode[child.getChildCount()];
			while (grandchildren.hasMoreElements()) {
			    DefaultMutableTreeNode grandchild = 
				(DefaultMutableTreeNode)grandchildren.nextElement();
			    grandchildArray[newIndex++] = grandchild;
			}
			removeNodeFromParent(child);
			for (int j = 0; j < grandchildArray.length; j++) {
			    removeNodeFromParent(grandchildArray[j]);
			    insertNodeInto(grandchildArray[j], node, j);
			}
			// Remove the original child node
			nodeModified = true;
			node.setUserObject(newName);
		    }
		}
	    }
	    if (!nodeModified) {
		finished = true;
	    }
	}
    }

}
