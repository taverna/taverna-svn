/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.utility.TreeModelAdapter;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

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

	List<PropertyKeySetting> fullKeySettings = null;

	private ScuflModel currentWorkflow = null;
	
	private List<PropertyKeySetting> initializeKeySettings(
			final Set<PropertyKey> propertyKeyProfile) {
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting>();

		for (PropertyKey key : propertyKeyProfile) {
			PropertyKeySetting setting = ObjectFactory
					.getInstance(PropertyKeySetting.class);
			setting.setPropertyKey(key);
			keySettings.add(setting);
		}

		return keySettings;
	}

	void expandAll() {
		for (int i = 0; i <= this.currentTree.getRowCount(); i++) {
			this.currentTree.expandRow(i);
		}
	}

	@SuppressWarnings("unchecked")
	TreeModel createTreeModel(final ActivityRegistrySubsetModel subsetModel,
			final List<PropertyKeySetting> theKeySettings) {
		TreeModel result = null;

		PropertiedTreeModel<ProcessorFactory> propertiedTreeModel = ObjectFactory
				.getInstance(PropertiedTreeModel.class);
		propertiedTreeModel.setPropertyKeySettings(theKeySettings);
		propertiedTreeModel.setFilter(subsetModel.getFilter());

		propertiedTreeModel.setPropertiedGraphView(subsetModel
				.getParentRegistry().getGraphView());

		result = TreeModelAdapter.untypedView(propertiedTreeModel);
		return result;
	}

	/**
	 * Construct a ActivitySubsetPanel that takes a PropertiedObjectSet
	 * populated with example data and shows it as a JTree. The effects of
	 * altering the PropertiedObjectSet are shown to be mirrored in the JTree.
	 */
	@SuppressWarnings("unchecked")
	public ActivitySubsetPanel(final ActivityRegistrySubsetModel subsetModel) {
		this.setName(subsetModel.getName());
		setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(0, 0));
		this.fullKeySettings = initializeKeySettings(subsetModel
				.getPropertyKeyProfile());

		TreeModel treeModel = createTreeModel(subsetModel, this.fullKeySettings);
		this.currentTree = new ActivitySubsetTree(treeModel);
		this.currentTree.setRowHeight(0);
		this.currentTree.setLargeModel(true);
		this.currentTree.setCellRenderer(new ActivityTreeCellRenderer());
		this.currentTree.addMouseListener(new ActivitySubsetListener(this));
		this.currentTree.setDragEnabled(true);
//		this.currentTree.setTransferHandler(new ActivitySubsetTransferHandler());
		JScrollPane treePane = new JScrollPane(this.currentTree);
		collapseAll(); // As requested by users

		ActivitySubsetTableModel activitiesTableModel = new ActivitySubsetTableModel(
				((PropertiedTreeRootNode<ProcessorFactory>) treeModel.getRoot()));
		this.currentTable = new ActivitySubsetTable(activitiesTableModel);
		this.currentTable.addMouseListener(new ActivitySubsetListener(this));
		JScrollPane tablePane = new JScrollPane(this.currentTable);
		final ListSelectionModel tableSelectionModel = this.currentTable
				.getSelectionModel();

		this.currentTree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent arg0) {
				TreePath[] paths = arg0.getPaths();
				ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) currentTable
						.getModel();
				/**
				 * Note that this has to be done as two separate iterations
				 * because of the tree structure that can cause nodes to be
				 * implicitly selected for removal and addition.
				 */
				for (int i = 0; i < paths.length; i++) {
					PropertiedTreeNode<ProcessorFactory> node = (PropertiedTreeNode<ProcessorFactory>) paths[i]
							.getLastPathComponent();
					Set<ProcessorFactory> allObjects = node.getAllObjects();
					if (!arg0.isAddedPath(i)) {
						for (ProcessorFactory pf : allObjects) {
							int rowIndex = tableModel.getObjectIndex(pf);
							tableSelectionModel.removeSelectionInterval(
									rowIndex, rowIndex);
						}
					}
				}
				for (int i = 0; i < paths.length; i++) {
					PropertiedTreeNode<ProcessorFactory> node = (PropertiedTreeNode<ProcessorFactory>) paths[i]
							.getLastPathComponent();
					Set<ProcessorFactory> allObjects = node.getAllObjects();
					if (arg0.isAddedPath(i)) {
						for (ProcessorFactory pf : allObjects) {
							int rowIndex = tableModel.getObjectIndex(pf);
							tableSelectionModel.addSelectionInterval(rowIndex,
									rowIndex);
						}
					}
				}
			}
		});


		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				treePane, tablePane);
		splitPane.setDividerLocation(0.5);
		this.add(splitPane, BorderLayout.CENTER);

		Vector<String> keyNames = new Vector<String>();
		for (PropertyKeySetting pks : this.fullKeySettings) {
			PropertyKey pk = pks.getPropertyKey();
			if (pk == null) {
				throw new IllegalStateException("key cannot be null"); //$NON-NLS-1$
			}
			keyNames.add(pk.toString());
		}

		DefaultTableModel keyNamesTableModel = new DefaultTableModel(keyNames,
				0);
		JTable keyNamesTable = new JTable(keyNamesTableModel);
		TableColumnModel columnModel = keyNamesTable.getColumnModel();
		columnModel.addColumnModelListener(new TableColumnModelListener() {

			public void columnAdded(TableColumnModelEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnMarginChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnMoved(TableColumnModelEvent arg0) {
				int fromIndex = arg0.getFromIndex();
				int toIndex = arg0.getToIndex();
				if (fromIndex != toIndex) {
					PropertyKeySetting movedKey = ActivitySubsetPanel.this.fullKeySettings
							.get(fromIndex);
					ActivitySubsetPanel.this.fullKeySettings.remove(fromIndex);
					ActivitySubsetPanel.this.fullKeySettings.add(toIndex,
							movedKey);
					TreeModel newTreeModel = createTreeModel(subsetModel,
							ActivitySubsetPanel.this.fullKeySettings);
					ActivitySubsetPanel.this.currentTree.setModel(newTreeModel);
					collapseAll(); // As requested by users
					ActivitySubsetTableModel newActivitiesTableModel = new ActivitySubsetTableModel(
							((PropertiedTreeRootNode<ProcessorFactory>) newTreeModel
									.getRoot()));
					ActivitySubsetPanel.this.currentTable
							.setModel(newActivitiesTableModel);
				}
			}

			public void columnRemoved(TableColumnModelEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnSelectionChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		this.add(keyNamesTable.getTableHeader(), BorderLayout.NORTH);

		validate();
	}

	void collapseAll() {
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
	 *            the currentWorkflow to set
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

	public void destroy() {
		// TODO
	}

	/**
	 * @return the fullKeySettings
	 */
	public synchronized final List<PropertyKeySetting> getPropertyProfile() {
		return fullKeySettings;
	}

	public Set<ProcessorFactory> getSelectedObjects() {
		Set<ProcessorFactory> result = new HashSet<ProcessorFactory> ();
		int[] selectedRows = currentTable.getSelectedRows();
		ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) currentTable.getModel();
		for (int i = 0; i < selectedRows.length; i++) {
			int row = selectedRows[i];
			ProcessorFactory pf = tableModel.getRowObject(row);
			result.add(pf);
		}
		return result;
	}
}
