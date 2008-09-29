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

package net.sf.taverna.process;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import java.util.Enumeration;

/**
 * An OperationTree contains the resolved (partially or otherwise)
 * tree of Operation nodes.
 * @author Tom Oinn
 */
public class OperationTree extends DefaultTreeModel {
    
    OperationGroup operationGroup = null;

    /**
     * Create a new OperationTree with the specified Operation object
     * as the root node's user object.
     */
    public OperationTree(Operation rootOperation, OperationGroup operationGroup) {
	super(new OperationTreeNode(rootOperation));
	this.operationGroup = operationGroup;
	((OperationTreeNode)getRoot()).getOperation().operationGroup = operationGroup;
	// Add a new listener - this will respond to node additions and deletions
	// and keep the references in any contained Operation objects consistant
	addTreeModelListener(new TreeModelListener() {
		public void treeNodesChanged(TreeModelEvent e) {
		    // Ignore this, won't have removed or added nodes
		}
		public void treeStructureChanged(TreeModelEvent e) {
		    // Similarly, not paying attention to this
		}
		public void treeNodesInserted(TreeModelEvent e) {
		    OperationTreeNode[] newNodes = (OperationTreeNode[])e.getChildren();
		    for (int i = 0; i < newNodes.length; i++) {
			// Set the operationGroup field on the Operations within each node
			setOperationGroup(newNodes[i], OperationTree.this.operationGroup);
		    }
		}
		public void treeNodesRemoved(TreeModelEvent e) {
		    OperationTreeNode[] newNodes = (OperationTreeNode[])e.getChildren();
		    for (int i = 0; i < newNodes.length; i++) {
			// Set the operationGroup field to null as the node is no longer attached
			// to the parent OperationGroup of this OperationTree
			setOperationGroup(newNodes[i], null);
		    }
		}
		// Recursively set the operation group on the specified node
		private void setOperationGroup(OperationTreeNode node, OperationGroup newGroup) {
		    node.getOperation().operationGroup = newGroup;
		    Enumeration en = node.children();
		    while (en.hasMoreElements()) {
			setOperationGroup((OperationTreeNode)en.nextElement(), newGroup);
		    }
		}
	    });
    }
    
}
