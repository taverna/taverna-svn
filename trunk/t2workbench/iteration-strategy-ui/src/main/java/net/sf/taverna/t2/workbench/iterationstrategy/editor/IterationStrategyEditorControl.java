/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.iterationstrategy.IterationStrategyIcons;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

/**
 * A control panel for the iteration tree editor allowing the user to manipulate
 * the tree, removing and adding nodes into the tree based on the context.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public class IterationStrategyEditorControl extends JPanel {

	protected static Set<IterationStrategyNode> descendentsOfNode(
			IterationStrategyNode node) {
		Set<IterationStrategyNode> descendants = new HashSet<IterationStrategyNode>();
		Set<IterationStrategyNode> nodesToVisit = new HashSet<IterationStrategyNode>();
		Set<IterationStrategyNode> visitedNodes = new HashSet<IterationStrategyNode>();

		// Note: Not added to descendants
		nodesToVisit.add(node);
		while (!nodesToVisit.isEmpty()) {
			// pick the first one
			IterationStrategyNode visiting = nodesToVisit.iterator().next();
			visitedNodes.add(visiting);
			nodesToVisit.remove(visiting);

			// Find new and interesting children
			List<IterationStrategyNode> children = visiting.getChildren();
			Set<IterationStrategyNode> newNodes = new HashSet<IterationStrategyNode>(
					children);
			newNodes.removeAll(visitedNodes);

			descendants.addAll(newNodes);
			nodesToVisit.addAll(newNodes);
		}
		return descendants;
	}

	private JButton addCross, addDot, normalize, remove, change;

	private IterationStrategyNode selectedNode = null;

	private IterationStrategyEditor tree;

	/**
	 * Create a new panel from the supplied iteration strategy
	 */
	public IterationStrategyEditorControl(IterationStrategyImpl strategy) {

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// Create the components
		tree = new IterationStrategyEditor(strategy);
		addCross = new JButton("Add Cross",
				IterationStrategyIcons.joinIteratorIcon);
		addCross.setHorizontalAlignment(SwingConstants.LEFT);
		addDot = new JButton("Add Dot",
				IterationStrategyIcons.lockStepIteratorIcon);
		addDot.setHorizontalAlignment(SwingConstants.LEFT);
		normalize = new JButton("Normalize");
		normalize.setHorizontalAlignment(SwingConstants.LEFT);
		remove = new JButton("Remove node", WorkbenchIcons.deleteIcon);
		remove.setHorizontalAlignment(SwingConstants.LEFT);

		change = new JButton("Switch to...",
				IterationStrategyIcons.joinIteratorIcon);
		change.setHorizontalAlignment(SwingConstants.LEFT);

		// Set the default enabled state to off on all buttons other than the
		// normalize
		// one.
		disableButtons();

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
		normalize.addActionListener(new NormalizeAction());

		// Add a dot product node as a child of the selected node
		addDot.addActionListener(new AddDotAction());

		// Add a cross product node as a child of the selected node
		addCross.addActionListener(new AddCrossAction());

		// Add a cross product node as a child of the selected node
		change.addActionListener(new ChangeAction());

		// Remove the selected node, moving any descendant leaf nodes
		// to the root to prevent them getting lost
		remove.addActionListener(new RemoveAction());

		// Listen to tree selection events and enable buttons appropriately
		tree.addTreeSelectionListener(new ButtonEnabler());

		// Add components to the control panel
		add(toolbar);
		JScrollPane treePane = new JScrollPane(tree);
		treePane.setPreferredSize(new Dimension(0, 0));
		add(treePane);
	}

	public void setIterationStrategy(IterationStrategyImpl iterationStrategy) {
		tree.setIterationStrategy(iterationStrategy);
		disableButtons();
		selectNode(null);
	}

	private void disableButtons() {
		remove.setEnabled(false);
		addCross.setEnabled(false);
		addDot.setEnabled(false);
		change.setEnabled(false);
	}

	protected void selectNode(TreeNode newNode) {
		DefaultTreeModel model = tree.getModel();
		if (newNode == null) {
			newNode = (TreeNode) model.getRoot();
		}
		TreeNode[] pathToRoot = model.getPathToRoot(newNode);
		tree.setSelectionPath(new TreePath(pathToRoot));
	}

	private final class AddCrossAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			CrossProduct newNode = new CrossProduct();
			newNode.setParent(selectedNode);
			DefaultTreeModel model = tree.getModel();
			model.nodeStructureChanged(selectedNode);
		}
	}

	private final class AddDotAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			DotProduct newNode = new DotProduct();
			newNode.setParent(selectedNode);
			DefaultTreeModel model = tree.getModel();
			model.nodeStructureChanged(selectedNode);
		}
	}

	private final class ButtonEnabler implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			TreePath selectedPath = e.getPath();
			IterationStrategyNode selectedObject = (IterationStrategyNode) selectedPath
					.getLastPathComponent();
			selectedNode = selectedObject;
			if (selectedObject instanceof CrossProduct
					|| selectedObject instanceof DotProduct) {
				if (selectedObject.getParent() == null) {
					remove.setEnabled(false);
				} else {
					remove.setEnabled(true);
				}
				if (selectedObject instanceof CrossProduct) {
					change.setText("Change to Dot Product");
					change.setIcon(IterationStrategyIcons.lockStepIteratorIcon);
				} else {
					change.setText("Change to Cross Product");
					change.setIcon(IterationStrategyIcons.joinIteratorIcon);
				}
				addCross.setEnabled(true);
				addDot.setEnabled(true);
				change.setEnabled(true);
			} else {
				// Top- or leaf node
				remove.setEnabled(false);
				addCross.setEnabled(false);
				addDot.setEnabled(false);
				change.setEnabled(false);
			}
		}
	}

	private final class ChangeAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			IterationStrategyNode newNode;
			if (selectedNode instanceof CrossProduct) {
				newNode = new DotProduct();
			} else {
				newNode = new CrossProduct();
			}

			List<IterationStrategyNode> children = new ArrayList<IterationStrategyNode>(
					selectedNode.getChildren());
			for (IterationStrategyNode child : children) {
				child.setParent(newNode);
			}

			DefaultTreeModel model = tree.getModel();
			if (selectedNode.getParent() == null) {
				model.setRoot(newNode);
				model.nodeStructureChanged(newNode);
				newNode.setParent(null);
			} else {
				IterationStrategyNode parent = selectedNode.getParent();
				int index = parent.getIndex(selectedNode);
				selectedNode.setParent(null);
				parent.insert(newNode, index);
				model.nodeStructureChanged(parent);
			}

			selectNode(newNode);
			tree.setAllNodesExpanded();
		}

	}

	private final class NormalizeAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// strategy.normalize();
			// Expand all the nodes in the tree
			tree.setAllNodesExpanded();
		}
	}

	private final class RemoveAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			IterationStrategyNode nodeToBeRemoved = selectedNode;

			DefaultTreeModel model = tree.getModel();

			// Now remove the candidate nodes from their parents and
			// put them back into the root node
			IterationStrategyNode root = findRoot();
			if (root == selectedNode) {
				return;
			}
			IterationStrategyNode oldParent = nodeToBeRemoved.getParent();

			for (IterationStrategyNode nodeToMove : descendentsOfNode(nodeToBeRemoved)) {
				nodeToMove.setParent(oldParent);
			}
			nodeToBeRemoved.setParent(null);
			model.nodeStructureChanged(oldParent);
			// Disable the various buttons, as the current selection
			// is now invalid.
			remove.setEnabled(false);
			addCross.setEnabled(false);
			addDot.setEnabled(false);
			change.setEnabled(false);
			selectNode(oldParent);
		}

	}

	private IterationStrategyNode findRoot() {
		IterationStrategyNode root = (IterationStrategyNode) tree.getModel()
				.getRoot();
		if (root.getChildCount() > 0) {
			return root.getChildAt(0);
		}
		return root;
	}

}
