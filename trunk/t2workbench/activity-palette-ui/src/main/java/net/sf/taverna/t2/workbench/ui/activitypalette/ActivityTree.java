package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.util.List;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.partition.Partition;
import net.sf.taverna.t2.partition.Query;
import net.sf.taverna.t2.partition.RootPartition;

public class ActivityTree extends JTree {

	private List<Query<?>> queryList;

	public ActivityTree(TreeModel newModel) {
		super(newModel);
		setEditable(false);
		setExpandsSelectedPaths(false);
		setDragEnabled(false);
		setScrollsOnExpand(false);
		addQueries(this.getModel());
	}

	private void addQueries(TreeModel model) {
		queryList = ((RootPartition) model).getSetModelChangeListener().getQueries();
	}

	public void doQueries() {
		for (Query<?> query : queryList) {
			query.doQuery();
		}
	}

	@Override
	public void setModel(TreeModel model) {
		if (treeModel == model)
			return;
		if (treeModelListener == null)
			treeModelListener = new TreeModelHandler() {
				@Override
				public void treeNodesInserted(final TreeModelEvent ev) {
					if (ev.getChildren()[0] instanceof Partition == false) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								TreePath path = ev.getTreePath();
								setExpandedState(path, true);
								fireTreeExpanded(path);
							}
						});
					}

				}

			};
		if (model != null) {
			model.addTreeModelListener(treeModelListener);
		}
		TreeModel oldValue = treeModel;
		treeModel = model;
		if (queryList != null) {
			doQueries();
		}
		firePropertyChange(TREE_MODEL_PROPERTY, oldValue, model);
	}

}
