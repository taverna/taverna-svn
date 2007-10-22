/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

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

	public TreeModel createTree() {
		PropertiedTreeModel<StringObject> testImpl = new PropertiedTreeModelImpl<StringObject>();
		PropertiedGraphView<StringObject> graphView = new PropertiedGraphViewImpl<StringObject>();
		PropertiedObjectSet<StringObject> testSet = new PropertiedObjectSetImpl<StringObject>();
		StringObject service1 = new StringObject("blast service");
		StringObject service2 = new StringObject("emma service");
		StringObject service3 = new StringObject("fetchPdb service");
		StringObject service4 = new StringObject("renderPdb service");

		PropertyKey domainKey = new StringKey("domain");
		PropertyKey nameKey = new StringKey("name");
		PropertyKey providerKey = new StringKey("provider");
		PropertyKey typeKey = new StringKey("type");

		PropertyValue ebiValue = new StringValue("ebi");
		PropertyValue manchesterValue = new StringValue("manchester");
		PropertyValue soaplabValue = new StringValue("soaplab");
		PropertyValue wsdlValue = new StringValue("wsdl");
		PropertyValue geneticsValue = new StringValue("genetics");
		PropertyValue structureValue = new StringValue("structure");
		PropertyValue blastValue = new StringValue("blast");
		PropertyValue emmaValue = new StringValue("emma");
		PropertyValue fetchPdbValue = new StringValue("fetchPdb");
		PropertyValue renderPdbValue = new StringValue("renderPdb");

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

		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting>();
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

	public TestJTree() {
		TreeModel model = createTree();
		Container content = getContentPane();
		JTree tree = new JTree(model);
		content.add(new JScrollPane(tree), BorderLayout.CENTER);
		setSize(275, 300);
		setVisible(true);
	}

	public static void main(String[] args) {
		new TestJTree();
	}
}
