/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.embl.ebi.escience.scufl.CrossNode;
import org.embl.ebi.escience.scufl.DotNode;
import org.embl.ebi.escience.scufl.IterationStrategy;
import org.embl.ebi.escience.scufl.LeafNode;

/**
 * A control panel for the iteration tree editor allowing the user to manipulate
 * the tree, removing and adding nodes into the tree based on the context.
 * 
 * @author Tom Oinn
 */
public class IterationStrategyEditorControl extends JPanel {

	private IterationStrategy strategy;

	private IterationStrategyEditor tree;

	private JButton addCross, addDot, normalize, remove, change;

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
		addCross = new JButton("Add Cross",
				IterationStrategyEditor.joinIteratorIcon);
		addCross.setHorizontalAlignment(SwingConstants.LEFT);
		addDot = new JButton("Add Dot",
				IterationStrategyEditor.lockStepIteratorIcon);
		addDot.setHorizontalAlignment(SwingConstants.LEFT);
		normalize = new JButton("Normalize");
		normalize.setHorizontalAlignment(SwingConstants.LEFT);
		remove = new JButton("Remove node", TavernaIcons.deleteIcon);
		remove.setHorizontalAlignment(SwingConstants.LEFT);

		change = new JButton("Switch to...",
				IterationStrategyEditor.joinIteratorIcon);
		change.setHorizontalAlignment(SwingConstants.LEFT);

		// Set the default enabled state to off on all buttons other than the
		// normalize
		// one.
		remove.setEnabled(false);
		addCross.setEnabled(false);
		addDot.setEnabled(false);
		change.setEnabled(false);

		// Create a layout with the tree on the right and the buttons in a grid
		// layout
		// on the left
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		// toolbar.setLayout(new GridLayout(2,2));
		toolbar.add(normalize);
		toolbar.add(addCross);
		toolbar.add(addDot);
		toolbar.add(remove);
		toolbar.add(change);
		toolbar.setAlignmentX(LEFT_ALIGNMENT);

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
				DefaultTreeModel model = (DefaultTreeModel) IterationStrategyEditorControl.this.tree
						.getModel();
				model.insertNodeInto(newNode,
						IterationStrategyEditorControl.this.selectedNode, 0);
			}
		});

		// Add a cross product node as a child of the selected node
		addCross.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MutableTreeNode newNode = new CrossNode();
				DefaultTreeModel model = (DefaultTreeModel) IterationStrategyEditorControl.this.tree
						.getModel();
				model.insertNodeInto(newNode,
						IterationStrategyEditorControl.this.selectedNode, 0);
			}
		});

		// Add a cross product node as a child of the selected node
		change.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode newNode;
				if (selectedNode instanceof CrossNode) {
					newNode = new DotNode();
				} else {
					newNode = new CrossNode();
				}

				while (selectedNode.getChildCount() > 0) {
					newNode.add((MutableTreeNode) selectedNode.getChildAt(0));
				}

				DefaultTreeModel model = (DefaultTreeModel) IterationStrategyEditorControl.this.tree
						.getModel();
				if (selectedNode.getParent() == null) {
					model.setRoot(newNode);
					model.nodeStructureChanged(newNode);
				} else {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode
							.getParent();
					int index = parent.getIndex(selectedNode);
					parent.remove(index);
					parent.insert(newNode, index);
					model.nodeStructureChanged(parent);
				}
				tree.setSelectionPath(new TreePath(newNode.getPath()));
				tree.setAllNodesExpanded();
			}
		});

		// Remove the selected node, moving any descendant leaf nodes
		// to the root to prevent them getting lost
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode nodeToBeRemoved = (DefaultMutableTreeNode) IterationStrategyEditorControl.this.selectedNode;
				// Search through, compile a list of leaf nodes
				// to be pushed back up to the root (can't modify
				// the tree structure whilst iterating over it)
				Set nodesToMove = new HashSet();
				for (Enumeration en = ((DefaultMutableTreeNode) selectedNode)
						.depthFirstEnumeration(); en.hasMoreElements();) {
					Object candidate = en.nextElement();
					if (candidate instanceof LeafNode) {
						nodesToMove.add(candidate);
					}
				}
				// Now remove the candidate nodes from their parents and
				// put them back into the root node
				DefaultTreeModel model = (DefaultTreeModel) IterationStrategyEditorControl.this.tree
						.getModel();
				for (Iterator i = nodesToMove.iterator(); i.hasNext();) {
					MutableTreeNode nodeToMove = (MutableTreeNode) i.next();
					model.removeNodeFromParent(nodeToMove);
					model.insertNodeInto(nodeToMove, (MutableTreeNode) model
							.getRoot(), 0);
				}
				model.removeNodeFromParent(nodeToBeRemoved);

				// Disable the various buttons, as the current selection
				// is now invalid.
				IterationStrategyEditorControl.this.remove.setEnabled(false);
				IterationStrategyEditorControl.this.addCross.setEnabled(false);
				IterationStrategyEditorControl.this.addDot.setEnabled(false);
				IterationStrategyEditorControl.this.change.setEnabled(false);
			}
		});

		// Listen to tree selection events and enable buttons appropriately
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectedPath = e.getPath();
				MutableTreeNode selectedObject = (MutableTreeNode) selectedPath
						.getLastPathComponent();
				IterationStrategyEditorControl.this.selectedNode = selectedObject;
				if (selectedObject instanceof CrossNode
						|| selectedObject instanceof DotNode) {
					if (selectedObject.getParent() == null) {
						IterationStrategyEditorControl.this.remove
								.setEnabled(false);
					} else {
						IterationStrategyEditorControl.this.remove
								.setEnabled(true);
					}
					if (selectedObject instanceof CrossNode) {
						IterationStrategyEditorControl.this.change
								.setText("Change to Dot Product");
						IterationStrategyEditorControl.this.change
								.setIcon(IterationStrategyEditor.lockStepIteratorIcon);
					} else {
						IterationStrategyEditorControl.this.change
								.setText("Change to Cross Product");
						IterationStrategyEditorControl.this.change
								.setIcon(IterationStrategyEditor.joinIteratorIcon);
					}
					IterationStrategyEditorControl.this.addCross
							.setEnabled(true);
					IterationStrategyEditorControl.this.addDot.setEnabled(true);
					IterationStrategyEditorControl.this.change.setEnabled(true);
				} else {
					IterationStrategyEditorControl.this.remove
							.setEnabled(false);
					IterationStrategyEditorControl.this.addCross
							.setEnabled(false);
					IterationStrategyEditorControl.this.addDot
							.setEnabled(false);
					IterationStrategyEditorControl.this.change
							.setEnabled(false);
				}
			}
		});

		// Add components to the control panel
		add(toolbar);
		JScrollPane treePane = new JScrollPane(tree);
		treePane.setPreferredSize(new Dimension(0, 0));
		add(treePane);

	}

}
