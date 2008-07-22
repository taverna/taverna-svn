/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

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

	int addedCount;

	int changedCount;

	int removedCount;

	private PropertiedObjectListener createListener() {
		return new PropertiedObjectListener() {
			public void propertyAdded(Object o, PropertyKey key,
					PropertyValue value) {
				PropertiedObjectImplTest.this.addedCount++;
			}

			public void propertyRemoved(Object o, PropertyKey key,
					PropertyValue value) {
				PropertiedObjectImplTest.this.removedCount++;
			}

			public void propertyChanged(Object o, PropertyKey key,
					PropertyValue oldValue, PropertyValue newValue) {
				PropertiedObjectImplTest.this.changedCount++;
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
		// Nothing to do
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Nothing to do
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.addedCount = 0;
		this.changedCount = 0;
		this.removedCount = 0;
		this.testImpl = new PropertiedObjectImpl<ExampleObject>();
		this.testObject = new ExampleObject();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// Nothing to do
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#PropertiedObjectImpl()}.
	 */
	@Test
	public void testPropertiedObjectImpl() {
		assertNull(this.testImpl.getObject());
		assertEquals("testImpl.getPropertyKeys().size()", 0, //$NON-NLS-1$
				this.testImpl.getPropertyKeys().size());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#addListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testAddListener() {
		this.testImpl.setObject(this.testObject);

		try {
			this.testImpl.addListener(null);
			fail("NullPointerException should have been thrown"); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// This is expected
		}
		this.testImpl.addListener(createListener());
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();

		this.testImpl.setProperty(testKey, testValue1);
		assertEquals("addedCount", 1, this.addedCount); //$NON-NLS-1$
		assertEquals("changedCount", 0, this.changedCount); //$NON-NLS-1$
		assertEquals("removedCount", 0, this.removedCount); //$NON-NLS-1$

		this.testImpl.setProperty(testKey, testValue2);
		assertEquals("addedCount", 1, this.addedCount); //$NON-NLS-1$
		assertEquals("changedCount", 1, this.changedCount); //$NON-NLS-1$
		assertEquals("removedCount", 0, this.removedCount); //$NON-NLS-1$

		this.testImpl.removeProperty(testKey);
		assertEquals("addedCount", 1, this.addedCount); //$NON-NLS-1$
		assertEquals("changedCount", 1, this.changedCount); //$NON-NLS-1$
		assertEquals("removedCount", 1, this.removedCount); //$NON-NLS-1$
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#hasProperty(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testHasProperty() {
		this.testImpl.setObject(this.testObject);
		try {
			this.testImpl.hasProperty(null);
			fail("NullPointerException should have been thrown"); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// This is expected
		}
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		assertFalse("testImpl.hasProperty(testKey)", //$NON-NLS-1$
				this.testImpl.hasProperty(testKey));
		this.testImpl.setProperty(testKey, testValue1);
		assertTrue("testImpl.hasProperty(testKey)", //$NON-NLS-1$
				this.testImpl.hasProperty(testKey));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#getObject()}.
	 */
	@Test
	public void testGetObject() {
		assertNull("testImpl.getObject()", this.testImpl.getObject()); //$NON-NLS-1$
		this.testImpl.setObject(this.testObject);
		assertEquals("testImpl.getObject()", this.testObject, this.testImpl.getObject()); //$NON-NLS-1$
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#getPropertyKeys()}.
	 */
	@Test
	public void testGetPropertyKeys() {
		this.testImpl.setObject(this.testObject);
		assertEquals("testImpl.getPropertyKeys().size()", 0, //$NON-NLS-1$
				this.testImpl.getPropertyKeys().size());

		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		this.testImpl.setProperty(testKey1, testValue1);
		Set<PropertyKey> keys = this.testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 1, keys.size()); //$NON-NLS-1$
		assertTrue("testImpl.getPropertyKeys().contains(testKey1)", //$NON-NLS-1$
				keys.contains(testKey1));

		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();
		this.testImpl.setProperty(testKey2, testValue2);
		keys = this.testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 2, keys.size()); //$NON-NLS-1$
		assertTrue("testImpl.getPropertyKeys().contains(testKey1)", //$NON-NLS-1$
				keys.contains(testKey1));
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)", //$NON-NLS-1$
				keys.contains(testKey2));

		this.testImpl.removeProperty(testKey1);
		keys = this.testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 1, keys.size()); //$NON-NLS-1$
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)", //$NON-NLS-1$
				keys.contains(testKey2));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#getPropertyValue(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testGetPropertyValue() {
		this.testImpl.setObject(this.testObject);
		try {
			this.testImpl.getPropertyValue(null);
			fail("NullPointerException should have been thrown"); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// This is expected
		}
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();

		this.testImpl.setProperty(testKey, testValue1);
		assertEquals("testImpl.getPropertyValue(testKey)", testValue1, //$NON-NLS-1$
				this.testImpl.getPropertyValue(testKey));
		this.testImpl.setProperty(testKey, testValue2);
		assertEquals("testImpl.getPropertyValue(testKey)", testValue2, //$NON-NLS-1$
				this.testImpl.getPropertyValue(testKey));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#removeListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testRemoveListener() {
		this.testImpl.setObject(this.testObject);

		try {
			this.testImpl.removeListener(null);
			fail("NullPointerException should have been thrown"); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectListener testListener = createListener();
		this.testImpl.addListener(testListener);
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();

		this.testImpl.setProperty(testKey, testValue1);
		assertEquals("addedCount", 1, this.addedCount); //$NON-NLS-1$
		assertEquals("changedCount", 0, this.changedCount); //$NON-NLS-1$
		assertEquals("removedCount", 0, this.removedCount); //$NON-NLS-1$
		this.testImpl.removeListener(testListener);
		this.testImpl.setProperty(testKey, testValue2);
		this.testImpl.removeProperty(testKey);
		assertEquals("addedCount", 1, this.addedCount); //$NON-NLS-1$
		assertEquals("changedCount", 0, this.changedCount); //$NON-NLS-1$
		assertEquals("removedCount", 0, this.removedCount); //$NON-NLS-1$
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#removeProperty(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testRemoveProperty() {
		this.testImpl.setObject(this.testObject);
		PropertiedObjectListener testListener = createListener();
		this.testImpl.addListener(testListener);
		try {
			this.testImpl.removeListener(null);
			fail("NullPointerException should have been thrown"); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// This is expected
		}

		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		this.testImpl.setProperty(testKey1, testValue1);
		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();
		this.testImpl.setProperty(testKey2, testValue2);
		Set<PropertyKey> keys = this.testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 2, keys.size()); //$NON-NLS-1$
		assertTrue("testImpl.getPropertyKeys().contains(testKey1)", //$NON-NLS-1$
				keys.contains(testKey1));
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)", //$NON-NLS-1$
				keys.contains(testKey2));

		this.testImpl.removeProperty(testKey1);
		keys = this.testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 1, keys.size()); //$NON-NLS-1$
		assertFalse("testImpl.getPropertyKeys().contains(testKey1)", keys.contains(testKey1)); //$NON-NLS-1$
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)", keys.contains(testKey2)); //$NON-NLS-1$

		// Check removing twice does nothing
		this.testImpl.removeProperty(testKey1);
		keys = this.testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 1, keys.size()); //$NON-NLS-1$
		assertFalse("testImpl.getPropertyKeys().contains(testKey1)", //$NON-NLS-1$
				keys.contains(testKey1));
		assertTrue("testImpl.getPropertyKeys().contains(testKey2)", //$NON-NLS-1$
				keys.contains(testKey2));

		this.testImpl.removeProperty(testKey2);
		keys = this.testImpl.getPropertyKeys();
		assertEquals("testImpl.getPropertyKeys().size()", 0, keys.size()); //$NON-NLS-1$
		assertFalse("testImpl.getPropertyKeys().contains(testKey1)", //$NON-NLS-1$
				keys.contains(testKey1));
		assertFalse("testImpl.getPropertyKeys().contains(testKey2)", //$NON-NLS-1$
				keys.contains(testKey2));

		assertEquals("addedCount", 2, this.addedCount); //$NON-NLS-1$
		assertEquals("changedCount", 0, this.changedCount); //$NON-NLS-1$
		assertEquals("removedCount", 2, this.removedCount); //$NON-NLS-1$
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#setProperty(net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public void testSetProperty() {
		this.testImpl.setObject(this.testObject);
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		this.testImpl.addListener(createListener());
		PropertyKey testKey3 = createKey();
		PropertyValue testValue3 = createValue();

		try {
			this.testImpl.setProperty(null, testValue1);
			fail("NullPointerException shoould have been thrown for key"); //$NON-NLS-1$

			this.testImpl.setProperty(testKey1, null);
			fail("NullPointerException should have been thrown for value"); //$NON-NLS-1$

			this.testImpl.setProperty(null, null);
			fail("NullPointerException should have been thrown for key or value or both"); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// . This is expected
		}

		this.testImpl.setProperty(testKey1, testValue1);
		assertTrue("testImpl.hasProperty(testKey1)", //$NON-NLS-1$
				this.testImpl.hasProperty(testKey1));
		assertEquals("testImpl.getPropertyValue(testKey1)", testValue1, //$NON-NLS-1$
				this.testImpl.getPropertyValue(testKey1));

		this.testImpl.setProperty(testKey1, testValue2);
		assertTrue("testImpl.hasProperty(testKey1)", //$NON-NLS-1$
				this.testImpl.hasProperty(testKey1));
		assertEquals("testImpl.getPropertyValue(testKey1)", testValue2, //$NON-NLS-1$
				this.testImpl.getPropertyValue(testKey1));

		this.testImpl.setProperty(testKey3, testValue3);
		assertTrue("testImpl.hasProperty(testKey1)", //$NON-NLS-1$
				this.testImpl.hasProperty(testKey1));
		assertEquals("testImpl.getPropertyValue(testKey1)", testValue2, //$NON-NLS-1$
				this.testImpl.getPropertyValue(testKey1));
		assertTrue("testImpl.hasProperty(testKey3)", //$NON-NLS-1$
				this.testImpl.hasProperty(testKey3));
		assertEquals("testImpl.getPropertyValue(testKey3)", testValue3, //$NON-NLS-1$
				this.testImpl.getPropertyValue(testKey3));

		assertEquals("addedCount", 2, this.addedCount); //$NON-NLS-1$
		assertEquals("changedCount", 1, this.changedCount); //$NON-NLS-1$
		assertEquals("removedCount", 0, this.removedCount); //$NON-NLS-1$
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#getAsBean()}.
	 */
	@Test
	public void testGetAsBean() {
		this.testImpl.setObject(this.testObject);
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();

		this.testImpl.setProperty(testKey1, testValue1);
		this.testImpl.setProperty(testKey2, testValue2);

		PropertiedObjectBean testBean = this.testImpl.getAsBean();
/*		HashMapBean<PropertyKey, PropertyValue> beanedProperties = testBean
				.getProperties();
		assertTrue("beanedProperties.containsKey(testKey1)", //$NON-NLS-1$
				beanedProperties.containsKey(testKey1));
		assertTrue("beanedProperties.containsKey(testKey2)", //$NON-NLS-1$
				beanedProperties.containsKey(testKey2));
		assertEquals("beanedProperties.size()", 2, //$NON-NLS-1$
				beanedProperties.size());
		assertEquals("beanedProperties.get(testKey1)", testValue1, //$NON-NLS-1$
				beanedProperties.get(testKey1));
		assertEquals("beanedProperties.get(testKey2)", testValue2, //$NON-NLS-1$
				beanedProperties.get(testKey2));*/
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#setFromBean(net.sf.taverna.t2.drizzle.bean.PropertiedObjectBean)}.
	 */
	@Test
	public void testSetFromBean() {
		this.testImpl.setObject(this.testObject);
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();

		this.testImpl.setProperty(testKey1, testValue1);
		this.testImpl.setProperty(testKey2, testValue2);

		PropertiedObjectBean testBean = this.testImpl.getAsBean();

		PropertiedObjectImpl<ExampleObject> backFromBean = new PropertiedObjectImpl<ExampleObject>();
		backFromBean.setFromBean(testBean);
		// The object is not tested as it is set in the context of a
		// PropertiedObjectSet
		assertTrue("backFromBean.hasProperty(testKey1)", //$NON-NLS-1$
				backFromBean.hasProperty(testKey1));
		assertTrue("backFromBean.hasProperty(testKey2)", //$NON-NLS-1$
				backFromBean.hasProperty(testKey2));
		assertEquals("backFromBean.getPropertyValue(testKey1)", testValue1, //$NON-NLS-1$
				backFromBean.getPropertyValue(testKey1));
		assertEquals("backFromBean.getPropertyValue(testKey2)", testValue2, //$NON-NLS-1$
				backFromBean.getPropertyValue(testKey2));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectImpl#setObject(java.lang.Object)}.
	 */
	@Test
	public void testSetObject() {
		assertNull(this.testImpl.getObject());
		try {
			this.testImpl.setObject(null);
			fail("NullPointerException should have been thrown for object"); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// This is expected
		}
		this.testImpl.setObject(this.testObject);
		assertEquals("testImpl.getObject()", this.testObject, this.testImpl.getObject()); //$NON-NLS-1$

		// Setting the value twice should throw an exception
		try {
			this.testImpl.setObject(this.testObject);
			fail("IllegalStateException should have been thrown"); //$NON-NLS-1$
		} catch (IllegalStateException e) {
			// This is expected
		}
	}

}
