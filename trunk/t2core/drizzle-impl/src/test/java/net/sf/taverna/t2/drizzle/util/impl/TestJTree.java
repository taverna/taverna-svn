/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import java.util.Timer;

import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;
import net.sf.taverna.t2.utility.TreeModelAdapter;

/**
 * @author alanrw
 * 
 */
public final class TestJTree extends JFrame {
	private PropertiedTreeModel<StringObject> testImpl = new PropertiedTreeModelImpl<StringObject>();

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

	List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting>();
	
	/**
	 * Populate the PropertiedObjectSet and create a TreeModel over it
	 * 
	 * @return
	 */
	public TreeModel createTree() {
		PropertiedGraphView<StringObject> graphView = new PropertiedGraphViewImpl<StringObject>();

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

		PropertyKeySetting typeSetting = new PropertyKeySettingImpl();
		typeSetting.setPropertyKey(typeKey);
		keySettings.add(typeSetting);
		PropertyKeySetting domainSetting = new PropertyKeySettingImpl();
		domainSetting.setPropertyKey(domainKey);
		keySettings.add(domainSetting);
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

	/**
	 * Construct a TestJTree that takes a PropertiedObjectSet populated with
	 * example data and shows it as a JTree. The effects of altering the
	 * PropertiedObjectSet are shown to be mirrored in the JTree.
	 * 
	 * @throws InterruptedException
	 */
	public TestJTree() throws InterruptedException {
		TreeModel model = createTree();
		Container content = getContentPane();
		tree = new JTree(model);
		content.setLayout(new BorderLayout());
		content.add(tree, BorderLayout.CENTER);
		setSize(275, 300);
		expandAll(tree);
		setVisible(true);
		Vector<String> keyNames = new Vector<String>();
		for (PropertyKeySetting pks : keySettings) {
			keyNames.add (pks.getPropertyKey().toString());
		}
		DefaultTableModel tableModel = new DefaultTableModel (keyNames, 
				2);
		JTable table = new JTable(tableModel);
		content.add(table.getTableHeader(), BorderLayout.NORTH);
		validate();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				testSet.removeObject(service1);
				expandAll(tree);
			}

		}, 10000);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				testSet.setProperty(service2, typeKey, wsdlValue);
				expandAll(tree);
			}

		}, 20000);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				testSet.addObject(service1);
				expandAll(tree);
			}

		}, 30000);
		timer.schedule(new TimerTask() {

			@Override
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
