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

package net.sf.taverna.data;

import javax.swing.tree.TreeNode;

/**
 * Implements a large subset of the methods from javax.swing.tree.TreeNode
 * by delegation to methods in the DataThing interface.
 * @author Tom Oinn
 */
public abstract class AbstractDataThing implements DataThing {

    /**
     * By definition DataThing nodes are immutable so this is a bit
     * of a non issue but the interface requires it. We define getAllowsChildren
     * to be the logical complement of isLeaf()
     */
    public boolean getAllowsChildren() {
	return !isLeaf();
    }
    
    /**
     * Return true if getDepth() == 0
     */
    public boolean isLeaf() {
	return getDepth() == 0;
    }
    
    /**
     * Use the getRelated method to return
     * the appropriate DataThing object
     */
    public TreeNode getChildAt(int childIndex) {
	int[] nodeIndex = getIndex();
	int[] targetIndex = new int[nodeIndex.length + 1];
	for (int i = 0; i < nodeIndex.length; i++) {
	    targetIndex[i] = nodeIndex[i];
	}
	targetIndex[targetIndex.length - 1] = childIndex;
	try {
	    return getRelated(targetIndex);
	}
	catch (DataThingIndexException dtie) {
	    throw new ArrayIndexOutOfBoundsException(dtie.getMessage());
	}
    }
    
    /**
     * Use getRelated to obtain the parent, return null if this
     * node has no parent (i.e. getIndex().length == 0)
     */
    public TreeNode getParent() {
	int nodeIndex = getIndex();
	if (nodeIndex.length == 0) {
	    return null;
	}
	int targetIndex = new int[nodeIndex.length - 1];
	for (int i = 0; i < targetIndex.length; i++) {
	    targetIndex[i] = nodeIndex[i];
	}
	try {
	    return getRelated(targetIndex);
	}
	catch (DataThingIndexException dtie) {
	    throw new ArrayIndexOutOfBoundsException(dtie.getMessage());
	}
    }

    /**
     * Compare the indices of the node to locate the child index. Note
     * that this doesn't actually check whether the root nodes of each
     * node are the same so isn't valid if used to compare two nodes from
     * different collection structures but it's highly unlikely anything
     * will ever call this method in this way so we probably don't risk 
     * too much by not checking this and it's potentially quite an expensive
     * test.<p>
     * If the supplied node isn't an instance of DataThing then this always
     * returns -1
     */
    public int getIndex(TreeNode node) {
	if (node instanceof DataThing == false) {
	    return -1;
	}
	DataThing thing = (DataThing)node;
	int[] childIndex = thing.getIndex();
	int[] nodeIndex = getIndex();
	if (childIndex.length != nodeIndex.length + 1) {
	    return -1;
	}
	return childIndex[childIndex.length - 1];
    }

}
