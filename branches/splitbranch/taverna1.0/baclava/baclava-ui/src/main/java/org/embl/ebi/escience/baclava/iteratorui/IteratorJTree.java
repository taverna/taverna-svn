/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.iteratorui;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.embl.ebi.escience.baclava.BaclavaIterator;
import org.embl.ebi.escience.baclava.BaclavaIteratorNode;
import org.embl.ebi.escience.baclava.IteratorNodeTests;
import org.embl.ebi.escience.baclava.JoinIteratorNode;
import org.embl.ebi.escience.baclava.LockStepIteratorNode;

/**
 * A JTree placed on top of a tree made of iterator nodes from the baclava
 * package, it allows the user to configure the iteration strategy of the
 * underlying processor.
 * 
 * @author Tom Oinn
 */
public class IteratorJTree extends JTree implements TreeModelListener {

	static {
		try {
			Class c = Class
					.forName("org.embl.ebi.escience.baclava.iteratorui.IteratorJTree");
			joinIteratorIcon = new ImageIcon(c
					.getResource("crossproducticon.png"));
			lockStepIteratorIcon = new ImageIcon(c
					.getResource("dotproducticon.png"));
			baclavaIteratorIcon = new ImageIcon(c
					.getResource("baclavaiteratoricon.png"));
		} catch (Exception ex) {
			//
		}
	}

	private static ImageIcon joinIteratorIcon, lockStepIteratorIcon,
			baclavaIteratorIcon;

	public IteratorJTree(IteratorTreeModel model) {
		super(model);
		model.addTreeModelListener(this);
		setRowHeight(0);
		setCellRenderer(new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, selected,
						expanded, leaf, row, hasFocus);
				if (value instanceof JoinIteratorNode) {
					setIcon(IteratorJTree.joinIteratorIcon);
					JoinIteratorNode n = (JoinIteratorNode) value;
					setText("cross");
				} else if (value instanceof LockStepIteratorNode) {
					setIcon(IteratorJTree.lockStepIteratorIcon);
					LockStepIteratorNode n = (LockStepIteratorNode) value;
					setText("dot");
				} else if (value instanceof BaclavaIteratorNode) {
					setIcon(IteratorJTree.baclavaIteratorIcon);
					setText(((BaclavaIteratorNode) value).getName());
				}
				return this;
			}
		});
		//
	}

	public void treeNodesChanged(TreeModelEvent e) {
		expandAll(this, new TreePath(this.getModel().getRoot()), true);
	}

	public void treeNodesInserted(TreeModelEvent e) {
		expandAll(this, new TreePath(this.getModel().getRoot()), true);
	}

	public void treeNodesRemoved(TreeModelEvent e) {
		expandAll(this, new TreePath(this.getModel().getRoot()), true);
	}

	public void treeStructureChanged(TreeModelEvent e) {
		expandAll(this, new TreePath(this.getModel().getRoot()), true);
	}

	/**
	 * Test the component
	 */
	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame("Iterator tree demo");
			IteratorJTree tree = new IteratorJTree(new IteratorTreeModel());
			frame.getContentPane().add(new JScrollPane(tree));
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			IteratorTreeModel theModel = (IteratorTreeModel) tree.getModel();
			MutableTreeNode root = (MutableTreeNode) theModel.getRoot();

			LockStepIteratorNode n = new LockStepIteratorNode();
			theModel.insertNodeInto(n, root, 0);
			JoinIteratorNode j = new JoinIteratorNode();
			theModel.insertNodeInto(j, n, 0);
			theModel.insertNodeInto(new BaclavaIteratorNode(
					new BaclavaIterator(IteratorNodeTests.colours), "Colours"),
					j, 0);
			theModel.insertNodeInto(new BaclavaIteratorNode(
					new BaclavaIterator(IteratorNodeTests.shapes), "Shapes"),
					j, 0);
			theModel.insertNodeInto(new BaclavaIteratorNode(
					new BaclavaIterator(IteratorNodeTests.animals), "Animals"),
					root, 0);
			expandAll(tree, new TreePath(root), true);
			frame.pack();
			frame.setVisible(true);
			Thread.sleep(2000);
			theModel.normalize();
			Thread.sleep(1000000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static void expandAll(JTree tree, TreePath parent, boolean expand) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			TreeNode n = (TreeNode) e.nextElement();
			TreePath path = parent.pathByAddingChild(n);
			expandAll(tree, path, expand);
		}
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

}
