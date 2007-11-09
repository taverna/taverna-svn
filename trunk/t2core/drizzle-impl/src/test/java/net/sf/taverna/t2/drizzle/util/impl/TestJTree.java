/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
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

import org.junit.Ignore;

/**
 * @author alanrw
 * 
 */
@Ignore("Not a test case")
public final class TestJTree extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1244291627173869253L;

	PropertiedObjectSet<StringObject> testSet = new PropertiedObjectSetImpl<StringObject>();

	StringObject service1 = new StringObject("blast service"); //$NON-NLS-1$

	StringObject service2 = new StringObject("emma service"); //$NON-NLS-1$

	private StringObject service3 = new StringObject("fetchPdb service"); //$NON-NLS-1$

	private StringObject service4 = new StringObject("renderPdb service"); //$NON-NLS-1$

	PropertyKey domainKey = new StringKey("domain"); //$NON-NLS-1$

	PropertyKey nameKey = new StringKey("name"); //$NON-NLS-1$

	PropertyKey providerKey = new StringKey("provider"); //$NON-NLS-1$

	PropertyKey typeKey = new StringKey("type"); //$NON-NLS-1$

	PropertyValue ebiValue = new StringValue("ebi"); //$NON-NLS-1$

	private PropertyValue manchesterValue = new StringValue("manchester"); //$NON-NLS-1$

	private PropertyValue soaplabValue = new StringValue("soaplab"); //$NON-NLS-1$

	PropertyValue wsdlValue = new StringValue("wsdl"); //$NON-NLS-1$

	PropertyValue geneticsValue = new StringValue("genetics"); //$NON-NLS-1$

	private PropertyValue structureValue = new StringValue("structure"); //$NON-NLS-1$

	PropertyValue blastValue = new StringValue("blast"); //$NON-NLS-1$

	private PropertyValue emmaValue = new StringValue("emma"); //$NON-NLS-1$

	private PropertyValue fetchPdbValue = new StringValue("fetchPdb"); //$NON-NLS-1$

	private PropertyValue renderPdbValue = new StringValue("renderPdb"); //$NON-NLS-1$

	JTree currentTree;

	private PropertiedGraphView<StringObject> graphView = null;

	TreeModel model = null;
	
	List<PropertyKeySetting> fullKeySettings = null;

	private void initializeSet() {
		this.graphView = new PropertiedGraphViewImpl<StringObject>();

		this.testSet.setProperty(this.service1, this.providerKey, this.ebiValue);
		this.testSet.setProperty(this.service1, this.typeKey, this.wsdlValue);
		this.testSet.setProperty(this.service1, this.domainKey, this.geneticsValue);
		this.testSet.setProperty(this.service1, this.nameKey, this.blastValue);

		this.testSet.setProperty(this.service2, this.providerKey, this.ebiValue);
		this.testSet.setProperty(this.service2, this.typeKey, this.soaplabValue);
		this.testSet.setProperty(this.service2, this.domainKey, this.geneticsValue);
		this.testSet.setProperty(this.service2, this.nameKey, this.emmaValue);

		this.testSet.setProperty(this.service3, this.providerKey, this.ebiValue);
		this.testSet.setProperty(this.service3, this.typeKey, this.soaplabValue);
		this.testSet.setProperty(this.service3, this.domainKey, this.structureValue);
		this.testSet.setProperty(this.service3, this.nameKey, this.fetchPdbValue);

		this.testSet.setProperty(this.service4, this.providerKey, this.manchesterValue);
		this.testSet.setProperty(this.service4, this.typeKey, this.wsdlValue);
		this.testSet.setProperty(this.service4, this.domainKey, this.structureValue);
		this.testSet.setProperty(this.service4, this.nameKey, this.renderPdbValue);

		this.graphView.setPropertiedObjectSet(this.testSet);

	}

	private List<PropertyKeySetting> initializeSettings() {
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting>();

		PropertyKeySetting typeSetting = new PropertyKeySettingImpl();
		typeSetting.setPropertyKey(this.typeKey);
		keySettings.add(typeSetting);
		PropertyKeySetting domainSetting = new PropertyKeySettingImpl();
		domainSetting.setPropertyKey(this.domainKey);
		keySettings.add(domainSetting);
		PropertyKeySetting providerSetting = new PropertyKeySettingImpl();
		providerSetting.setPropertyKey(this.providerKey);
		keySettings.add(providerSetting);
		PropertyKeySetting nameSetting = new PropertyKeySettingImpl();
		nameSetting.setPropertyKey(this.nameKey);
		keySettings.add(nameSetting);
		PropertyKeySetting edgeSetting = new PropertyKeySettingImpl();
		keySettings.add(edgeSetting);
		return keySettings;
	}

	List<PropertyKeySetting> getTreeSettings
		(final List<PropertyKeySetting> fullSettings1) {
		List<PropertyKeySetting> result = new ArrayList<PropertyKeySetting>();
		for (PropertyKeySetting pks : fullSettings1) {
			if (pks.getPropertyKey() != null) {
				result.add(pks);
			}
			else {
				break;
			}
		}
		return result;
	}
	
	List<PropertyKeySetting> getTableSettings
	(final List<PropertyKeySetting> fullSettings1) {
		PropertyKeySetting[] fullArray = fullSettings1.toArray(new PropertyKeySetting[0]);
		List<PropertyKeySetting> result = new ArrayList<PropertyKeySetting> ();
		
		int index = 0;
		for (; fullArray[index].getPropertyKey() != null; index++) {
			// Just loop
		}
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
		testImpl.setPropertiedGraphView(this.graphView);

		TreeModel untypedView = TreeModelAdapter.untypedView(testImpl);
		return untypedView;
	}

	void expandAll(final JTree expansionTree) {
		for (int i = 0; i <= expansionTree.getRowCount(); i++) {
			expansionTree.expandRow(i);
		}
	}

	public class TableTreeCellRenderer extends JPanel implements TreeCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8795507848377363512L;

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
					/**
					 * 
					 */
					private static final long serialVersionUID = 6622165324630089260L;
					@Override
					public int getRowCount() {
						return 1;
					}
					@Override
					public int getColumnCount() {
						List<PropertyKeySetting> tableKeySettings =
							getTableSettings(TestJTree.this.fullKeySettings);
						int count = 1;
						if (tableKeySettings.size() > count) {
							count = tableKeySettings.size();
						}
						return count;
					}
					@SuppressWarnings("unchecked")
					@Override
					public Object getValueAt(int valueRow, int column) {
						Object resultValue;
						StringObject so = ((PropertiedTreeObjectNode<StringObject>) value).getObject();
						List<PropertyKeySetting> tableKeySettings =
							getTableSettings(TestJTree.this.fullKeySettings);
						if (column == tableKeySettings.size()) {
							resultValue = so.toString();
						}
						else {
						PropertyKey key = tableKeySettings.get(column).getPropertyKey();
						String valueString = "missing"; //$NON-NLS-1$
						PropertyValue pv = TestJTree.this.testSet.getPropertyValue(so, key);
						if (pv != null) {
							valueString = pv.toString();
						}
						resultValue = valueString;
						}
						return resultValue;
					}
				});
				this.table.doLayout();
				result = this;
			}
			else {
			result = this.defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			}
			return result;
		}
		
	}
	/**
	 * Construct a TestJTree that takes a PropertiedObjectSet populated with
	 * example data and shows it as a JTree. The effects of altering the
	 * PropertiedObjectSet are shown to be mirrored in the JTree.
	 */
	public TestJTree() {
		initializeSet();
		this.fullKeySettings = initializeSettings();
		
		this.model = createTree(getTreeSettings(this.fullKeySettings));
		Container content = getContentPane();
		this.currentTree = new JTree(this.model);
		this.currentTree.setCellRenderer (new TableTreeCellRenderer());
		content.setLayout(new BorderLayout());
		content.add(this.currentTree, BorderLayout.CENTER);
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
				PropertyKeySetting movedKey = TestJTree.this.fullKeySettings.get(fromIndex);
				TestJTree.this.fullKeySettings.remove(fromIndex);
				TestJTree.this.fullKeySettings.add(toIndex, movedKey);
				TestJTree.this.model = createTree(getTreeSettings(TestJTree.this.fullKeySettings));
				TestJTree.this.currentTree.setModel(TestJTree.this.model);
				expandAll(TestJTree.this.currentTree);

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
			@Override
			public void run() {
				TestJTree.this.testSet.removeObject(TestJTree.this.service1);
				expandAll(TestJTree.this.currentTree);
			}
		}, 10000);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				TestJTree.this.testSet.setProperty(TestJTree.this.service2, TestJTree.this.typeKey, TestJTree.this.wsdlValue);
				expandAll(TestJTree.this.currentTree);
			}
		}, 20000);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				TestJTree.this.testSet.addObject(TestJTree.this.service1);
				expandAll(TestJTree.this.currentTree);
			}
		}, 30000);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				TestJTree.this.testSet.setProperty(TestJTree.this.service1, TestJTree.this.providerKey, TestJTree.this.ebiValue);
				TestJTree.this.testSet.setProperty(TestJTree.this.service1, TestJTree.this.typeKey, TestJTree.this.wsdlValue);
				TestJTree.this.testSet.setProperty(TestJTree.this.service1, TestJTree.this.domainKey, TestJTree.this.geneticsValue);
				TestJTree.this.testSet.setProperty(TestJTree.this.service1, TestJTree.this.nameKey, TestJTree.this.blastValue);
				expandAll(TestJTree.this.currentTree);
			}
		}, 40000);
		 
	}

	/**
	 * Show the test JTree.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new TestJTree();
	}
}
