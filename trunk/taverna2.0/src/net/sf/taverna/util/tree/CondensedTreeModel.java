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

package net.sf.taverna.util.tree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * An extension of the DefaultTreeModel providing two additional
 * operations - firstly the ability to add nodes by path where
 * nodes with single children are condensed into a single node
 * containing an Object[] of the child and parent user objects and
 * secondly a filter operation which produces a CondensedTreeModel
 * containing only those leaf nodes which match a particular test.
 * <p>All nodes must be subclasses of DefaultMutableTreeNode for
 * this class to make use of them, if created by the createNode
 * method this is taken care of.
 * @author Tom Oinn
 */
public class CondensedTreeModel extends DefaultTreeModel {
    
    /**
     * Create a new CondensedTreeModel with a single empty
     * default mutable tree node as its root
     */
    public CondensedTreeModel() {
	super(new DefaultMutableTreeNode());
    }

    /**
     * Given an array of user object representing the path from
     * an immediate child of the root node (not the root node
     * itself as we want to allow the creation of multiple subtrees)
     * create and / or return the matching MutableTreeNode object.
     * <p>If there is already a node at the specified location with
     * all intermediate items fulfilling a .equals contract then it
     * is simply returned - this means that there is no way to create
     * duplicate nodes and that the returned node may not contain
     * the exact object in the last item of the array, merely that 
     * it contains an object which is equal to that object.
     */
    public DefaultMutableTreeNode createNode(Object[] userObjects) {
	return doInnerCreateNode(userObjects, (DefaultMutableTreeNode)getRoot());
    }
    private DefaultMutableTreeNode doInnerCreateNode(Object[] userObjects,
						     DefaultMutableTreeNode node) {
	// Is there a child of the specified node which matches the first item
	// in the user object array? Matches are only checked against nodes
	// with an Object[]
	DefaultMutableTreeNode foundNode = null;
	for (int i = 0; i < getChildCount(node) && foundNode == null; i++) {
	    DefaultMutableTreeNode query = (DefaultMutableTreeNode)getChild(node, i);
	    Object o = query.getUserObject();
	    if (userObjects.length > 1) {
		if (o instanceof Object[]) {
		    if (userObjects[0].equals(((Object[])o)[0])) {
			foundNode = query;
		    }
		}
	    }
	}
	// If no node found then we have to either add a new one (if there's
	// a path left i.e. userObjects.length > 1 or check to see if there's
	// an existing child with the same object by equals contract.
	if (foundNode == null) {
	    if (userObjects.length > 1) {
		// Create new array...
		Object[] newArray = new Object[userObjects.length - 1];
		for (int i = 0; i < userObjects.length - 1; i++) {
		    newArray[i] = userObjects[i];
		}
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(newArray);
		insertNodeInto(newChild, node, 0);
		DefaultMutableTreeNode newLeaf = 
		    new DefaultMutableTreeNode(userObjects[userObjects.length]);
		insertNodeInto(newLeaf, newChild, 0);
		return newLeaf;
	    }
	    else {
		// Look for existing child node with the same object
		for (int i = 0; i < getChildCount(node); i++) {
		    DefaultMutableTreeNode query = (DefaultMutableTreeNode)getChild(node, i);
		    if (query.getUserObject().equals(userObjects[0])) {
			return query;
		    }
		}
		// Not found so create and insert
		DefaultMutableTreeNode newLeaf = 
		    new DefaultMutableTreeNode(userObjects[0]);
		insertNodeInto(newLeaf, node, 0);
		return newLeaf;
	    }
	}
	else {
	    // Found a node, so see how far we can walk along its objects before
	    // falling off. If we can get all the way along then remove that number
	    // of items from the head of the user objects array and recurse,
	    // if not then we have to split this node and do complex things with
	    // node children :)
	    Object[] nodeObjects = (Object[])foundNode.getUserObject();
	    int matchingItems = 0;
	    boolean matching = true;
	    for (int i = 0; i < nodeObjects.length && i < userObjects.length && matching; i++) {
		if (nodeObjects[i].equals(userObjects[i])) {
		    matchingItems++;
		}
		else {
		    matching = false;
		}
	    }
	    // Did we hit the end of the node object array? If so we can just recurse
	    // with the node concerned and a shorter array of child objects
	    if (matchingItems == nodeObjects.length) {
		Object[] newUserObjectArray = new Object[userObjects.length - matchingItems];
		for (int i = 0; i + matchingItems < userObjects.length; i++) {
		    newUserObjectArray[i] = userObjects[i + matchingItems];
		}
		return doInnerCreateNode(newUserObjectArray, foundNode);
	    }
	    // Did we hit the end of the user object array? Bad if so as it means that
	    // we're trying to add a subset of the existing tree - highly unlikely
	    // and almost certainly an error condition!
	    if (matchingItems == userObjects.length) {
		throw new RuntimeException("Tree structure error - cannot add a new node that also occurs in an existing node path");
	    }
	    // If we're here then we need to split the found node at the point where
	    // things start failing to match. First get a set of all the current children...
	    DefaultMutableTreeNode[] currentChildren = new DefaultMutableTreeNode[getChildCount(foundNode)];
	    for (int i = 0; i < currentChildren.length; i++) {
		currentChildren[i] = (DefaultMutableTreeNode)getChild(foundNode, i);
	    }
	    // Now create a new array to replace the current found node user object...
	    Object[] newNodeObjects = new Object[matchingItems];
	    for (int i = 0; i < matchingItems; i++) {
		newNodeObjects[i] = nodeObjects[i];
	    }
	    // Also create a new array for the child node after the split...
	    Object[] newChildObjects = new Object[nodeObjects.length - matchingItems];
	    for (int i = 0; i < nodeObjects.length - matchingItems; i++) {
		newChildObjects[i] = nodeObjects[i + matchingItems];
	    }
	    DefaultMutableTreeNode newChildNode = new DefaultMutableTreeNode(newChildObjects);
	    // Set the new user object and message the tree that we've changed it
	    foundNode.setUserObject(newNodeObjects);
	    nodeChanged(foundNode);
	    // Remove all children from the found node
	    for (int i = 0; i < currentChildren.length; i++) {
		removeNodeFromParent(currentChildren[i]);
	    }
	    // Add the new child node
	    insertNodeInto(newChildNode, foundNode, 0);
	    // Add all previous children to the new child node
	    for (int i = 0; i < currentChildren.length; i++) {
		insertNodeInto(currentChildren[i], newChildNode, i);
	    }
	    // Create a new version of the user object array with the first 'matchingItems' objects
	    // removed and recurse on the found node
	    Object[] newUserObjectArray = new Object[userObjects.length - matchingItems];
	    for (int i = 0; i + matchingItems < userObjects.length; i++) {
		newUserObjectArray[i] = userObjects[i + matchingItems];
	    }
	    return doInnerCreateNode(newUserObjectArray, foundNode);
	}
	
    }
    
