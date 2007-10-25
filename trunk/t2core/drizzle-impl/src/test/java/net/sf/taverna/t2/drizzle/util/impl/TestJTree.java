/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import java.util.Timer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedObject;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;
import net.sf.taverna.t2.utility.TreeModelAdapter;

/**
 * @author alanrw
 * 
 */
public final class TestJTree extends JFrame {

	private PropertiedObjectSet<StringObject> testSet = new PropertiedObjectSetImpl<StringObject>();

	private StringObject service1 = new StringObject("blast service");

	private StringObject service2 = new StringObject("emma service");

	private StringObject service3 = new StringObject("fetchPdb service");

	private StringObject service4 = new StringObject("renderPdb service");

	private PropertyKey domainKey = new StringKey("domain");

	private PropertyKey nameKey = new StringKey("name");

	private PropertyKey providerKey = new StringKey("provider");

	private PropertyKey typeKey = new StringKey("type");

	private PropertyValue ebiValue = new StringValue("ebi");

	private PropertyValue manchesterValue = new StringValue("manchester");

	private PropertyValue soaplabValue = new StringValue("soaplab");

	private PropertyValue wsdlValue = new StringValue("wsdl");

	private PropertyValue geneticsValue = new StringValue("genetics");

	private PropertyValue structureValue = new StringValue("structure");

	private PropertyValue blastValue = new StringValue("blast");

	private PropertyValue emmaValue = new StringValue("emma");

	private PropertyValue fetchPdbValue = new StringValue("fetchPdb");

	private PropertyValue renderPdbValue = new StringValue("renderPdb");

	private JTree tree;

	private PropertiedGraphView<StringObject> graphView = null;

	private TreeModel model = null;
	
	private List<PropertyKeySetting> fullKeySettings = null;

	private void initializeSet() {
		graphView = new PropertiedGraphViewImpl<StringObject>();

		testSet.setProperty(service1, providerKey, ebiValue);
		testSet.setProperty(service1, typeKey, wsdlValue);
		testSet.setProperty(service1, domainKey, geneticsValue);
		testSet.setProperty(service1, nameKey, blastValue);

		testSet.setProperty(service2, providerKey, ebiValue);
		testSet.setProperty(service2, typeKey, soaplabValue);
		testSet.setProperty(service2, domainKey, geneticsValue);
		testSet.setProperty(service2, nameKey, emmaValue);

		testSet.setProperty(service3, providerKey, ebiValue);
		testSet.setProperty(service3, typeKey, soaplabValue);
		testSet.setProperty(service3, domainKey, structureValue);
		testSet.setProperty(service3, nameKey, fetchPdbValue);

		testSet.setProperty(service4, providerKey, manchesterValue);
		testSet.setProperty(service4, typeKey, wsdlValue);
		testSet.setProperty(service4, domainKey, structureValue);
		testSet.setProperty(service4, nameKey, renderPdbValue);

		graphView.setPropertiedObjectSet(testSet);

	}

	private List<PropertyKeySetting> initializeSettings() {
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting>();

		PropertyKeySetting typeSetting = new PropertyKeySettingImpl();
		typeSetting.setPropertyKey(typeKey);
		keySettings.add(typeSetting);
		PropertyKeySetting domainSetting = new PropertyKeySettingImpl();
		domainSetting.setPropertyKey(domainKey);
		keySettings.add(domainSetting);
		PropertyKeySetting providerSetting = new PropertyKeySettingImpl();
		providerSetting.setPropertyKey(providerKey);
		keySettings.add(providerSetting);
		PropertyKeySetting nameSetting = new PropertyKeySettingImpl();
		nameSetting.setPropertyKey(nameKey);
		keySettings.add(nameSetting);
		PropertyKeySetting edgeSetting = new PropertyKeySettingImpl();
		keySettings.add(edgeSetting);
		return keySettings;
	}

	private List<PropertyKeySetting> getTreeSettings
		(final List<PropertyKeySetting> fullKeySettings) {
		List<PropertyKeySetting> result = new ArrayList<PropertyKeySetting>();
		for (PropertyKeySetting pks : fullKeySettings) {
			if (pks.getPropertyKey() != null) {
				result.add(pks);
			}
			else {
				break;
			}
		}
		return result;
	}
	
	private List<PropertyKeySetting> getTableSettings
	(final List<PropertyKeySetting> fullKeySettings) {
		PropertyKeySetting[] fullArray = fullKeySettings.toArray(new PropertyKeySetting[0]);
		List<PropertyKeySetting> result = new ArrayList<PropertyKeySetting> ();
		
		int index = 0;
		for (; fullArray[index].getPropertyKey() != null; index++);
		for (++index; index < fullArray.length; index++) {
			result.add(fullArray[index]);
		}
		return result;
	}
	/**
	 * Populate the PropertiedObjectSet and create a TreeModel over it
	 * 
	 * @return
	 */
	public TreeModel createTree(final List<PropertyKeySetting> keySettings) {
		PropertiedTreeModel<StringObject> testImpl = new PropertiedTreeModelImpl<StringObject>();
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);

