/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import org.embl.ebi.escience.scufl.*;

/**
 * A control panel for the iteration tree editor allowing
 * the user to manipulate the tree, removing and adding
 * nodes into the tree based on the context.
 * @author Tom Oinn
 */
public class IterationStrategyEditorControl extends JPanel {
    
    private IterationStrategy strategy;
    private IterationStrategyEditor tree;
    private JButton addCross, addDot, normalize, remove;
    private MutableTreeNode selectedNode = null;

    /**
     * Create a new panel from the supplied iteration strategy
     */
    public IterationStrategyEditorControl(IterationStrategy strategy) {
	
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

	// Set the strategy object for later access
	this.strategy = strategy;

	// Create the components
	tree = new IterationStrategyEditor(strategy);
	addCross = new JButton("Add Cross", IterationStrategyEditor.joinIteratorIcon);
	addCross.setHorizontalAlignment(SwingConstants.LEFT);
	addDot = new JButton("Add Dot", IterationStrategyEditor.lockStepIteratorIcon);
	addDot.setHorizontalAlignment(SwingConstants.LEFT);
	normalize = new JButton("Normalize");
	normalize.setHorizontalAlignment(SwingConstants.LEFT);
	remove = new JButton("Remove node", ScuflIcons.deleteIcon);
	remove.setHorizontalAlignment(SwingConstants.LEFT);

	// Set the default enabled state to off on all buttons other than the normalize
	// one.
	remove.setEnabled(false);
	addCross.setEnabled(false);
	addDot.setEnabled(false);
	
	// Create a layout with the tree on the right and the buttons in a grid layout
	// on the left
	JPanel buttonPanel = new JPanel() {
		public Dimension getMaximumSize() {
		    return new Dimension(99999,40);
		}
	    };
	buttonPanel.setLayout(new GridLayout(2,2));
	buttonPanel.add(normalize);
	buttonPanel.add(remove);
	buttonPanel.add(addCross);
	buttonPanel.add(addDot);
	//buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
	
	// Normalize the tree when the button is pressed
	normalize.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    IterationStrategyEditorControl.this.strategy.normalize();
		    // Expand all the nodes in the tree
		    IterationStrategyEditorControl.this.tree.setAllNodesExpanded();
		}
	    });

	// Add a dot product node as a child of the selected node
	addDot.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    MutableTreeNode newNode = new DotNode();
		    DefaultTreeModel model = (DefaultTreeModel)IterationStrategyEditorControl.this.tree.getModel();
		    model.insertNodeInto(newNode,
					 IterationStrategyEditorControl.this.selectedNode,
					 0);
		}
	    });
	
	// Add a cross product node as a child of the selected node
	addCross.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    MutableTreeNode newNode = new CrossNode();
		    DefaultTreeModel model = (DefaultTreeModel)IterationStrategyEditorControl.this.tree.getModel();
		    model.insertNodeInto(newNode,
					 IterationStrategyEditorControl.this.selectedNode,
					 0);
		}
	    });

	// Remove the selected node, moving any descendant leaf nodes
	// to the root to prevent them getting lost
	remove.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    DefaultMutableTreeNode nodeToBeRemoved = 
			(DefaultMutableTreeNode)IterationStrategyEditorControl.this.selectedNode;
		    // Search through, compile a list of leaf nodes
		    // to be pushed back up to the root (can't modify
		    // the tree structure whilst iterating over it)
		    Set nodesToMove = new HashSet();
		    for (Enumeration en = ((DefaultMutableTreeNode)selectedNode).depthFirstEnumeration(); en.hasMoreElements();) {
			Object candidate = en.nextElement();
			if (candidate instanceof LeafNode) {
			    nodesToMove.add(candidate);
			}
		    }
		    // Now remove the candidate nodes from their parents and
		    // put them back into the root node
		    DefaultTreeModel model = (DefaultTreeModel)IterationStrategyEditorControl.this.tree.getModel();
		    for (Iterator i = nodesToMove.iterator(); i.hasNext();) {
			MutableTreeNode nodeToMove = (MutableTreeNode)i.next();
			model.removeNodeFromParent(nodeToMove);
			model.insertNodeInto(nodeToMove, (MutableTreeNode)model.getRoot(), 0);
		    }
		    model.removeNodeFromParent(nodeToBeRemoved);

		    // Disable the various buttons, as the current selection
		    // is now invalid.
		    IterationStrategyEditorControl.this.remove.setEnabled(false);
		    IterationStrategyEditorControl.this.addCross.setEnabled(false);
		    IterationStrategyEditorControl.this.addDot.setEnabled(false);
		}
	    });

	// Listen to tree selection events and enable buttons appropriately
	tree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    TreePath selectedPath = e.getPath();
		    MutableTreeNode selectedObject = (MutableTreeNode)selectedPath.getLastPathComponent();
		    IterationStrategyEditorControl.this.selectedNode = selectedObject;
		    if (selectedObject instanceof CrossNode || selectedObject instanceof DotNode) {
			if (selectedObject.getParent() == null) {
			    IterationStrategyEditorControl.this.remove.setEnabled(false);
			}
			else {
			    IterationStrategyEditorControl.this.remove.setEnabled(true);
			}
			IterationStrategyEditorControl.this.addCross.setEnabled(true);
			IterationStrategyEditorControl.this.addDot.setEnabled(true);
		    }
		    else {
			IterationStrategyEditorControl.this.remove.setEnabled(false);
			IterationStrategyEditorControl.this.addCross.setEnabled(false);
			IterationStrategyEditorControl.this.addDot.setEnabled(false);
		    }
		}
	    });
	
	// Add components to the control panel
	add(buttonPanel);
	JScrollPane treePane = new JScrollPane(tree);
	treePane.setPreferredSize(new Dimension(0,0));
	add(treePane);
	
    }

}
