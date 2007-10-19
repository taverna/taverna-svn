/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.tree.TreePath;

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
import net.sf.taverna.t2.utility.TypedTreeModelEvent;
import net.sf.taverna.t2.utility.TypedTreeModelListener;

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
	
	TypedTreeModelEvent<PropertiedTreeNode<ExampleObject>>	changeEvent = null;;

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
		assertNull(testImpl.getPropertyKeySettings());
		assertNull(testImpl.getFilter());
		assertNull(testImpl.getObjectComparator());
	}

	private TypedTreeModelListener<PropertiedTreeNode<ExampleObject>> createListener () {
		return new TypedTreeModelListener<PropertiedTreeNode<ExampleObject>> () {

			public void treeNodesChanged(TypedTreeModelEvent<PropertiedTreeNode<ExampleObject>> e) {
				// Nothing
			}

			public void treeNodesInserted(TypedTreeModelEvent<PropertiedTreeNode<ExampleObject>> e) {
				// Nothing
			}

			public void treeNodesRemoved(TypedTreeModelEvent<PropertiedTreeNode<ExampleObject>> e) {
				// Nothing
			}

			public void treeStructureChanged(TypedTreeModelEvent<PropertiedTreeNode<ExampleObject>> e) {
				changeEvent = e;
			}
					
		};
	}
	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#addTreeModelListener(net.sf.taverna.t2.utility.TypedTreeModelListener)}.
	 */
	@Test
	public final void testAddTreeModelListener() {
		try {
			testImpl.addTreeModelListener(null);
			fail ("NullPointerException should have been thrown");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		
		PropertiedObjectSet<ExampleObject> testSet =
			new PropertiedObjectSetImpl<ExampleObject> ();
		ExampleObject service1 = new ExampleObject();
		ExampleObject service2 = new ExampleObject();
		
		PropertyKey domainKey = new ExampleKey();
		
		PropertyValue geneticsValue = new ExampleValue();
		
		testSet.setProperty(service1, domainKey, geneticsValue);
		PropertiedGraphView<ExampleObject> graphView = new PropertiedGraphViewImpl<ExampleObject> ();
		graphView.setPropertiedObjectSet(testSet);

		// Try an empty ordering
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting> ();
		testImpl.setPropertyKeySettings(keySettings);
		
		testImpl.addTreeModelListener (createListener());
		testImpl.setPropertiedGraphView(graphView);
		assertTrue (changeEvent != null);
		assertEquals(testImpl, changeEvent.getSource());
		PropertiedTreeNode<ExampleObject> root = testImpl.getRoot();
		assertEquals(1, root.getChildCount());
		assertEquals(new TreePath(new PropertiedTreeNode[] {root}),
				changeEvent.getTreePath());

		changeEvent = null;
		testSet.addObject(service1);
		assertNull(changeEvent);
		
		changeEvent = null;
		testSet.addObject(service2);
		assertEquals(2, root.getChildCount());
		assertEquals(new TreePath(new PropertiedTreeNode[] {root}),
				changeEvent.getTreePath());
		
		changeEvent = null;
		testSet.removeObject(service1);
		assert(changeEvent != null);
		assertEquals(1, root.getChildCount());
		assertEquals(new TreePath(new PropertiedTreeNode[] {root}),
				changeEvent.getTreePath());
		
		changeEvent = null;
		testSet.setProperty(service2, domainKey, geneticsValue);
		assert(changeEvent != null);
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
		// I'm not sure what there is to test
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
		try {
			testImpl.removeTreeModelListener(null);
			fail ("NullPointerException should have been thrown");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		
		PropertiedObjectSet<ExampleObject> testSet =
			new PropertiedObjectSetImpl<ExampleObject> ();
		ExampleObject service1 = new ExampleObject();
		PropertyKey domainKey = new ExampleKey();
		
		PropertyValue geneticsValue = new ExampleValue();
		
		testSet.setProperty(service1, domainKey, geneticsValue);
		PropertiedGraphView<ExampleObject> graphView = new PropertiedGraphViewImpl<ExampleObject> ();
		graphView.setPropertiedObjectSet(testSet);

		// Try an empty ordering
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting> ();
		testImpl.setPropertyKeySettings(keySettings);
		
		TypedTreeModelListener<PropertiedTreeNode<ExampleObject>> listener = createListener();
		testImpl.addTreeModelListener (listener);
		testImpl.setPropertiedGraphView(graphView);
		assertTrue (changeEvent != null);
		assertEquals(testImpl, changeEvent.getSource());
		PropertiedTreeNode<ExampleObject> root = testImpl.getRoot();
		assertEquals(1, root.getChildCount());
		assertEquals(new TreePath(new PropertiedTreeNode[] {root}),
				changeEvent.getTreePath());
		
		changeEvent = null;
		testImpl.removeTreeModelListener(listener);
		testSet.removeObject(service1);
		assertNull (changeEvent);
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
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeModelImpl#setObjectComparator(java.util.Comparator)}.
	 */
	@SuppressWarnings("unchecked")
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
		try {
			testImpl.setPropertiedGraphView(null);
			fail("NullPointerException should have been thrown");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertiedGraphView<ExampleObject> graphView =
			new PropertiedGraphViewImpl<ExampleObject>();
		
		try {
			testImpl.setPropertiedGraphView(graphView);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is expected
		}
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
		
		// Try a reverse comparator
		
		Comparator<ExampleObject> reverseComparator = new Comparator<ExampleObject> () {

			public int compare(ExampleObject arg0, ExampleObject arg1) {
				return - arg0.compareTo(arg1);
			}
		};
		
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setObjectComparator(reverseComparator);
		testImpl.setPropertiedGraphView(graphView);
		root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(4, root.getChildCount());
		child0 = root.getChild(0);
		child1 = root.getChild(1);
		child2 = root.getChild(2);
		child3 = root.getChild(3);
		assertTrue (child0 instanceof PropertiedTreeObjectNode);
		assertTrue (child1 instanceof PropertiedTreeObjectNode);
		assertTrue (child2 instanceof PropertiedTreeObjectNode);
		assertTrue (child3 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)child0).getObject());
		assertEquals (service3, ((PropertiedTreeObjectNode)child1).getObject());
		assertEquals (service2, ((PropertiedTreeObjectNode)child2).getObject());
		assertEquals (service1, ((PropertiedTreeObjectNode)child3).getObject());
		
		// Try a filter
		PropertiedObjectFilter<ExampleObject> evenFilter =
			new PropertiedObjectFilter<ExampleObject> () {
			
			public boolean acceptObject(ExampleObject object) {
				return ((object.getI() % 2) == 0);
			}
			
		};
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setFilter(evenFilter);
		testImpl.setPropertiedGraphView(graphView);
		root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(2, root.getChildCount());
		child0 = root.getChild(0);
		child1 = root.getChild(1);
		assertTrue (child0 instanceof PropertiedTreeObjectNode);
		assertTrue (child1 instanceof PropertiedTreeObjectNode);
		
		// Try one level of ordering
		testImpl.detachFromGraphView();
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
		
		// Try two levels of ordering
		testImpl.detachFromGraphView();
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
		
		// Try one level with a missing value
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		keySettings.add (typeSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testSet.removeProperty(service1, typeKey);
		testImpl.setPropertiedGraphView(graphView);
		
		root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(3, root.getChildCount());
		child0 = root.getChild(0);
		child1 = root.getChild(1);
		child2 = root.getChild(2);
		assertTrue (child0 instanceof PropertiedTreePropertyValueNode);
		assertTrue (child1 instanceof PropertiedTreePropertyValueNode);
		assertTrue (child2 instanceof PropertiedTreePropertyValueNode);
		assertEquals(2, child0.getChildCount());
		assertEquals(1, child1.getChildCount());
		assertEquals(1, child2.getChildCount());
		
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child0).getKey());
		assertEquals(soaplabValue, ((PropertiedTreePropertyValueNode)child0).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child1).getKey());
		assertEquals(wsdlValue, ((PropertiedTreePropertyValueNode)child1).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child2).getKey());
		assertNull(((PropertiedTreePropertyValueNode)child2).getValue());
		grandchild0 = child0.getChild(0);
		grandchild1 = child0.getChild(1);
		grandchild2 = child1.getChild(0);
		grandchild3 = child2.getChild(0);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild2).getObject());
		assertTrue (grandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)grandchild3).getObject());
		
		// Try removing a property that's in the tree
		testSet.setProperty(service1, typeKey, wsdlValue);
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		keySettings.add (typeSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		testSet.removeProperty(service1, typeKey);
		root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(3, root.getChildCount());
		child0 = root.getChild(0);
		child1 = root.getChild(1);
		child2 = root.getChild(2);
		assertTrue (child0 instanceof PropertiedTreePropertyValueNode);
		assertTrue (child1 instanceof PropertiedTreePropertyValueNode);
		assertTrue (child2 instanceof PropertiedTreePropertyValueNode);
		assertEquals(2, child0.getChildCount());
		assertEquals(1, child1.getChildCount());
		assertEquals(1, child2.getChildCount());
		
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child0).getKey());
		assertEquals(soaplabValue, ((PropertiedTreePropertyValueNode)child0).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child1).getKey());
		assertEquals(wsdlValue, ((PropertiedTreePropertyValueNode)child1).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child2).getKey());
		assertNull(((PropertiedTreePropertyValueNode)child2).getValue());
		grandchild0 = child0.getChild(0);
		grandchild1 = child0.getChild(1);
		grandchild2 = child1.getChild(0);
		grandchild3 = child2.getChild(0);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild2).getObject());
		assertTrue (grandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)grandchild3).getObject());
		
		// Try removing a property that isn't in the tree
		testSet.setProperty(service1, typeKey, wsdlValue);
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		keySettings.add (typeSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		testSet.removeProperty(service1, providerKey);
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
		grandchild0 = child0.getChild(0);
		grandchild1 = child0.getChild(1);
		grandchild2 = child1.getChild(0);
		grandchild3 = child1.getChild(1);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)grandchild2).getObject());
		assertTrue (grandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild3).getObject());
		
		// Try adding a property that is in the tree
		testSet.setProperty(service1, providerKey, ebiValue);
		testSet.removeProperty(service1, typeKey);
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		keySettings.add (typeSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		testSet.setProperty(service1, typeKey, wsdlValue);
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
		grandchild0 = child0.getChild(0);
		grandchild1 = child0.getChild(1);
		grandchild2 = child1.getChild(0);
		grandchild3 = child1.getChild(1);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)grandchild2).getObject());
		assertTrue (grandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild3).getObject());
		
		// Try adding a property that isn't in the tree
		testSet.removeProperty(service1, providerKey);
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		keySettings.add (typeSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		testSet.setProperty(service1, providerKey, ebiValue);
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
		grandchild0 = child0.getChild(0);
		grandchild1 = child0.getChild(1);
		grandchild2 = child1.getChild(0);
		grandchild3 = child1.getChild(1);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)grandchild2).getObject());
		assertTrue (grandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild3).getObject());
		
		// Check changing of a property value
		testSet.setProperty(service1, typeKey, soaplabValue);
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		keySettings.add (typeSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		testSet.setProperty(service1, typeKey, wsdlValue);
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
		grandchild0 = child0.getChild(0);
		grandchild1 = child0.getChild(1);
		grandchild2 = child1.getChild(0);
		grandchild3 = child1.getChild(1);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)grandchild2).getObject());
		assertTrue (grandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild3).getObject());
		
		// Try removing an object
		testImpl.detachFromGraphView();
		testImpl = new PropertiedTreeModelImpl<ExampleObject> ();
		keySettings = new ArrayList<PropertyKeySetting> ();
		keySettings.add (typeSetting);
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(graphView);
		testSet.removeObject(service1);
		root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(2, root.getChildCount());
		child0 = root.getChild(0);
		child1 = root.getChild(1);
		assertEquals(2, graphView.getValues(typeKey).size());
		assertTrue (child0 instanceof PropertiedTreePropertyValueNode);
		assertTrue (child1 instanceof PropertiedTreePropertyValueNode);
		assertEquals(2, child0.getChildCount());
		assertEquals(1, child1.getChildCount());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child0).getKey());
		assertEquals(soaplabValue, ((PropertiedTreePropertyValueNode)child0).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child1).getKey());
		assertEquals(wsdlValue, ((PropertiedTreePropertyValueNode)child1).getValue());
		grandchild0 = child0.getChild(0);
		grandchild1 = child0.getChild(1);
		grandchild2 = child1.getChild(0);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild2).getObject());
		
		// Try adding an object
		testSet.addObject(service1);
		root = testImpl.getRoot();
		assertTrue (root instanceof PropertiedTreeRootNode);
		assertEquals(3, root.getChildCount());
		child0 = root.getChild(0);
		child1 = root.getChild(1);
		child2 = root.getChild(2);
		assertEquals(2, graphView.getValues(typeKey).size());
		assertTrue (child0 instanceof PropertiedTreePropertyValueNode);
		assertTrue (child1 instanceof PropertiedTreePropertyValueNode);
		assertEquals(2, child0.getChildCount());
		assertEquals(1, child1.getChildCount());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child0).getKey());
		assertEquals(soaplabValue, ((PropertiedTreePropertyValueNode)child0).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child1).getKey());
		assertEquals(wsdlValue, ((PropertiedTreePropertyValueNode)child1).getValue());
		assertEquals(typeKey, ((PropertiedTreePropertyValueNode)child1).getKey());
		assertNull(((PropertiedTreePropertyValueNode)child2).getValue());
		grandchild0 = child0.getChild(0);
		grandchild1 = child0.getChild(1);
		grandchild2 = child1.getChild(0);
		grandchild3 = child2.getChild(0);
		assertTrue (grandchild0 instanceof PropertiedTreeObjectNode);
		assertEquals (service2, ((PropertiedTreeObjectNode)grandchild0).getObject());
		assertTrue (grandchild1 instanceof PropertiedTreeObjectNode);
		assertEquals (service3, ((PropertiedTreeObjectNode)grandchild1).getObject());
		assertTrue (grandchild2 instanceof PropertiedTreeObjectNode);
		assertEquals (service4, ((PropertiedTreeObjectNode)grandchild2).getObject());
		assertTrue (grandchild3 instanceof PropertiedTreeObjectNode);
		assertEquals (service1, ((PropertiedTreeObjectNode)grandchild3).getObject());
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
		// Nothing to check
	}

}
