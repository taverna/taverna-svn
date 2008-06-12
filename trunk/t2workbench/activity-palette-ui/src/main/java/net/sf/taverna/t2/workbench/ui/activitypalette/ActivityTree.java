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
import net.sf.taverna.t2.partition.SetModelChangeListener;

/**
 * Contains all the activities. Has a {@link TreeModel} based on
 * {@link Partition} (actually a {@link RootPartition}) so that the items in
 * the tree can be filtered and re-ordered. When the {@link TreeModel} is
 * updated the {@link Query}s are rerun which will update the display
 * 
 * @author Ian Dunlop
 * 
 */
public class ActivityTree extends JTree {

	/** A query for each type of activity */
	private List<Query<?>> queryList;

	public ActivityTree(TreeModel newModel) {
		super(newModel);
		setEditable(false);
		setExpandsSelectedPaths(false);
		setDragEnabled(false);
		setScrollsOnExpand(false);
		addQueries(this.getModel());
	}

	/**
	 * When the tree is first created it gets all the {@link Query}s from the
	 * {@link SetModelChangeListener} belonging to the {@link RootPartition} ie.
	 * its {@link TreeModel}. This means that when the model is updated ie.
	 * when the user wants to filter the activities the tree itself can re-fire
	 * the queries to update the display
	 * 
	 * @param model
	 */
	private void addQueries(TreeModel model) {
		queryList = ((RootPartition) model).getSetModelChangeListener()
				.getQueries();
		doQueries();
	}

	/**
	 * Ensures that the {@link Query}s are listening to the
	 * {@link RootPartition} when the user wants to filter the activities. Calls
	 * {@link Query#doQuery()} for each query to get the display to show the
	 * user selected filter.
	 */
	private void doQueries() {
		for (Query<?> query : queryList) {
			query
					.addSetModelChangeListener((SetModelChangeListener) ((RootPartition) this.treeModel)
							.getSetModelChangeListener());
		}
		for (Query<?> query : queryList) {
			query.doQuery();
		}
	}

	@Override
	/**
	 * Resets the model which means that the user selected filter has probably
	 * changed so it calls doQuery to get the display to change
	 */
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
