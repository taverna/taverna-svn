/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.PropertiedObjectBean;
import net.sf.taverna.t2.drizzle.bean.PropertiedObjectSetBean;
import net.sf.taverna.t2.drizzle.util.PropertiedObject;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
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
public class PropertiedObjectSetImplTest {
	
	private PropertiedObjectSetImpl<ExampleObject> testImpl;

	private int addedPropertyCount;

	private int changedPropertyCount;

	private int removedPropertyCount;
	
	private int addedCount;
	
	private int removedCount;
	
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
		addedPropertyCount = 0;
		changedPropertyCount = 0;
		removedPropertyCount = 0;
		testImpl = new PropertiedObjectSetImpl<ExampleObject> ();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	private PropertiedObjectListener createObjectListener () {
		return new PropertiedObjectListener ()
			 {	
					public void propertyAdded (Object o, PropertyKey key, PropertyValue value) {
						addedPropertyCount++;
					}
					public void propertyRemoved (Object o, PropertyKey key, PropertyValue value) {
						removedPropertyCount++;
					}
					public void propertyChanged (Object o, PropertyKey key, PropertyValue oldValue, PropertyValue newValue) {
						changedPropertyCount++;
					}
		};
	}
	
	private PropertiedObjectSetListener createObjectSetListener () {
		return new PropertiedObjectSetListener ()
			 {	
					public void objectAdded (PropertiedObjectSet pos, Object o) {
						addedCount++;
					}
					public void objectRemoved (PropertiedObjectSet pos, Object o) {
						removedCount++;
					}
		};
	}
	
	private ExampleObject createObject() {
		return new ExampleObject();
	}
	
	private PropertyKey createKey() {
		return new ExampleKey();
	}

