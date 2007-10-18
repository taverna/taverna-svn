/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class PropertiedTreeModelImplTest {
	
	private PropertiedTreeModel<ExampleObject> testImpl;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#PropertiedTreeModelImpl()}.
	 */
	@Test
	public final void testPropertiedTreeModelImpl() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#addTreeModelListener(net.sf.taverna.t2.utility.TypedTreeModelListener)}.
	 */
	@Test
	public final void testAddTreeModelListener() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#getChild(net.sf.taverna.t2.drizzle.util.PropertiedTreeNode, int)}.
	 */
	@Test
	public final void testGetChild() {
		try {
			testImpl.getChild(null, 0);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertiedTreeRootNode<ExampleObject> parent =
			new PropertiedTreeRootNodeImpl<ExampleObject>();
		PropertiedTreeObjectNode<ExampleObject> child1 =
			new PropertiedTreeObjectNodeImpl<ExampleObject>();
		parent.addChild(child1);
		PropertiedTreeObjectNode<ExampleObject> child2 =
			new PropertiedTreeObjectNodeImpl<ExampleObject>();
		parent.addChild(child2);

		assertEquals(child1, testImpl.getChild(parent, 0));
		assertEquals(child2, testImpl.getChild(parent, 1));
		assertNull(testImpl.getChild(parent, -1));
		assertNull(testImpl.getChild(parent, 7));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#getChildCount(net.sf.taverna.t2.drizzle.util.PropertiedTreeNode)}.
	 */
	@Test
	public final void testGetChildCount() {
		try {
			testImpl.getChildCount(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertiedTreeRootNode<ExampleObject> parent =
			new PropertiedTreeRootNodeImpl<ExampleObject>();
		assertEquals(0, testImpl.getChildCount(parent));
		
		PropertiedTreeObjectNode<ExampleObject> child1 =
			new PropertiedTreeObjectNodeImpl<ExampleObject>();
		parent.addChild(child1);
		assertEquals(1, testImpl.getChildCount(parent));
		
		PropertiedTreeObjectNode<ExampleObject> child2 =
			new PropertiedTreeObjectNodeImpl<ExampleObject>();
		parent.addChild(child2);
		assertEquals(2, testImpl.getChildCount(parent));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#getIndexOfChild(net.sf.taverna.t2.drizzle.util.PropertiedTreeNode, net.sf.taverna.t2.drizzle.util.PropertiedTreeNode)}.
	 */
	@Test
	public final void testGetIndexOfChild() {
		try {
			testImpl.getIndexOfChild(null, null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertiedTreeRootNode<ExampleObject> parent =
			new PropertiedTreeRootNodeImpl<ExampleObject>();
		
		try {
			testImpl.getIndexOfChild(parent, null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		
		PropertiedTreeObjectNode<ExampleObject> child1 =
			new PropertiedTreeObjectNodeImpl<ExampleObject>();
		PropertiedTreeObjectNode<ExampleObject> child2 =
			new PropertiedTreeObjectNodeImpl<ExampleObject>();
		try {
			testImpl.getIndexOfChild(null, child1);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
			
		}
		parent.addChild(child1);
		assertEquals(0, testImpl.getIndexOfChild(parent, child1));
		assertEquals(-1, testImpl.getIndexOfChild(parent, child2));
		
		parent.addChild(child2);
		assertEquals(1, testImpl.getIndexOfChild(parent, child2));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#getPropertyKeySettings()}.
	 */
	@Test
	public final void testGetPropertyKeySettings() {
		List<PropertyKeySetting> settings = testImpl.getPropertyKeySettings();
		assertNull(settings);
		
		List<PropertyKeySetting> newSettings = new ArrayList<PropertyKeySetting>();
		PropertyKeySetting setting1 = new PropertyKeySettingImpl();
		newSettings.add(setting1);
		PropertyKeySetting setting2 = new PropertyKeySettingImpl();
		newSettings.add(setting2);
		
		testImpl.setPropertyKeySettings(newSettings);
		settings = testImpl.getPropertyKeySettings();
		assertEquals(newSettings, settings);
		
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#getRoot()}.
	 */
	@Test
	public final void testGetRoot() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#isLeaf(net.sf.taverna.t2.drizzle.util.PropertiedTreeNode)}.
	 */
	@Test
	public final void testIsLeaf() {
		try {
			testImpl.isLeaf(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertiedTreeRootNode<ExampleObject> parent =
			new PropertiedTreeRootNodeImpl<ExampleObject>();
		PropertiedTreeObjectNode<ExampleObject> child =
			new PropertiedTreeObjectNodeImpl<ExampleObject>();
		parent.addChild(child);
		
		assertFalse(testImpl.isLeaf(parent));
		assertTrue(testImpl.isLeaf(child));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#removeTreeModelListener(net.sf.taverna.t2.utility.TypedTreeModelListener)}.
	 */
	@Test
	public final void testRemoveTreeModelListener() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#setFilter(net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter)}.
	 */
	@Test
	public final void testSetFilter() {
		try {
			testImpl.setFilter(null);
			// This is OK
		}
		catch (NullPointerException e) {
			fail ("NullPointerException should not be thrown");
		}
		PropertiedObjectFilter<ExampleObject> filter =
			new PropertiedObjectFilter<ExampleObject> () {

				public boolean acceptObject(ExampleObject object) {
					return true;
				}
			
		};
		testImpl.setFilter(filter);
		assertEquals(filter, testImpl.getFilter());
		
		try {
			testImpl.setFilter(filter);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is OK
		}
		// TODO check the filtering happens
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#setObjectComparator(java.util.Comparator)}.
	 */
	@Test
	public final void testSetObjectComparator() {
		try {
			testImpl.setObjectComparator(null);
			fail("NullPointerException should have been thrown");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		Comparator comparator = Collator.getInstance();

		testImpl.setObjectComparator(comparator);
		assertEquals(comparator, testImpl.getObjectComparator());
		
		try {
			testImpl.setObjectComparator(comparator);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is OK
		}
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#setPropertiedGraphView(net.sf.taverna.t2.drizzle.util.PropertiedGraphView)}.
	 */
	@Test
	public final void testSetPropertiedGraphView() {
		PropertiedObjectSet<ExampleObject> testSet =
			new PropertiedObjectSetImpl<ExampleObject> ();
		ExampleObject service1 = new ExampleObject();
		ExampleObject service2 = new ExampleObject();
		ExampleObject service3 = new ExampleObject();
		ExampleObject service4 = new ExampleObject();
		
		PropertyKey domainKey = new ExampleKey();
		PropertyKey nameKey = new ExampleKey();
		PropertyKey providerKey = new ExampleKey();
		PropertyKey typeKey = new ExampleKey();
		
		PropertyValue ebiValue = new ExampleValue();
		PropertyValue manchesterValue = new ExampleValue();
		PropertyValue soaplabValue = new ExampleValue();
		PropertyValue wsdlValue = new ExampleValue();
		PropertyValue geneticsValue = new ExampleValue();
		PropertyValue structureValue = new ExampleValue();
		PropertyValue blastValue = new ExampleValue();
		PropertyValue emmaValue = new ExampleValue();
		PropertyValue fetchPdbValue = new ExampleValue();
		PropertyValue renderPdbValue = new ExampleValue();
		
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
		
		PropertiedGraphView<ExampleObject> graphView =
			new PropertiedGraphViewImpl<ExampleObject>();
		graphView.setPropertiedObjectSet(testSet);

		// Try an empty ordering
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting> ();
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		
		PropertiedTreeNode<ExampleObject> root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(4, root.getChildCount());
		PropertiedTreeNode child0 = root.getChild(0);
		PropertiedTreeNode child1 = root.getChild(1);
		PropertiedTreeNode child2 = root.getChild(2);
		PropertiedTreeNode child3 = root.getChild(3);
		assertTrue (child0 instanceof PropertiedTreeObjectNode);
		assertTrue (child1 instanceof PropertiedTreeObjectNode);
		assertTrue (child2 instanceof PropertiedTreeObjectNode);
		assertTrue (child3 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)child0).getObject());
		assertEquals (service2, ((PropertiedTreeObjectNode)child1).getObject());
		assertEquals (service3, ((PropertiedTreeObjectNode)child2).getObject());
		assertEquals (service4, ((PropertiedTreeObjectNode)child3).getObject());
		
		// Reset testImpl
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		PropertyKeySetting typeSetting = new PropertyKeySettingImpl();
		typeSetting.setPropertyKey(typeKey);
		keySettings.add (typeSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		
		root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(2, root.getChildCount());
		child0 = root.getChild(0);
		child1 = root.getChild(1);
		assertTrue (child0 instanceof PropertiedTreePropertyValueNode);
		assertTrue (child1 instanceof PropertiedTreePropertyValueNode);
		assertEquals(2, child0.getChildCount());
		assertEquals(2, child1.getChildCount());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child0).getKey());
		assertEquals(soaplabValue, ((PropertiedTreePropertyValueNode)child0).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child1).getKey());
		assertEquals(wsdlValue, ((PropertiedTreePropertyValueNode)child1).getValue());
		PropertiedTreeNode grandchild0 = child0.getChild(0);
		PropertiedTreeNode grandchild1 = child0.getChild(1);
		PropertiedTreeNode grandchild2 = child1.getChild(0);
		PropertiedTreeNode grandchild3 = child1.getChild(1);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)grandchild2).getObject());
		assertTrue (grandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild3).getObject());
		
		// Reset testImpl
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		keySettings.add (typeSetting);
		PropertyKeySetting providerSetting = new PropertyKeySettingImpl();
		providerSetting.setPropertyKey(providerKey);
		keySettings.add (providerSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		
		root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(2, root.getChildCount());
		child0 = root.getChild(0);
		child1 = root.getChild(1);
		assertTrue (child0 instanceof PropertiedTreePropertyValueNode);
		assertTrue (child1 instanceof PropertiedTreePropertyValueNode);
		assertEquals(1, child0.getChildCount());
		assertEquals(2, child1.getChildCount());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child0).getKey());
		assertEquals(soaplabValue, ((PropertiedTreePropertyValueNode)child0).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child1).getKey());
		assertEquals(wsdlValue, ((PropertiedTreePropertyValueNode)child1).getValue());
		
		grandchild0 = child0.getChild(0);
		grandchild1 = child1.getChild(0);
		grandchild2 = child1.getChild(1);
		assertTrue (grandchild0 instanceof PropertiedTreePropertyValueNode);
		assertTrue (grandchild1 instanceof PropertiedTreePropertyValueNode);
		assertTrue (grandchild2 instanceof PropertiedTreePropertyValueNode);
		assertEquals(2, grandchild0.getChildCount());
		assertEquals(1, grandchild1.getChildCount());
		assertEquals(1, grandchild2.getChildCount());
		assertEquals(providerKey, ((PropertiedTreePropertyValueNode)grandchild0).getKey());
		assertEquals(ebiValue, ((PropertiedTreePropertyValueNode)grandchild0).getValue());
		assertEquals(providerKey, ((PropertiedTreePropertyValueNode)grandchild1).getKey());
		assertEquals(ebiValue, ((PropertiedTreePropertyValueNode)grandchild1).getValue());
		assertEquals(providerKey, ((PropertiedTreePropertyValueNode)grandchild2).getKey());
		assertEquals(manchesterValue, ((PropertiedTreePropertyValueNode)grandchild2).getValue());
		
		PropertiedTreeNode greatgrandchild0 = grandchild0.getChild(0);
		PropertiedTreeNode greatgrandchild1 = grandchild0.getChild(1);
		PropertiedTreeNode greatgrandchild2 = grandchild1.getChild(0);
		PropertiedTreeNode greatgrandchild3 = grandchild2.getChild(0);
		assertTrue (greatgrandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)greatgrandchild0).getObject());
		assertTrue (greatgrandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)greatgrandchild1).getObject());
		assertTrue (greatgrandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)greatgrandchild2).getObject());
		assertTrue (greatgrandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)greatgrandchild3).getObject());
		
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#setPropertyKeySettings(java.util.List)}.
	 */
	@Test
	public final void testSetPropertyKeySettings() {
		try {
			testImpl.setPropertyKeySettings(null);
			fail("NullPointerException should have been thrown");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		
		List<PropertyKeySetting> newSettings = new ArrayList<PropertyKeySetting>();
		PropertyKeySetting setting1 = new PropertyKeySettingImpl();
		newSettings.add(setting1);
		PropertyKeySetting setting2 = new PropertyKeySettingImpl();
		newSettings.add(setting2);
		
		testImpl.setPropertyKeySettings(newSettings);
		List<PropertyKeySetting> settings = testImpl.getPropertyKeySettings();
		assertEquals(newSettings, settings);
		
		try {
			testImpl.setPropertyKeySettings(newSettings);
			fail("IllegalStateException should have been thrown");
		}
		catch (IllegalStateException e) {
			// This is OK
		}
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)}.
	 */
	@Test
	public final void testValueForPathChanged() {
		fail("Not yet implemented"); // TODO
	}

}
