/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeModel;

import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.utility.TreeModelAdapter;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTreeRenderer;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 * 
 */
public final class ActivityTabPanel extends JPanel {

	JTree currentTree;

	List<PropertyKeySetting> fullKeySettings = null;
	
	private ScuflModel currentWorkflow = null;

	private List<PropertyKeySetting> initializeKeySettings(final Set<PropertyKey> propertyKeyProfile) {
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting>();

		for (PropertyKey key : propertyKeyProfile) {
			PropertyKeySetting setting = ObjectFactory.getInstance(PropertyKeySetting.class);
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
	
	TreeModel createTreeModel (final ActivityRegistrySubsetModel subsetModel,
			final List<PropertyKeySetting> keySettings) {
		TreeModel result = null;
		
		PropertiedTreeModel<ProcessorFactory> propertiedTreeModel = ObjectFactory.getInstance(PropertiedTreeModel.class);
		propertiedTreeModel.setPropertyKeySettings(fullKeySettings);
		propertiedTreeModel.setFilter(subsetModel.getFilter());
		
		propertiedTreeModel.setPropertiedGraphView(subsetModel.getParentRegistry().getGraphView());

		result = TreeModelAdapter.untypedView(propertiedTreeModel);
		return result;
	}

	/**
	 * Construct a ActivityTabPanel that takes a PropertiedObjectSet populated with
	 * example data and shows it as a JTree. The effects of altering the
	 * PropertiedObjectSet are shown to be mirrored in the JTree.
	 */
	public ActivityTabPanel(final ActivityRegistrySubsetModel subsetModel) {
		this.setName(subsetModel.getName());
		setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(0,0));
		this.fullKeySettings = initializeKeySettings (subsetModel.getPropertyKeyProfile());

		this.currentTree = new JTree(createTreeModel(subsetModel, fullKeySettings));
		this.currentTree.setRowHeight(0);
		this.currentTree.setLargeModel(true);
		this.currentTree.setCellRenderer(new ActivityTreeCellRenderer());
		this.currentTree.addMouseListener(new ActivityTreeListener(this));
//		this.currentTree.addTreeSelectionListener(new ActivityTreeListener());
		JScrollPane treePane = new JScrollPane(this.currentTree);
		treePane.setPreferredSize(new Dimension(0,0));
		this.add(treePane, BorderLayout.CENTER);

		expandAll();
		
		Vector<String> keyNames = new Vector<String>();
		for (PropertyKeySetting pks : this.fullKeySettings) {
			PropertyKey pk = pks.getPropertyKey();
			if (pk == null) {
				throw new IllegalStateException("key cannot be null");
			}
			keyNames.add(pk.toString());
		}
		
		DefaultTableModel tableModel = new DefaultTableModel(keyNames, 0);
		JTable table = new JTable(tableModel);
		TableColumnModel columnModel = table.getColumnModel();
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
				PropertyKeySetting movedKey = ActivityTabPanel.this.fullKeySettings.get(fromIndex);
				ActivityTabPanel.this.fullKeySettings.remove(fromIndex);
				ActivityTabPanel.this.fullKeySettings.add(toIndex, movedKey);
			ActivityTabPanel.this.currentTree.setModel(ActivityTabPanel.this.createTreeModel(subsetModel, ActivityTabPanel.this.fullKeySettings));
				expandAll();
				}
			}

			public void columnRemoved(TableColumnModelEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnSelectionChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		this.add(table.getTableHeader(), BorderLayout.NORTH);
		
		validate();
	}

	void collapseAll() {
		for (int i = this.currentTree.getRowCount() -1; i > 0; i--) {
			this.currentTree.collapseRow(i);
		}
	}

	/**
	 * @return the currentWorkflow
	 */
	public synchronized final ScuflModel getCurrentWorkflow() {
		return currentWorkflow;
	}

	/**
	 * @param currentWorkflow the currentWorkflow to set
	 */
	public synchronized final void setCurrentWorkflow(ScuflModel currentWorkflow) {
		this.currentWorkflow = currentWorkflow;
	}

	/**
	 * @return the currentTree
	 */
	public synchronized final JTree getCurrentTree() {
		return currentTree;
	}
}
