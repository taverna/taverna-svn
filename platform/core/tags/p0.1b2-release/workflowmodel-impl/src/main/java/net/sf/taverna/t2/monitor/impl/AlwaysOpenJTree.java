/**
 * 
 */
package net.sf.taverna.t2.monitor.impl;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.monitor.MonitorNode;

/**
 * Simple subclass of JTree that responds to node creation events on its
 * underlying tree model by ensuring that the new node is visible.
 * 
 * @author Tom Oinn
 * 
 */
class AlwaysOpenJTree extends JTree {

	private static final long serialVersionUID = -3769998854485605447L;

	public AlwaysOpenJTree(TreeModel newModel) {
		super(newModel);
		setRowHeight(18);
		setLargeModel(true);
		setEditable(false);
		setExpandsSelectedPaths(false);
		setDragEnabled(false);
		setScrollsOnExpand(false);
		setSelectionModel(EmptySelectionModel.sharedInstance());
		setCellRenderer(new CellRenderer());
	}

	@Override
	public void setModel(TreeModel model) {
		if (treeModel == model)
			return;
		if (treeModelListener == null)
			treeModelListener = new TreeModelListener();
		if (model != null) {
			model.addTreeModelListener(treeModelListener);
		}
		TreeModel oldValue = treeModel;
		treeModel = model;
		firePropertyChange(TREE_MODEL_PROPERTY, oldValue, model);
	}

	protected class CellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 7106767124654545039L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			if (value instanceof MonitorNodeImpl) {
				MonitorNode mn = (MonitorNode) value;
				if (mn.hasExpired()) {
					setEnabled(false);
				}
			}
			return this;
		}
	}

	protected class TreeModelListener extends TreeModelHandler {
		@Override
		public void treeNodesInserted(final TreeModelEvent ev) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					TreePath path = ev.getTreePath();
					setExpandedState(path, true);
					fireTreeExpanded(path);
				}
			});
		}

		@Override
		public void treeStructureChanged(final TreeModelEvent ev) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					TreePath path = ev.getTreePath();
					setExpandedState(path, true);
					fireTreeExpanded(path);
				}
			});
		}
	}
}