    /**
     * Get an object path to the specified node, expanding any condensed nodes in the
     * path.
     */
    public Object[] getCondensedPath(DefaultMutableTreeNode node) {
	TreeNode[] treePath = getPathToRoot(node);
	List objectList = new ArrayList();
	for (int i = 1; i < treePath.length - 1; i++) {
	    DefaultMutableTreeNode pathNode = (DefaultMutableTreeNode)treePath[i];
	    try {
		Object[] items = (Object[])pathNode.getUserObject();
		for (int j = 0; j < items.length; j++) {
		    objectList.add(items[j]);
		}
	    }
	    catch (ClassCastException cce) {
		objectList.add(node.getUserObject());
	    }
	}
	objectList.add(((DefaultMutableTreeNode)treePath[treePath.length]).getUserObject());
	return (Object[])objectList.toArray(new Object[0]);
    }

    /**
     * Get a copy of this CondensedTreeModel containing only those leaf nodes
     * which match the specified textual regular expression from their user 
     * object toString methods
     */
    public CondensedTreeModel search(String regex) {
	CondensedTreeModel newModel = new CondensedTreeModel();
	DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
	for (Enumeration en = root.depthFirstEnumeration(); en.hasMoreElements();) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
	    if (node.isLeaf() && 
		node.getUserObject().toString().matches(regex)) {
		newModel.createNode(getCondensedPath(node));
	    }
	}
	return newModel;
    }

    /**
     * Print out the tree model as a string, one line per leaf node
     */
    public String toString() {
	if (getChildCount(getRoot()) == 0) {
	    return "Empty CondensedTreeModel";
	}
	else {
	    StringBuffer sb = new StringBuffer();
	    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)getRoot();
	    for (Enumeration en = rootNode.depthFirstEnumeration(); en.hasMoreElements();) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
		if (node.isLeaf()) {
		    Object[] nodeObjects = getCondensedPath(node);
		    sb.append(nodeObjects[0].toString());
		    for (int i = 1; i < nodeObjects.length; i++) {
			sb.append(":"+nodeObjects[i].toString());
		    }
		    sb.append("\n");
		}
	    }
	    return sb.toString();
	}
    }
    
}
