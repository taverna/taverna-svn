/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeModel;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetModel;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.utility.TreeModelAdapter;

/**
 * @author alanrw
 *
 */
public final class ActivitySubsetKeyTableHeader {
	
	private static HashMap<String, TableColumnModel> columnModelMap = new HashMap<String, TableColumnModel>();
	private static HashMap<String, List<PropertyKeySetting>> keySettingMap = new HashMap<String, List<PropertyKeySetting>>();
	
	public static JTableHeader getTableHeader(final ActivityRegistrySubsetModel subsetModel, final JTree currentTree, final JTable currentTable) {
		final String kind = subsetModel.getIdent().getKind();
		TableColumnModel tableColumnModel = null;
		 List<PropertyKeySetting> fullKeySettings = null;
		if (columnModelMap.containsKey(kind)) {
			tableColumnModel = columnModelMap.get(kind);
			fullKeySettings = keySettingMap.get(kind);
		} else {
			Set<PropertyKey> keyProfile = subsetModel.getIdent().getPropertyKeyProfile();
			Vector<String> keyNames = new Vector<String>();
			fullKeySettings = new ArrayList<PropertyKeySetting>();
			
			for (PropertyKey pk : keyProfile) {
				keyNames.add(pk.toString());
				PropertyKeySetting setting = ObjectFactory
				.getInstance(PropertyKeySetting.class);
		setting.setPropertyKey(pk);
		fullKeySettings.add(setting);			}
			DefaultTableModel keyNamesTableModel = new DefaultTableModel(keyNames, 1);
			JTable keyNamesTable = new JTable(keyNamesTableModel);
			tableColumnModel = keyNamesTable.getColumnModel();
			columnModelMap.put(kind, tableColumnModel);
			keySettingMap.put(kind, fullKeySettings);
		}
		
		tableColumnModel.addColumnModelListener(new TableColumnModelListener() {

			public void columnAdded(TableColumnModelEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnMarginChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnMoved(TableColumnModelEvent arg0) {
				 List<PropertyKeySetting> keySettings = keySettingMap.get(kind);
				
				int fromIndex = arg0.getFromIndex();
				int toIndex = arg0.getToIndex();
				if (fromIndex != toIndex) {
					PropertyKeySetting movedKey = keySettings
							.get(fromIndex);
					keySettings.remove(fromIndex);
					keySettings.add(toIndex,
							movedKey);
					TreeModel newTreeModel = createTreeModel(subsetModel,
							keySettings);
					currentTree.setModel(newTreeModel);
//					collapseAll(); // As requested by users
					ActivitySubsetTableModel newActivitiesTableModel = new ActivitySubsetTableModel(
							((PropertiedTreeRootNode<ProcessorFactoryAdapter>) newTreeModel
									.getRoot()));
					currentTable
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
		JTableHeader result = new JTableHeader(tableColumnModel);
		result.setToolTipText("Move the cells to re-order the tree and table"); //$NON-NLS-1$
		ToolTipManager.sharedInstance().registerComponent(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static TreeModel createTreeModel(final ActivityRegistrySubsetModel subsetModel,
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
				.getParentRegistry().getGraphView());
		
		propertiedTreeModel.detachFromGraphView();
		result = TreeModelAdapter.untypedView(propertiedTreeModel);
		return result;
	}
}
