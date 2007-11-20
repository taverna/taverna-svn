/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedObject;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;
import net.sf.taverna.t2.drizzle.util.StringKey;
import net.sf.taverna.t2.drizzle.util.StringValue;
import net.sf.taverna.t2.utility.TreeModelAdapter;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.junit.Ignore;

/**
 * @author alanrw
 * 
 */
public final class ActivityTabPanel extends JPanel {

	JTree currentTree;

	private PropertiedGraphView<ProcessorFactory> graphView = null;

	List<PropertyKeySetting> fullKeySettings = null;

	private List<PropertyKeySetting> initializeKeySettings(final Set<PropertyKey> propertyKeyProfile) {
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting>();

		for (PropertyKey key : propertyKeyProfile) {
			PropertyKeySetting setting = ObjectFactory.getInstance(PropertyKeySetting.class);
			setting.setPropertyKey(key);
			keySettings.add(setting);
		}
		PropertyKeySetting separatorKey = ObjectFactory.getInstance(PropertyKeySetting.class);
		keySettings.add(separatorKey);

		return keySettings;
	}

	void expandAll(final JTree expansionTree) {
		for (int i = 0; i <= expansionTree.getRowCount(); i++) {
			expansionTree.expandRow(i);
		}
	}
	
	TreeModel createTreeModel (final ActivityRegistrySubsetModel subsetModel,
			final List<PropertyKeySetting> keySettings) {
		TreeModel result = null;
		
		PropertiedTreeModel<ProcessorFactory> propertiedTreeModel = ObjectFactory.getInstance(PropertiedTreeModel.class);
		propertiedTreeModel.setPropertyKeySettings(fullKeySettings);
		propertiedTreeModel.setFilter(subsetModel.getFilter());
		
		PropertiedGraphView<ProcessorFactory> graphView = ObjectFactory.getInstance(PropertiedGraphView.class);
		graphView.setPropertiedObjectSet(subsetModel.getParentRegistry().getRegistry());
		propertiedTreeModel.setPropertiedGraphView(this.graphView);

		result = TreeModelAdapter.untypedView(propertiedTreeModel);
		return result;
	}

	/**
	 * Construct a ActivityTabPanel that takes a PropertiedObjectSet populated with
	 * example data and shows it as a JTree. The effects of altering the
	 * PropertiedObjectSet are shown to be mirrored in the JTree.
	 */
	public ActivityTabPanel(final ActivityRegistrySubsetModel subsetModel) {
		this.graphView = ObjectFactory.getInstance(PropertiedGraphView.class);
		this.graphView.setPropertiedObjectSet(subsetModel.getParentRegistry().getRegistry());
		this.fullKeySettings = initializeKeySettings (subsetModel.getPropertyKeyProfile());

		this.currentTree = new JTree(createTreeModel(subsetModel, fullKeySettings));
		this.currentTree.setCellRenderer(new TableTreeCellRenderer());
		this.currentTree.setRowHeight(0);
		this.currentTree.setRootVisible(false);
		this.setLayout(new BorderLayout());
		this.add(this.currentTree, BorderLayout.CENTER);
		setSize(275, 300);
		expandAll(this.currentTree);
		setVisible(true);
		Vector<String> keyNames = new Vector<String>();
		int edgeIndex = 0;
		for (PropertyKeySetting pks : this.fullKeySettings) {
			PropertyKey pk = pks.getPropertyKey();
			if (pk != null) {
				keyNames.add(pk.toString());
			} else {
				edgeIndex = keyNames.size();
				keyNames.add(""); //$NON-NLS-1$
			}
		}
		
		DefaultTableModel tableModel = new DefaultTableModel(keyNames, 0);
		JTable table = new JTable(tableModel);
		TableColumnModel columnModel = table.getColumnModel();
		TableColumn edgeColumn = columnModel.getColumn(edgeIndex);
		DefaultTableCellRenderer blackRenderer = new DefaultTableCellRenderer();
		blackRenderer.setBackground(Color.BLACK);
		edgeColumn.setHeaderRenderer(blackRenderer);
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
				expandAll(ActivityTabPanel.this.currentTree);
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
}
