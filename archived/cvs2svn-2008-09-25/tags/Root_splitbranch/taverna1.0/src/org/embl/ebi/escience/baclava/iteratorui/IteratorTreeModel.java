/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.iteratorui;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.embl.ebi.escience.baclava.BaclavaIteratorNode;
import org.embl.ebi.escience.baclava.JoinIteratorNode;

/**
 * A TreeModel designed to contain iterator nodes
 * 
 * @author Tom Oinn
 */
public class IteratorTreeModel extends DefaultTreeModel {

	public IteratorTreeModel() {
		super(new JoinIteratorNode());
		addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
				//
			}

			public void treeNodesInserted(TreeModelEvent e) {
				//
			}

			public void treeNodesRemoved(TreeModelEvent e) {
				// If a node is removed, all children that are
				// instances of BaclavaIterator should be re-attached
				// to the root node to prevent them getting lost
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) (e
						.getTreePath().getLastPathComponent());
				if (n instanceof BaclavaIteratorNode == false) {
					Enumeration en = (n.depthFirstEnumeration());
					Set nodesToRescue = new HashSet();
					while (en.hasMoreElements()) {
						DefaultMutableTreeNode m = (DefaultMutableTreeNode) en
								.nextElement();
						if (m instanceof BaclavaIteratorNode) {
							nodesToRescue.add(m);
						}
					}
					for (Iterator i = nodesToRescue.iterator(); i.hasNext();) {
						DefaultMutableTreeNode m = (DefaultMutableTreeNode) i
								.next();
						IteratorTreeModel.this.removeNodeFromParent(m);
						IteratorTreeModel.this.insertNodeInto(m,
								(MutableTreeNode) IteratorTreeModel.this
										.getRoot(), 0);
					}
				}
			}

			public void treeStructureChanged(TreeModelEvent e) {
				//
			}
		});
	}

	/**
	 * Performs transformations on the tree, removing redundant nodes.
	 */
	public synchronized void normalize() {
		boolean finished = false;
		while (!finished) {
			finished = true;
			Enumeration e = ((DefaultMutableTreeNode) getRoot())
					.breadthFirstEnumeration();
			while (e.hasMoreElements() && finished == true) {
				MutableTreeNode n = (MutableTreeNode) e.nextElement();
				// Check whether anything needs doing

				// Check for collation nodes with no children
				if (!(n.isLeaf()) && n.getParent() != null
						&& n.getChildCount() == 0) {
					// Remove the node from its parent and set finished to false
					removeNodeFromParent(n);
					finished = false;
				} else if (!(n.isLeaf()) && n.getParent() != null
						&& n.getChildCount() == 1) {
					// Is a collation node with a single child, and therefore
					// pointless.
					// Replace it with the child node
					MutableTreeNode child = (MutableTreeNode) n.getChildAt(0);
					MutableTreeNode parent = (MutableTreeNode) n.getParent();
					removeNodeFromParent(n);
					insertNodeInto(child, parent, 0);
					finished = false;
				} else if (n.getParent() == null && n.getChildCount() == 1) {
					// Is the root node but with only one child, so must
					// be a collation node and have no effect on the iterator
					MutableTreeNode child = (MutableTreeNode) n.getChildAt(0);
					removeNodeFromParent(child);
					setRoot(child);
					finished = false;
				}
			}
		}
	}
}
