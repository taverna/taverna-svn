/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.PropertiedObjectBean;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectListener;
import net.sf.taverna.t2.drizzle.util.PropertyValue;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author alanrw
 * 
 */
public final class PropertiedObjectImplTest {

	private PropertiedObjectImpl<ExampleObject> testImpl;

	private ExampleObject testObject;

	private int addedCount;

	private int changedCount;

	private int removedCount;

	private PropertiedObjectListener createListener() {
		return new PropertiedObjectListener() {
			public void propertyAdded(Object o, PropertyKey key,
					PropertyValue value) {
				addedCount++;
			}

			public void propertyRemoved(Object o, PropertyKey key,
					PropertyValue value) {
				removedCount++;
			}

			public void propertyChanged(Object o, PropertyKey key,
					PropertyValue oldValue, PropertyValue newValue) {
				changedCount++;
			}
		};
	}

	private PropertyKey createKey() {
		return new ExampleKey();
	}

	private PropertyValue createValue() {
		return new ExampleValue();
	}

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
		addedCount = 0;
		changedCount = 0;
		removedCount = 0;
		testImpl = new PropertiedObjectImpl<ExampleObject>();
		testObject = new ExampleObject();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#PropertiedObjectImpl()}.
	 */
	@Test
	public void testPropertiedObjectImpl() {
		assertNull(testImpl.getObject());
		assertEquals("testImpl.getPropertyKeys().size()", 0,
				testImpl.getPropertyKeys().size());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#addListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testAddListener() {
		testImpl.setObject(testObject);

		try {
			testImpl.addListener(null);
			fail("NullPointerException should have been thrown");
		} catch (NullPointerException e) {
			// This is expected
		}
		testImpl.addListener(createListener());
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();

		testImpl.setProperty(testKey, testValue1);
		assertEquals("addedCount", 1, addedCount);
		assertEquals("changedCount", 0, changedCount);
		assertEquals("removedCount", 0, removedCount);

		testImpl.setProperty(testKey, testValue2);
		assertEquals("addedCount", 1, addedCount);
		assertEquals("changedCount", 1, changedCount);
		assertEquals("removedCount", 0, removedCount);

		testImpl.removeProperty(testKey);
		assertEquals("addedCount", 1, addedCount);
		assertEquals("changedCount", 1, changedCount);
		assertEquals("removedCount", 1, removedCount);
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#hasProperty(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testHasProperty() {
		testImpl.setObject(testObject);
		try {
			testImpl.hasProperty(null);
			fail("NullPointerException should have been thrown");
		} catch (NullPointerException e) {
			// This is expected
		}
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		assertFalse("testImpl.hasProperty(testKey)",
				testImpl.hasProperty(testKey));
		testImpl.setProperty(testKey, testValue1);
		assertTrue("testImpl.hasProperty(testKey)",
				testImpl.hasProperty(testKey));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#getObject()}.
	 */
	@Test
	public void testGetObject() {
		assertNull("testImpl.getObject()", testImpl.getObject());
		testImpl.setObject(testObject);
		assertEquals("testImpl.getObject()", testObject, testImpl.getObject());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#getPropertyKeys()}.
	 */
	@Test
	public void testGetPropertyKeys() {
		testImpl.setObject(testObject);
		assertEquals("testImpl.getPropertyKeys().size()", 0,
				testImpl.getPropertyKeys().size());

		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		testImpl.setProperty(testKey1, testValue1);
		Set<PropertyKey> keys = testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 1, keys.size());
		assertTrue("testImpl.getPropertyKeys().contains(testKey1)",
				keys.contains(testKey1));

		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();
		testImpl.setProperty(testKey2, testValue2);
		keys = testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 2, keys.size());
		assertTrue("testImpl.getPropertyKeys().contains(testKey1)",
				keys.contains(testKey1));
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)",
				keys.contains(testKey2));

		testImpl.removeProperty(testKey1);
		keys = testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 1, keys.size());
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)",
				keys.contains(testKey2));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#getPropertyValue(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testGetPropertyValue() {
		testImpl.setObject(testObject);
		try {
			testImpl.getPropertyValue(null);
			fail("NullPointerException should have been thrown");
		} catch (NullPointerException e) {
			// This is expected
		}
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();

		testImpl.setProperty(testKey, testValue1);
		assertEquals("testImpl.getPropertyValue(testKey)", testValue1,
				testImpl.getPropertyValue(testKey));
		testImpl.setProperty(testKey, testValue2);
		assertEquals("testImpl.getPropertyValue(testKey)", testValue2,
				testImpl.getPropertyValue(testKey));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#removeListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testRemoveListener() {
		testImpl.setObject(testObject);

		try {
			testImpl.removeListener(null);
			fail("NullPointerException should have been thrown");
		} catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectListener testListener = createListener();
		testImpl.addListener(testListener);
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();

		testImpl.setProperty(testKey, testValue1);
		assertEquals("addedCount", 1, addedCount);
		assertEquals("changedCount", 0, changedCount);
		assertEquals("removedCount", 0, removedCount);
		testImpl.removeListener(testListener);
		testImpl.setProperty(testKey, testValue2);
		testImpl.removeProperty(testKey);
		assertEquals("addedCount", 1, addedCount);
		assertEquals("changedCount", 0, changedCount);
		assertEquals("removedCount", 0, removedCount);
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#removeProperty(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testRemoveProperty() {
		testImpl.setObject(testObject);
		PropertiedObjectListener testListener = createListener();
		testImpl.addListener(testListener);
		try {
			testImpl.removeListener(null);
			fail("NullPointerException should have been thrown");
		} catch (NullPointerException e) {
			// This is expected
		}

		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		testImpl.setProperty(testKey1, testValue1);
		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();
		testImpl.setProperty(testKey2, testValue2);
		Set<PropertyKey> keys = testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 2, keys.size());
		assertTrue("testImpl.getPropertyKeys().contains(testKey1)",
				keys.contains(testKey1));
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)",
				keys.contains(testKey2));

		testImpl.removeProperty(testKey1);
		keys = testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 1, keys.size());
		assertFalse("testImpl.getPropertyKeys().contains(testKey1)", keys.contains(testKey1));
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)", keys.contains(testKey2));

		// Check removing twice does nothing
		testImpl.removeProperty(testKey1);
		keys = testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 1, keys.size());
		assertFalse("testImpl.getPropertyKeys().contains(testKey1)",
				keys.contains(testKey1));
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)",
				keys.contains(testKey2));

		testImpl.removeProperty(testKey2);
		keys = testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 0, keys.size());
		assertFalse("testImpl.getPropertyKeys().contains(testKey1)",
				keys.contains(testKey1));
		assertFalse("testImpl.getPropertyKeys().contains(testKey2)",
				keys.contains(testKey2));

		assertEquals("addedCount", 2, addedCount);
		assertEquals("changedCount", 0, changedCount);
		assertEquals("removedCount", 2, removedCount);
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#setProperty(net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public void testSetProperty() {
		testImpl.setObject(testObject);
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		testImpl.addListener(createListener());
		PropertyKey testKey3 = createKey();
		PropertyValue testValue3 = createValue();

		try {
			testImpl.setProperty(null, testValue1);
			fail("NullPointerException shoould have been thrown for key");

			testImpl.setProperty(testKey1, null);
			fail("NullPointerException should have been thrown for value");

			testImpl.setProperty(null, null);
			fail("NullPointerException should have been thrown for key or value or both");
		} catch (NullPointerException e) {
			// . This is expected
		}

		testImpl.setProperty(testKey1, testValue1);
		assertTrue("testImpl.hasProperty(testKey1)",
				testImpl.hasProperty(testKey1));
		assertEquals("testImpl.getPropertyValue(testKey1)", testValue1,
				testImpl.getPropertyValue(testKey1));

		testImpl.setProperty(testKey1, testValue2);
		assertTrue("testImpl.hasProperty(testKey1)",
				testImpl.hasProperty(testKey1));
		assertEquals("testImpl.getPropertyValue(testKey1)", testValue2,
				testImpl.getPropertyValue(testKey1));

		testImpl.setProperty(testKey3, testValue3);
		assertTrue("testImpl.hasProperty(testKey1)",
				testImpl.hasProperty(testKey1));
		assertEquals("testImpl.getPropertyValue(testKey1)", testValue2,
				testImpl.getPropertyValue(testKey1));
		assertTrue("testImpl.hasProperty(testKey3)",
				testImpl.hasProperty(testKey3));
		assertEquals("testImpl.getPropertyValue(testKey3)", testValue3,
				testImpl.getPropertyValue(testKey3));

		assertEquals("addedCount", 2, addedCount);
		assertEquals("changedCount", 1, changedCount);
		assertEquals("removedCount", 0, removedCount);
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#getAsBean()}.
	 */
	@Test
	public void testGetAsBean() {
		testImpl.setObject(testObject);
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();

		testImpl.setProperty(testKey1, testValue1);
		testImpl.setProperty(testKey2, testValue2);

		PropertiedObjectBean testBean = testImpl.getAsBean();
		HashMap<PropertyKey, PropertyValue> beanedProperties = testBean
				.getProperties();
		assertTrue("beanedProperties.containsKey(testKey1)",
				beanedProperties.containsKey(testKey1));
		assertTrue("beanedProperties.containsKey(testKey2)",
				beanedProperties.containsKey(testKey2));
		assertEquals("beanedProperties.size()", 2,
				beanedProperties.size());
		assertEquals("beanedProperties.get(testKey1)", testValue1,
				beanedProperties.get(testKey1));
		assertEquals("beanedProperties.get(testKey2)", testValue2,
				beanedProperties.get(testKey2));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#setFromBean(net.sf.taverna.t2.drizzle.bean.PropertiedObjectBean)}.
	 */
	@Test
	public void testSetFromBean() {
		testImpl.setObject(testObject);
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();

		testImpl.setProperty(testKey1, testValue1);
		testImpl.setProperty(testKey2, testValue2);

		PropertiedObjectBean testBean = testImpl.getAsBean();

		PropertiedObjectImpl backFromBean = new PropertiedObjectImpl();
		backFromBean.setFromBean(testBean);
		// The object is not tested as it is set in the context of a
		// PropertiedObjectSet
		assertTrue("backFromBean.hasProperty(testKey1)",
				backFromBean.hasProperty(testKey1));
		assertTrue("backFromBean.hasProperty(testKey2)",
				backFromBean.hasProperty(testKey2));
		assertEquals("backFromBean.getPropertyValue(testKey1)", testValue1,
				backFromBean.getPropertyValue(testKey1));
		assertEquals("backFromBean.getPropertyValue(testKey2)", testValue2,
				backFromBean.getPropertyValue(testKey2));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#setObject(java.lang.Object)}.
	 */
	@Test
	public void testSetObject() {
		assertNull(testImpl.getObject());
		try {
			testImpl.setObject(null);
			fail("NullPointerException should have been thrown for object");
		} catch (NullPointerException e) {
			// This is expected
		}
		testImpl.setObject(testObject);
		assertEquals("testImpl.getObject()", testObject, testImpl.getObject());

		// Setting the value twice should throw an exception
		try {
			testImpl.setObject(testObject);
			fail("IllegalStateException should have been thrown");
		} catch (IllegalStateException e) {
			// This is expected
		}
	}

}
