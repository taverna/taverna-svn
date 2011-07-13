package uk.ac.manchester.cs.elico.utilities.repositorybrowser;

import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

public class RapidAnalyticsRepositoryTree extends JPanel {

	protected DefaultMutableTreeNode myRootTreeNode;
	protected DefaultTreeModel myTreeModel;
	protected JTree myTree;
    private Toolkit myToolkit = Toolkit.getDefaultToolkit();

	public RapidAnalyticsRepositoryTree() {

		super(new GridLayout(1,0));
		
		myRootTreeNode = new DefaultMutableTreeNode("RapidAnalytics Repository");
		myTreeModel = new DefaultTreeModel(myRootTreeNode);
		myTreeModel.addTreeModelListener(new MyTreeModelListener());		
		myTree = new JTree(myTreeModel);
		myTree.setEditable(true);
		myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		JScrollPane scrollPane = new JScrollPane(myTree);
		add(scrollPane);
		
	}
	
	public void removeCurrentNode() {
		
		TreePath currentSelection = myTree.getSelectionPath();
		
		if (currentSelection != null) {
			
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
			MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
			
			if (parent != null) {
				
				myTreeModel.removeNodeFromParent(currentNode);
				return;
				
			}	
		}
		
		myToolkit.beep();
		
	}
	
	public DefaultMutableTreeNode getNodeAt(TreePath path) {
		
		Object [] myObjects = path.getPath();
		//[debug]System.out.println(" path " + myObjects[0] + " , " + myObjects[1]);
		
		//	for (Enumeration e = myRootTreeNode.children(); e.hasMoreElements();) {
					
		//	}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		return node;
		
	}
	
	public DefaultMutableTreeNode searchNode(String nodeStr) {
		
	    DefaultMutableTreeNode node = null;
	    
	    Enumeration e = myRootTreeNode.breadthFirstEnumeration();
	    
	    while (e.hasMoreElements()) {
	    	
	      node = (DefaultMutableTreeNode) e.nextElement();
	      
	      if (nodeStr.equals(node.getUserObject().toString())) {
	    	  
	        return node;
	        
	      }
	      
	    }
	    
	    return null;
	    
	  }
	
	public DefaultMutableTreeNode addObject(Object child) {
		
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = myTree.getSelectionPath();
		
		if (parentPath == null) {
			
			parentNode = myRootTreeNode;
			
		} else {
			
			parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
			
		}
		
		return addObject(parentNode, child, true);
		
	}
	 
	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child) {
		
		return addObject(parent, child, false);
		
	}
	
	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
		
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
		
		if (parent == null) {
			
			parent = myRootTreeNode;
			
		}
		
		myTreeModel.insertNodeInto(childNode, parent, parent.getChildCount());
		
		if (shouldBeVisible) {
			
			myTree.scrollPathToVisible(new TreePath(childNode.getPath()));
			
		}
		return childNode;
		
	}
	
	
	public void clearNodes() { 
		
		myRootTreeNode.removeAllChildren();
		myTreeModel.reload();
		
	}
	
	class MyTreeModelListener implements TreeModelListener {
		
	    public void treeNodesChanged(TreeModelEvent e) {
	        DefaultMutableTreeNode node;
	        node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());
	
	        /*
	         * 		If the event lists children, then the changed
	         *		 node is the child of the node we've already
	         * 			gotten.  Otherwise, the changed node and the
	         * 				specified node are the same.
	         */
	
	            int index = e.getChildIndices()[0];
	            node = (DefaultMutableTreeNode)(node.getChildAt(index));
	
	          //[debug]System.out.println("The user has finished editing the node.");
	          //[debug]System.out.println("New value: " + node.getUserObject());
	    }
	    
	    public void treeNodesInserted(TreeModelEvent e) 
	    {
	    }
	    
	    public void treeNodesRemoved(TreeModelEvent e) 
	    {
	    }
	    
	    public void treeStructureChanged(TreeModelEvent e) 
	    {
	    }
	    
	}
}