	private PropertyValue createValue() {
		return new ExampleValue();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#PropertiedObjectSetImpl()}.
	 */
	@Test
	public void testPropertiedObjectSetImpl() {
		assertFalse ("testImpl.getObjects()", testImpl.getObjects() == null);
		assertEquals("testImpl.getObjects().size()", 0, testImpl.getObjects().size());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#addAllObjectsListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testAddAllObjectsListener() {
		try {
			testImpl.addAllObjectsListener(null);
			fail("NullPointerException should have been thrown for listener");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		testImpl.addAllObjectsListener (createObjectListener());
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		testImpl.setProperty(testObject1, testKey1, testValue1);
		testImpl.setProperty(testObject2, testKey1, testValue1);
		testImpl.setProperty(testObject1, testKey1, testValue2);
		testImpl.removeProperty(testObject1, testKey1);
		assertEquals("addedPropertyCount", 2, addedPropertyCount);
		assertEquals("changedPropertyCount", 1, changedPropertyCount);
		assertEquals("removedPropertyCount", 1, removedPropertyCount);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#addListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener)}.
	 */
	@Test
	public void testAddListener() {
		try {
			testImpl.addListener (null);
			fail ("NullPointerException should have been thrown for listener");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		testImpl.addListener(createObjectSetListener());
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		testImpl.addObject (testObject1);
		testImpl.addObject (testObject2);
		testImpl.removeObject (testObject1);
		assertEquals ("addedCount", 2, addedCount);
		assertEquals ("removedCount", 1, removedCount);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#addObject(java.lang.Object)}.
	 */
	@Test
	public void testAddObject() {
		try {
			testImpl.addObject (null);
			fail ("NullPointerException should have been thrown for object");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		testImpl.addObject (testObject);
		assertTrue ("testImpl.containsObject (testObject)",
				testImpl.containsObject (testObject));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#containsObject(java.lang.Object)}.
	 */
	@Test
	public void testContainsObject() {
		try {
			testImpl.containsObject (null);
			fail ("NullPointerException should have been thrown for object");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		assertFalse ("testImpl.containsObject (testObject1)",
				testImpl.containsObject (testObject));
		testImpl.addObject (testObject);
		assertTrue ("testImpl.containsObject (testObject1)",
				testImpl.containsObject (testObject));
		testImpl.removeObject (testObject);
		assertFalse ("testImpl.containsObject (testObject1)",
				testImpl.containsObject (testObject));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#containsPropertiedObject(net.sf.taverna.t2.drizzle.util.PropertiedObject)}.
	 */
	@Test
	public void testContainsPropertiedObject() {
		try {
			testImpl.containsPropertiedObject (null);
			fail ("NullPointerException should have been thrown for propertied object");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		PropertiedObject testPropertiedObject = testImpl.addObject (testObject);
		testImpl.addObject (testObject);
		assertTrue ("testImpl.containsPropertiedObject (testPropertiedObject)",
				testImpl.containsPropertiedObject (testPropertiedObject));
		testImpl.removeObject (testObject);
		assertFalse ("testImpl.containsPropertiedObject (testPropertiedObject)",
				testImpl.containsPropertiedObject (testPropertiedObject));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getAllPropertyKeys()}.
	 */
	@Test
	public void testGetAllPropertyKeys() {
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		PropertyKey testKey1 = createKey();
		PropertyKey testKey2 = createKey();
		PropertyKey testKey3 = createKey();
		PropertyValue testValue = createValue();
		testImpl.setProperty(testObject1, testKey1, testValue);
		testImpl.setProperty(testObject1, testKey2, testValue);
		testImpl.setProperty(testObject2, testKey3, testValue);
		
		Set<PropertyKey> keys = testImpl.getAllPropertyKeys();
		assertEquals ("keys.size()", 3, keys.size());
		assertTrue("keys.contains(testKey1)", keys.contains(testKey1));
		assertTrue("keys.contains(testKey2)", keys.contains(testKey2));
		assertTrue("keys.contains(testKey3)", keys.contains(testKey3));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getAllPropertyValues(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testGetAllPropertyValues() {
		try {
			testImpl.getAllPropertyValues(null);
			fail("NullPointerException should have been thrown");
		} catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject1 = createObject ();
		ExampleObject testObject2 = createObject ();
		ExampleObject testObject3 = createObject ();
		testImpl.addObject(testObject1);
		testImpl.addObject(testObject2);
		testImpl.addObject(testObject3);
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		PropertyValue testValue3 = createValue();
		testImpl.setProperty (testObject1, testKey, testValue1);
		testImpl.setProperty (testObject2, testKey, testValue2);
		testImpl.setProperty (testObject3, testKey, testValue3);
		
		Set<PropertyValue> values = testImpl.getAllPropertyValues(testKey);
		assertEquals ("values.size()", 3, values.size());
		assertTrue("values.contains(testValue1)", values.contains(testValue1));
		assertTrue("values.contains(testValue2)", values.contains(testValue2));
		assertTrue("values.contains(testValue3)", values.contains(testValue3));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getObjects()}.
	 */
	@Test
	public void testGetObjects() {
		ExampleObject testObject1 = createObject ();
		ExampleObject testObject2 = createObject ();
		ExampleObject testObject3 = createObject ();
		testImpl.addObject(testObject1);
		testImpl.addObject(testObject2);
		testImpl.addObject(testObject3);
		Set<ExampleObject> objects = testImpl.getObjects();
		assertEquals ("objects.size()", 3, objects.size());
		assertTrue("objects.contains(testObject1)", objects.contains(testObject1));
		assertTrue("objects.contains(testObject2)", objects.contains(testObject2));
		assertTrue("objects.contains(testObject3)", objects.contains(testObject3));
		
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getPropertiedObject(java.lang.Object)}.
	 */
	@Test
	public void testGetPropertiedObject() {
		try {
			testImpl.getPropertiedObject(null);
			fail ("NullPointerException should have been thrown");
		}
		catch (NullPointerException e) {
			// this is expected
		}
		ExampleObject testObject = createObject();
		PropertiedObject testPropertiedObject = testImpl.addObject(testObject);
		assertEquals("testImpl.getPropertiedObject(testObject)", testPropertiedObject,
				testImpl.getPropertiedObject(testObject));
		testImpl.removeObject(testObject);
		assertNull("testImpl.getPropertiedObject(testObject)",
				testImpl.getPropertiedObject(testObject));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getPropertiedObjects()}.
	 */
	@Test
	public void testGetPropertiedObjects() {
		ExampleObject testObject1 = createObject ();
		ExampleObject testObject2 = createObject ();
		ExampleObject testObject3 = createObject ();
		PropertiedObject testPropertiedObject1 = testImpl.addObject(testObject1);
		PropertiedObject testPropertiedObject2 = testImpl.addObject(testObject2);
		PropertiedObject testPropertiedObject3 = testImpl.addObject(testObject3);
		Set<PropertiedObject> propertiedObjects = testImpl.getPropertiedObjects();
		assertEquals ("propertiedObjects.size()", 3, propertiedObjects.size());
		assertTrue("propertiedObjects.contains(testPropertiedObject1)",
				propertiedObjects.contains(testPropertiedObject1));
		assertTrue("propertiedObjects.contains(testPropertiedObject2)",
				propertiedObjects.contains(testPropertiedObject2));
		assertTrue("propertiedObjects.contains(testPropertiedObject3)",
				propertiedObjects.contains(testPropertiedObject3));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeAllObjectsListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testRemoveAllObjectsListener() {
		try {
			testImpl.removeAllObjectsListener(null);
			fail("NullPointerException should have been thrown for listener");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectListener objectListener = createObjectListener();
		testImpl.addAllObjectsListener (objectListener);
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		testImpl.setProperty(testObject1, testKey1, testValue1);
		testImpl.removeAllObjectsListener(objectListener);
		testImpl.setProperty(testObject2, testKey1, testValue1);
		testImpl.setProperty(testObject1, testKey1, testValue2);
		testImpl.removeProperty(testObject1, testKey1);
		assertEquals("addedPropertyCount", 1, addedPropertyCount);
		assertEquals("changedPropertyCount", 0, changedPropertyCount);
		assertEquals("removedPropertyCount", 0, removedPropertyCount);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener)}.
	 */
	@Test
	public void testRemoveListener() {
		try {
			testImpl.removeListener (null);
			fail ("NullPointerException should have been thrown for listener");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectSetListener listener = createObjectSetListener();
		testImpl.addListener(listener);
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		testImpl.addObject (testObject1);
		testImpl.removeListener(listener);
		testImpl.addObject (testObject2);
		testImpl.removeObject (testObject1);
		assertEquals ("addedCount", 1, addedCount);
		assertEquals ("removedCount", 0, removedCount);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeObject(java.lang.Object)}.
	 */
	@Test
	public void testRemoveObject() {
		try {
			testImpl.removeObject (null);
			fail ("NullPointerException should have been thrown for object");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		PropertiedObject testPropertiedObject = testImpl.addObject (testObject);
		assertTrue ("testImpl.containsObject (testObject)",
				testImpl.containsObject (testObject));
		assertTrue ("testImpl.containsPropertiedObject  (testPropertiedObject)",
				testImpl.containsPropertiedObject (testPropertiedObject));
		testImpl.removeObject (testObject);
		assertFalse ("testImpl.containsObject (testObject)",
				testImpl.containsObject (testObject));
		assertFalse ("testImpl.containsPropertiedObject  (testPropertiedObject)",
				testImpl.containsPropertiedObject (testPropertiedObject));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeProperty(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testRemoveProperty() {
		try {
			testImpl.removeProperty(null, null);
			fail ("NullPointerException should have been thrown for object or key");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		PropertyKey testKey1 = createKey();
		PropertyValue testValue = createValue();
		PropertyKey testKey2 = createKey();
		try {
			testImpl.removeProperty(testObject, null);
			fail ("NullPointerException should have been thrown for key");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			testImpl.removeProperty(null, testKey1);
			fail ("NullPointerException should have been thrown for object");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObject testPropertiedObject = testImpl.addObject(testObject);
		testImpl.setProperty (testObject, testKey1, testValue);
		testImpl.setProperty (testObject, testKey2, testValue);
		testImpl.removeProperty (testObject, testKey1);
		assertFalse("testPropertiedObject.hasProperty(testKey1)",
				testPropertiedObject.hasProperty(testKey1));
		assertTrue("testPropertiedObject.hasProperty(testKey2)",
				testPropertiedObject.hasProperty(testKey2));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#setProperty(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public void testSetProperty() {
		try {
			testImpl.setProperty(null, null, null);
			fail ("NullPointerException should have been thrown for object, key or value");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		PropertyKey testKey1 = createKey();
		PropertyValue testValue = createValue();
		PropertyKey testKey2 = createKey();
		try {
			testImpl.setProperty(null, testKey1, testValue);
			fail ("NullPointerException should have been thrown for object");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			testImpl.setProperty(testObject, null, testValue);
			fail ("NullPointerException should have been thrown for key");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			testImpl.setProperty(testObject, testKey1, null);
			fail ("NullPointerException should have been thrown for value");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObject testPropertiedObject = testImpl.addObject(testObject);
		testImpl.setProperty (testObject, testKey1, testValue);
		testImpl.setProperty (testObject, testKey2, testValue);
		assertTrue("testPropertiedObject.hasProperty(testKey1)",
				testPropertiedObject.hasProperty(testKey1));
		assertTrue("testPropertiedObject.hasProperty(testKey2)",
				testPropertiedObject.hasProperty(testKey2));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getAsBean()}.
	 */
	@Test
	public void testGetAsBean() {
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		PropertyKey testKey1 = createKey();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		testImpl.setProperty (testObject1, testKey1, testValue1);
		testImpl.setProperty (testObject2, testKey1, testValue1);
		testImpl.setProperty (testObject2, testKey2, testValue2);
		
		PropertiedObjectSetBean<ExampleObject> testBean = testImpl.getAsBean();
		HashMap<ExampleObject, PropertiedObjectBean> beanedMap =
			testBean.getPropertiedObjectMap();
		assertEquals("beanedMap.size()", beanedMap.size(),2);
		assertTrue("beanedMap.containsKey(testObject1)", beanedMap.containsKey(testObject1));
		assertTrue("beanedMap.containsKey(testObject2)", beanedMap.containsKey(testObject2));
		
		PropertiedObjectBean beanedObject1 = beanedMap.get(testObject1);
		HashMap<PropertyKey, PropertyValue> beanedProperties1 = beanedObject1.getProperties();
		assertEquals("beanedProperties1.size()", 1, beanedProperties1.size());
		assertTrue("beanedProperties1.containsKey(testKey1)",
				beanedProperties1.containsKey(testKey1));
		assertEquals("beanedProperties1.get(testKey1)",
				testValue1, beanedProperties1.get(testKey1));

		PropertiedObjectBean beanedObject2 = beanedMap.get(testObject2);
		HashMap<PropertyKey, PropertyValue> beanedProperties2 = beanedObject2.getProperties();
		assertEquals("beanedProperties2.size()", 2, beanedProperties2.size());
		assertTrue("beanedProperties2.containsKey(testKey1)",
				beanedProperties2.containsKey(testKey1));
		assertEquals("beanedProperties2.get(testKey1)",
				testValue1, beanedProperties2.get(testKey1));
		assertTrue("beanedProperties2.containsKey(testKey2)",
				beanedProperties2.containsKey(testKey2));
		assertEquals("beanedProperties2.get(testKey2)",
				testValue2, beanedProperties2.get(testKey2));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#setFromBean(net.sf.taverna.t2.drizzle.bean.PropertiedObjectSetBean)}.
	 */
	@Test
	public void testSetFromBean() {
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		PropertyKey testKey1 = createKey();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		testImpl.setProperty (testObject1, testKey1, testValue1);
		testImpl.setProperty (testObject2, testKey1, testValue1);
		testImpl.setProperty (testObject2, testKey2, testValue2);
		
		PropertiedObjectSetBean<ExampleObject> testBean = testImpl.getAsBean();
		PropertiedObjectSetImpl<ExampleObject> backFromBean =
			new PropertiedObjectSetImpl<ExampleObject>();
		backFromBean.setFromBean (testBean);
		
		assertEquals("backFromBean.getObjects.size()",
				2, backFromBean.getObjects().size());
		assertTrue("backFromBean.containsObject(testObject1)",
				backFromBean.containsObject(testObject1));
		assertTrue("backFromBean.containsObject(testObject2)",
				backFromBean.containsObject(testObject2));
		PropertiedObject<ExampleObject> po1 =
			backFromBean.getPropertiedObject(testObject1);
		assertEquals("po1.getPropertyKeys().size()",
				1, po1.getPropertyKeys().size());
		assertEquals("po1.getPropertyValue(testKey1)",
				testValue1, po1.getPropertyValue(testKey1));
		
		PropertiedObject<ExampleObject> po2 =
			backFromBean.getPropertiedObject(testObject2);
		assertEquals("po2.getPropertyKeys().size()",
				2, po2.getPropertyKeys().size());
		assertEquals("po2.getPropertyValue(testKey1)",
				testValue1, po2.getPropertyValue(testKey1));
		assertEquals("po2.getPropertyValue(testKey2)",
				testValue2, po2.getPropertyValue(testKey2));
	}

}