		TreeModel untypedView = TreeModelAdapter.untypedView(testImpl);
		return untypedView;
	}

	private void expandAll(JTree tree) {
		for (int i = 0; i <= tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	public class TableTreeCellRenderer extends JPanel implements TreeCellRenderer {

		private JTable table;
		
		private TreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
		
		TableTreeCellRenderer() {
			this.setLayout(new BorderLayout());
			this.table = new JTable();
			this.add(this.table, BorderLayout.CENTER);
//			this.add(new JLabel("Hiya"), BorderLayout.NORTH);
			setSize(275, 300);
		}
		
		public Component getTreeCellRendererComponent(JTree tree, final Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component result = null;
			if (value instanceof PropertiedTreeObjectNode) {
				this.table.setModel(new DefaultTableModel() {
					public int getRowCount() {
						return 1;
					}
					public int getColumnCount() {
						List<PropertyKeySetting> tableKeySettings =
							getTableSettings(fullKeySettings);
						int result = 1;
						if (tableKeySettings.size() > result) {
							result = tableKeySettings.size();
						}
						return result;
					}
					public Object getValueAt(int row, int column) {
						Object result;
						StringObject so = ((PropertiedTreeObjectNode<StringObject>) value).getObject();
						List<PropertyKeySetting> tableKeySettings =
							getTableSettings(fullKeySettings);
						if (column == tableKeySettings.size()) {
							result = so.toString();
						}
						else {
						PropertyKey key = tableKeySettings.get(column).getPropertyKey();
						String keyString = key.toString();
						String valueString = "missing";
						PropertiedObject<StringObject> po = testSet.getPropertiedObject(so);
						if (po != null) {
							PropertyValue pv = po.getPropertyValue(key);
							if (pv != null) {
								valueString = pv.toString();
							}
						}
							result = valueString;
						}
						return result;
					}
				});
				this.table.doLayout();
				result = this;
			}
			else {
			result = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			}
			return result;
		}
		
	}
	/**
	 * Construct a TestJTree that takes a PropertiedObjectSet populated with
	 * example data and shows it as a JTree. The effects of altering the
	 * PropertiedObjectSet are shown to be mirrored in the JTree.
	 * 
	 * @throws InterruptedException
	 */
	public TestJTree() throws InterruptedException {
		initializeSet();
		fullKeySettings = initializeSettings();
		
		model = createTree(getTreeSettings(fullKeySettings));
		Container content = getContentPane();
		tree = new JTree(model);
		tree.setCellRenderer (new TableTreeCellRenderer());
		content.setLayout(new BorderLayout());
		content.add(tree, BorderLayout.CENTER);
		setSize(275, 300);
		expandAll(tree);
		setVisible(true);
		Vector<String> keyNames = new Vector<String>();
		int edgeIndex = 0;
		for (PropertyKeySetting pks : fullKeySettings) {
			PropertyKey pk = pks.getPropertyKey();
			if (pk != null) {
				keyNames.add(pk.toString());
			} else {
				edgeIndex = keyNames.size();
				keyNames.add("");
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
				PropertyKeySetting movedKey = fullKeySettings.get(fromIndex);
				fullKeySettings.remove(fromIndex);
				fullKeySettings.add(toIndex, movedKey);
				model = createTree(getTreeSettings(fullKeySettings));
				tree.setModel(model);
				expandAll(tree);

			}

			public void columnRemoved(TableColumnModelEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnSelectionChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		content.add(table.getTableHeader(), BorderLayout.NORTH);

		validate();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				testSet.removeObject(service1);
				expandAll(tree);
			}
		}, 10000);
		timer.schedule(new TimerTask() {

			public void run() {
				testSet.setProperty(service2, typeKey, wsdlValue);
				expandAll(tree);
			}
		}, 20000);
		timer.schedule(new TimerTask() {

			public void run() {
				testSet.addObject(service1);
				expandAll(tree);
			}
		}, 30000);
		timer.schedule(new TimerTask() {

			public void run() {
				testSet.setProperty(service1, providerKey, ebiValue);
				testSet.setProperty(service1, typeKey, wsdlValue);
				testSet.setProperty(service1, domainKey, geneticsValue);
				testSet.setProperty(service1, nameKey, blastValue);
				expandAll(tree);
			}
		}, 40000);
		 
	}

	/**
	 * Show the test JTree.
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		new TestJTree();
	}
}
