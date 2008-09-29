/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.model.ActivitySubsetModel;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.model.SubsetKindConfiguration;
import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.utility.TreeModelAdapter;

import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * @author alanrw
 * 
 */
public final class ActivitySubsetPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2864983295736168567L;

	JTree currentTree;

	JTable currentTable;

	private ScuflModel currentWorkflow = null;

	ActivitySubsetModel subsetModel = null;

	public static HashMap<String, SubsetKindConfiguration> kindConfigurationMap = new HashMap<String, SubsetKindConfiguration>();

	JScrollPane treePane;

	JScrollPane tablePane;

	private long lastModelEvaluation;

	private DefaultTreeModel holdingTree;

	private DefaultTableModel holdingTable;

	private SubsetKindConfiguration getConfiguration() {
		SubsetKindConfiguration result = null;
		String kind = this.subsetModel.getIdent().getKind();
		if (!kindConfigurationMap.containsKey(kind)) {
			result = new SubsetKindConfiguration();
			result.initialiseKeyList(this.subsetModel.getPropertyKeyProfile());
			kindConfigurationMap.put(kind, result);
		} else {
			result = kindConfigurationMap.get(kind);
		}
		return result;
	}

	private List<PropertyKeySetting> getTreeKeySettings() {
		return getConfiguration().getTreeKeySettings();
	}

	private List<PropertyKeySetting> getTableKeySettings() {
		return getConfiguration().getTableKeySettings();
	}

	private DefaultListModel getTreeTableListModel() {
		return getConfiguration().getTreeTableListModel();
	}

	/**
	 * 
	 */
	public void expandAll() {
		for (int i = 0; i <= this.currentTree.getRowCount(); i++) {
			this.currentTree.expandRow(i);
		}
	}

	/**
	 * 
	 */
	public void setModels() {
		SubsetKindConfiguration currentConfiguration = getConfiguration();
		if ((currentConfiguration.getLastChange() > this.lastModelEvaluation)
				|| this.subsetModel.isUpdated()) {
			this.lastModelEvaluation = System.currentTimeMillis();
			this.subsetModel.setUpdated(false);

			final List<PropertyKeySetting> treeKeySettings = getTreeKeySettings();
			if (treeKeySettings.size() == 0) {
				this.remove(this.treePane);
			} else {
				this.currentTree.setModel(this.holdingTree);
				Runnable populateTree = new Runnable() {

					public void run() {
						TreeModel treeModel = createTreeModel(
								ActivitySubsetPanel.this.subsetModel,
								treeKeySettings);
						ActivitySubsetPanel.this.currentTree
								.setModel(treeModel);
						if (ActivitySubsetPanel.this.treePane.getParent() == null) {
							if (ActivitySubsetPanel.this.tablePane.getParent() != null) {
								remove(ActivitySubsetPanel.this.tablePane);
								add(ActivitySubsetPanel.this.treePane);
							}
							add(ActivitySubsetPanel.this.treePane);
						}
					}

				};
				SwingUtilities.invokeLater(populateTree);

			}
			final List<PropertyKeySetting> tableKeySettings = getTableKeySettings();
			if (tableKeySettings.size() == 0) {
				this.remove(this.tablePane);
			} else {
				this.currentTable.setModel(this.holdingTable);
				Runnable populateTable = new Runnable() {

					@SuppressWarnings("unchecked")
					public void run() {
						final TreeModel tableModel = createTreeModel(
								ActivitySubsetPanel.this.subsetModel,
								tableKeySettings);
						ActivitySubsetTableModel activitiesTableModel = new ActivitySubsetTableModel(
								((PropertiedTreeRootNode<ProcessorFactoryAdapter>) tableModel
										.getRoot()));
						ActivitySubsetPanel.this.currentTable
								.setModel(activitiesTableModel);
						if (ActivitySubsetPanel.this.tablePane.getParent() == null) {
							add(ActivitySubsetPanel.this.tablePane);
						}
					}

				};
				SwingUtilities.invokeLater(populateTable);
			}
			this.repaint();
			this.validate();
		}
	}

	/**
	 * @param subsetModel
	 * @param theKeySettings
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static TreeModel createTreeModel(
			final ActivitySubsetModel subsetModel,
			final List<PropertyKeySetting> theKeySettings) {
		if (subsetModel == null) {
			throw new NullPointerException("subsetModel cannot be null"); //$NON-NLS-1$
		}
		if (theKeySettings == null) {
			throw new NullPointerException("theKeySettings cannot be null"); //$NON-NLS-1$
		}
		TreeModel result = null;

		PropertiedTreeModel<ProcessorFactoryAdapter> propertiedTreeModel = ObjectFactory
				.getInstance(PropertiedTreeModel.class);
		propertiedTreeModel.setPropertyKeySettings(theKeySettings);
		propertiedTreeModel.setFilter(subsetModel.getFilter());

		propertiedTreeModel.setPropertiedGraphView(subsetModel
				.getParentActivitySubsetModel().getGraphView());

		propertiedTreeModel.detachFromGraphView();
		result = TreeModelAdapter.untypedView(propertiedTreeModel);
		return result;
	}

	/**
	 * Construct a ActivitySubsetPanel that takes a PropertiedObjectSet
	 * populated with example data and shows it as a JTree. The effects of
	 * altering the PropertiedObjectSet are shown to be mirrored in the JTree.
	 */
	@SuppressWarnings("unchecked")
	public ActivitySubsetPanel(final ActivitySubsetModel subsetModel) {
		if (subsetModel == null) {
			throw new NullPointerException("subsetModel cannot be null"); //$NON-NLS-1$
		}
		this.subsetModel = subsetModel;
		this.lastModelEvaluation = 0;

		this.holdingTree = new DefaultTreeModel(new DefaultMutableTreeNode(
				"Please wait")); //$NON-NLS-1$
		this.holdingTable = new DefaultTableModel(
				new String[] { "Please wait" }, 0); //$NON-NLS-1$
		this.setName(subsetModel.getName());
		setLayout(new GridLayout(1, 2));
		this.setPreferredSize(new Dimension(0, 0));

		this.currentTree = new ActivitySubsetTree();
		this.currentTree.setRowHeight(0);
		this.currentTree.setLargeModel(true);
		this.currentTree.setCellRenderer(new ActivityTreeCellRenderer(
				getTreeTableListModel(), subsetModel.getParentActivitySubsetModel()
						.getPropertiedProcessorFactoryAdapterSet()));
		this.currentTree.addMouseListener(new ActivitySubsetListener(this));
		// this.currentTree.setDragEnabled(true);

		this.treePane = new JScrollPane(this.currentTree);

		this.currentTable = new ActivitySubsetTable();

		this.currentTable.addMouseListener(new ActivitySubsetListener(this));

		this.tablePane = new JScrollPane(this.currentTable);
		final ListSelectionModel tableSelectionModel = this.currentTable
				.getSelectionModel();

		this.currentTree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent arg0) {
				TreePath[] paths = arg0.getPaths();
				TableModel currentTableModel = ActivitySubsetPanel.this.currentTable
						.getModel();
				if (currentTableModel instanceof ActivitySubsetTableModel) {
					ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) currentTableModel;
					/**
					 * Note that this has to be done as two separate iterations
					 * because of the tree structure that can cause nodes to be
					 * implicitly selected for removal and addition.
					 */
					for (int i = 0; i < paths.length; i++) {
						PropertiedTreeNode<ProcessorFactoryAdapter> node = (PropertiedTreeNode<ProcessorFactoryAdapter>) paths[i]
								.getLastPathComponent();
						Set<ProcessorFactoryAdapter> allObjects = node
								.getAllObjects();
						if (!arg0.isAddedPath(i)) {
							for (ProcessorFactoryAdapter adapter : allObjects) {
								int rowIndex = tableModel
										.getObjectIndex(adapter);
								tableSelectionModel.removeSelectionInterval(
										rowIndex, rowIndex);
							}
						}
					}
					for (int i = 0; i < paths.length; i++) {
						PropertiedTreeNode<ProcessorFactoryAdapter> node = (PropertiedTreeNode<ProcessorFactoryAdapter>) paths[i]
								.getLastPathComponent();
						Set<ProcessorFactoryAdapter> allObjects = node
								.getAllObjects();
						if (arg0.isAddedPath(i)) {
							for (ProcessorFactoryAdapter adapter : allObjects) {
								int rowIndex = tableModel
										.getObjectIndex(adapter);
								tableSelectionModel.addSelectionInterval(
										rowIndex, rowIndex);
							}
						}
					}
				}
			}
		});

		// JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		// treePane, tablePane);
		// splitPane.setDividerLocation(0.5);
		this.add(this.treePane);
		this.add(this.tablePane);
		// this.add(splitPane, BorderLayout.CENTER);
	}

	/**
	 * 
	 */
	public void collapseAll() {
		for (int i = this.currentTree.getRowCount() - 1; i > 0; i--) {
			this.currentTree.collapseRow(i);
		}
	}

	/**
	 * @return the currentWorkflow
	 */
	public synchronized final ScuflModel getCurrentWorkflow() {
		return this.currentWorkflow;
	}

	/**
	 * @param currentWorkflow
	 *            the currentWorkflow to set, can be null
	 */
	public synchronized final void setCurrentWorkflow(ScuflModel currentWorkflow) {
		this.currentWorkflow = currentWorkflow;
	}

	/**
	 * @return the currentTree
	 */
	public synchronized final JTree getCurrentTree() {
		return this.currentTree;
	}

	/**
	 * 
	 */
	public void destroy() {
		// TODO
	}

	/**
	 * @return
	 */
	public Set<ProcessorFactoryAdapter> getSelectedObjects() {
		Set<ProcessorFactoryAdapter> result = new HashSet<ProcessorFactoryAdapter>();

		int[] selectedRows = this.currentTable.getSelectedRows();
		TableModel currentTableModel = this.currentTable.getModel();
		if ((currentTableModel instanceof ActivitySubsetTableModel)
				&& (selectedRows != null)) {
			ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) currentTableModel;
			for (int i = 0; i < selectedRows.length; i++) {
				int row = selectedRows[i];
				ProcessorFactoryAdapter pf = tableModel.getRowObject(row);
				result.add(pf);
			}
		} else {
			// Work off tree
			TreePath[] selectionPaths = this.currentTree.getSelectionPaths();
			if (selectionPaths != null) {
				for (TreePath selectionPath : selectionPaths) {
					Object objectNode = selectionPath.getLastPathComponent();
					if (objectNode instanceof PropertiedTreeNode) {
						PropertiedTreeNode<ProcessorFactoryAdapter> node = (PropertiedTreeNode<ProcessorFactoryAdapter>) objectNode;
						result.addAll(node.getAllObjects());
					}
				}
			}

		}
		return result;
	}

	/**
	 * @return the subsetModel
	 */
	public synchronized final ActivitySubsetModel getSubsetModel() {
		return this.subsetModel;
	}

	/**
	 * @param subsetModel
	 *            the subsetModel to set
	 */
	public synchronized final void setSubsetModel(
			ActivitySubsetModel subsetModel) {
		this.subsetModel = subsetModel;
	}

}
