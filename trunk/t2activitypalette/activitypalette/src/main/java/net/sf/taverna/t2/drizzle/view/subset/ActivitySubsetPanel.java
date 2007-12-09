/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetModel;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.model.SubsetKindConfiguration;
import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;

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

	private ScuflModel currentWorkflow = null;

	private ActivityRegistrySubsetModel subsetModel = null;

	public static HashMap<String, SubsetKindConfiguration> kindConfigurationMap = new HashMap<String, SubsetKindConfiguration>();

	private JScrollPane treePane;

	private JScrollPane tablePane;

	private SubsetKindConfiguration getConfiguration() {
		SubsetKindConfiguration result = null;
		String kind = subsetModel.getIdent().getKind();
		if (!kindConfigurationMap.containsKey(kind)) {
			result = new SubsetKindConfiguration();
			result.initialiseKeyList(subsetModel.getPropertyKeyProfile());
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

	public void expandAll() {
		for (int i = 0; i <= this.currentTree.getRowCount(); i++) {
			this.currentTree.expandRow(i);
		}
	}

	public void setTreeAndTableModels() {
		List<PropertyKeySetting> treeKeySettings = getTreeKeySettings();
		if (treeKeySettings.size() == 0) {
			this.remove(treePane);
		} else {
			TreeModel treeModel = ActivitySubsetKeyTableHeader.createTreeModel(
					subsetModel, treeKeySettings);
			this.currentTree.setModel(treeModel);
			if (treePane.getParent() == null) {
				if (tablePane.getParent() != null) {
					this.remove(tablePane);
					this.add(treePane);
				}
				this.add(treePane);
			}
		}
		List<PropertyKeySetting> tableKeySettings = getTableKeySettings();
		if (tableKeySettings.size() == 0) {
			this.remove(tablePane);
		} else {
			TreeModel tableModel = ActivitySubsetKeyTableHeader
					.createTreeModel(subsetModel, tableKeySettings);
			ActivitySubsetTableModel activitiesTableModel = new ActivitySubsetTableModel(
					((PropertiedTreeRootNode<ProcessorFactoryAdapter>) tableModel
							.getRoot()));
			this.currentTable.setModel(activitiesTableModel);
			if (tablePane.getParent() == null) {
				this.add(tablePane);
			}
		}
		this.repaint();
		this.validate();
	}

	public void setModels() {
		List<PropertyKeySetting> keySettings = getTreeKeySettings();
		synchronized (keySettings) {
			setTreeAndTableModels();
		}
	}

	/**
	 * Construct a ActivitySubsetPanel that takes a PropertiedObjectSet
	 * populated with example data and shows it as a JTree. The effects of
	 * altering the PropertiedObjectSet are shown to be mirrored in the JTree.
	 */
	@SuppressWarnings("unchecked")
	public ActivitySubsetPanel(final ActivityRegistrySubsetModel subsetModel) {
		if (subsetModel == null) {
			throw new NullPointerException("subsetModel cannot be null"); //$NON-NLS-1$
		}
		this.subsetModel = subsetModel;
		this.setName(subsetModel.getName());
		setLayout(new GridLayout(1, 2));
		this.setPreferredSize(new Dimension(0, 0));

		this.currentTree = new ActivitySubsetTree();
		this.currentTree.setRowHeight(0);
		this.currentTree.setLargeModel(true);
		this.currentTree.setCellRenderer(new ActivityTreeCellRenderer(getTreeTableListModel(), subsetModel.getParentRegistry().getRegistry()));
		this.currentTree.addMouseListener(new ActivitySubsetListener(this));
		this.currentTree.setDragEnabled(true);

		treePane = new JScrollPane(this.currentTree);

		this.currentTable = new ActivitySubsetTable();

		this.currentTable.addMouseListener(new ActivitySubsetListener(this));

		tablePane = new JScrollPane(this.currentTable);
		final ListSelectionModel tableSelectionModel = this.currentTable
				.getSelectionModel();

		this.currentTree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent arg0) {
				TreePath[] paths = arg0.getPaths();
				ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) ActivitySubsetPanel.this.currentTable
						.getModel();
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
							int rowIndex = tableModel.getObjectIndex(adapter);
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
							int rowIndex = tableModel.getObjectIndex(adapter);
							tableSelectionModel.addSelectionInterval(rowIndex,
									rowIndex);
						}
					}
				}
			}
		});

		// JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		// treePane, tablePane);
		// splitPane.setDividerLocation(0.5);
		this.add(treePane);
		this.add(tablePane);
		// this.add(splitPane, BorderLayout.CENTER);
	}

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

	public void destroy() {
		// TODO
	}

	public Set<ProcessorFactoryAdapter> getSelectedObjects() {
		Set<ProcessorFactoryAdapter> result = new HashSet<ProcessorFactoryAdapter>();
		int[] selectedRows = this.currentTable.getSelectedRows();
		ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) this.currentTable
				.getModel();
		for (int i = 0; i < selectedRows.length; i++) {
			int row = selectedRows[i];
			ProcessorFactoryAdapter pf = tableModel.getRowObject(row);
			result.add(pf);
		}
		return result;
	}

	/**
	 * @return the subsetModel
	 */
	public synchronized final ActivityRegistrySubsetModel getSubsetModel() {
		return subsetModel;
	}

	/**
	 * @param subsetModel
	 *            the subsetModel to set
	 */
	public synchronized final void setSubsetModel(
			ActivityRegistrySubsetModel subsetModel) {
		this.subsetModel = subsetModel;
	}

}
