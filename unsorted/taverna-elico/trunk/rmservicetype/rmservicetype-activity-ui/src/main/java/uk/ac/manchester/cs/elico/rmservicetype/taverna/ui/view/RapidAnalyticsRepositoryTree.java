package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

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
		System.out.println(" path " + myObjects[0] + " , " + myObjects[1]);
		
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
	
	        System.out.println("The user has finished editing the node.");
	        System.out.println("New value: " + node.getUserObject());
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