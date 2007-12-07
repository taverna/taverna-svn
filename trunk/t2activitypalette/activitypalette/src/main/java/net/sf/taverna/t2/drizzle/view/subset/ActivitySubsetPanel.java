/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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

	JTableHeader keyNamesTableHeader = null;

	private ScuflModel currentWorkflow = null;

	private ActivityRegistrySubsetModel subsetModel = null;

	private static HashMap<String, List<PropertyKeySetting>> keySettingMap = new HashMap<String, List<PropertyKeySetting>>();

	private TreeMap<PropertyKey, JCheckBoxMenuItem> keyToCheckBoxMap = new TreeMap<PropertyKey, JCheckBoxMenuItem>();

	private HashMap<JCheckBoxMenuItem, PropertyKey> checkBoxToKeyMap = new HashMap<JCheckBoxMenuItem, PropertyKey>();

	private boolean listeningToCheckBox = true;

	public List<PropertyKeySetting> getKeySettings() {
		String kind = subsetModel.getIdent().getKind();
		List<PropertyKeySetting> result = null;
		if (keySettingMap.containsKey(kind)) {
			result = keySettingMap.get(kind);
		} else {
			Set<PropertyKey> propertyKeyProfile = subsetModel.getIdent()
					.getPropertyKeyProfile();
			result = new ArrayList<PropertyKeySetting>();

			for (PropertyKey key : propertyKeyProfile) {
				PropertyKeySetting setting = ObjectFactory
						.getInstance(PropertyKeySetting.class);
				setting.setPropertyKey(key);
				result.add(setting);
			}

			keySettingMap.put(kind, result);
		}
		return result;
	}

	List<PropertyKey> getPropertyKeys() {
		List<PropertyKey> result = new ArrayList<PropertyKey>();
		for (PropertyKeySetting pks : getKeySettings()) {
			PropertyKey pk = pks.getPropertyKey();
			if (pk == null) {
				throw new IllegalStateException("key cannot be null"); //$NON-NLS-1$
			}
			result.add(pk);
		}
		return result;
	}

	private List<String> getKeyNames() {
		List<String> result = new ArrayList<String>();
		for (PropertyKeySetting pks : getKeySettings()) {
			PropertyKey pk = pks.getPropertyKey();
			if (pk == null) {
				throw new IllegalStateException("key cannot be null"); //$NON-NLS-1$
			}
			result.add(pk.toString());
		}
		return result;
	}

	public void expandAll() {
		for (int i = 0; i <= this.currentTree.getRowCount(); i++) {
			this.currentTree.expandRow(i);
		}
	}

	public void setTreeAndTableModels() {
		List<PropertyKeySetting> keySettings = getKeySettings();
		synchronized (keySettings) {
			TreeModel treeModel = ActivitySubsetKeyTableHeader.createTreeModel(
					subsetModel, keySettings);
			this.currentTree.setModel(treeModel);
			ActivitySubsetTableModel activitiesTableModel = new ActivitySubsetTableModel(
					((PropertiedTreeRootNode<ProcessorFactory>) treeModel
							.getRoot()));
			this.currentTable.setModel(activitiesTableModel);
		}
	}

	public void setModels() {
		List<PropertyKeySetting> keySettings = getKeySettings();
		synchronized (keySettings) {
			setTreeAndTableModels();

			Vector<String> keyNames = new Vector<String>(getKeyNames());

			DefaultTableModel keyNamesTableModel = new DefaultTableModel(
					keyNames, 0);
			JTable keyNamesTable = new JTable(keyNamesTableModel);
			TableColumnModel columnModel = keyNamesTable.getColumnModel();
			if (this.keyNamesTableHeader == null) {
				this.keyNamesTableHeader = keyNamesTable.getTableHeader();
				ColumnMoveListener moveListener = new ColumnMoveListener(
						keyNamesTableHeader, this);
			} else {
				this.keyNamesTableHeader.setColumnModel(columnModel);
			}
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
		setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(0, 0));

		this.currentTree = new ActivitySubsetTree();
		this.currentTree.setRowHeight(0);
		this.currentTree.setLargeModel(true);
		this.currentTree.setCellRenderer(new ActivityTreeCellRenderer());
		this.currentTree.addMouseListener(new ActivitySubsetListener(this));
		this.currentTree.setDragEnabled(true);

		JScrollPane treePane = new JScrollPane(this.currentTree);

		this.currentTable = new ActivitySubsetTable();

		setModels();

		this.currentTable.addMouseListener(new ActivitySubsetListener(this));
		JScrollPane tablePane = new JScrollPane(this.currentTable);
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

		JMenuBar northMenuBar = new JMenuBar();

		final JPopupMenu selectKeysMenu = new JPopupMenu("select keys");
		JMenuItem selectKeysItem = new JMenuItem("select keys");
		northMenuBar.add(selectKeysItem);
		selectKeysItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				JDialog dialog = new SelectKeysDialog(ActivitySubsetPanel.this);
				Component c = (Component) arg0.getSource();
				int py = c.getY() + c.getHeight() + 2;
				dialog.setLocationRelativeTo(ActivitySubsetPanel.this);
				dialog.setVisible(true);

			}
		});
		northMenuBar.add(selectKeysMenu);
		for (JCheckBoxMenuItem checkBox : keyToCheckBoxMap.values()) {
			selectKeysMenu.add(checkBox);
		}

		northMenuBar.add(new JSeparator(SwingConstants.VERTICAL));
		northMenuBar.add(keyNamesTableHeader);

		this.add(northMenuBar, BorderLayout.NORTH);
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

	public Set<ProcessorFactory> getSelectedObjects() {
		Set<ProcessorFactory> result = new HashSet<ProcessorFactory>();
		int[] selectedRows = this.currentTable.getSelectedRows();
		ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) this.currentTable
				.getModel();
		for (int i = 0; i < selectedRows.length; i++) {
			int row = selectedRows[i];
			ProcessorFactory pf = tableModel.getRowObject(row);
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
