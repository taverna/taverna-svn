/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.PropertiedObjectSetBean;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class PropertiedObjectSetImplTest {
	
	private PropertiedObjectSetImpl<ExampleObject> testImpl;

	int addedPropertyCount;

	int changedPropertyCount;

	int removedPropertyCount;
	
	int addedCount;
	
	int removedCount;
	
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
		this.addedPropertyCount = 0;
		this.changedPropertyCount = 0;
		this.removedPropertyCount = 0;
		this.testImpl = new PropertiedObjectSetImpl<ExampleObject> ();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// Nothing to do
	}

	private PropertiedObjectListener createObjectListener () {
		return new PropertiedObjectListener ()
			 {	
					public void propertyAdded (Object o, PropertyKey key, PropertyValue value) {
						PropertiedObjectSetImplTest.this.addedPropertyCount++;
					}
					public void propertyRemoved (Object o, PropertyKey key, PropertyValue value) {
						PropertiedObjectSetImplTest.this.removedPropertyCount++;
					}
					public void propertyChanged (Object o, PropertyKey key, PropertyValue oldValue, PropertyValue newValue) {
						PropertiedObjectSetImplTest.this.changedPropertyCount++;
					}
		};
	}
	
	private PropertiedObjectSetListener createObjectSetListener () {
		return new PropertiedObjectSetListener ()
			 {	
					public void objectAdded (PropertiedObjectSet<?> pos, Object o) {
						PropertiedObjectSetImplTest.this.addedCount++;
					}
					public void objectRemoved (PropertiedObjectSet<?> pos, Object o) {
						PropertiedObjectSetImplTest.this.removedCount++;
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
		assertFalse ("testImpl.getObjects()", this.testImpl.getObjects() == null); //$NON-NLS-1$
		assertEquals("testImpl.getObjects().size()", 0, this.testImpl.getObjects().size()); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#addAllObjectsListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testAddAllObjectsListener() {
		try {
			this.testImpl.addAllObjectsListener(null);
			fail("NullPointerException should have been thrown for listener"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		this.testImpl.addAllObjectsListener (createObjectListener());
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		this.testImpl.setProperty(testObject1, testKey1, testValue1);
		this.testImpl.setProperty(testObject2, testKey1, testValue1);
		this.testImpl.setProperty(testObject1, testKey1, testValue2);
		this.testImpl.removeProperty(testObject1, testKey1);
		assertEquals("addedPropertyCount", 2, this.addedPropertyCount); //$NON-NLS-1$
		assertEquals("changedPropertyCount", 1, this.changedPropertyCount); //$NON-NLS-1$
		assertEquals("removedPropertyCount", 1, this.removedPropertyCount); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#addListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener)}.
	 */
	@Test
	public void testAddListener() {
		try {
			this.testImpl.addListener (null);
			fail ("NullPointerException should have been thrown for listener"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		this.testImpl.addListener(createObjectSetListener());
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		this.testImpl.addObject (testObject1);
		this.testImpl.addObject (testObject2);
		this.testImpl.removeObject (testObject1);
		assertEquals ("addedCount", 2, this.addedCount); //$NON-NLS-1$
		assertEquals ("removedCount", 1, this.removedCount); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#addObject(java.lang.Object)}.
	 */
	@Test
	public void testAddObject() {
		try {
			this.testImpl.addObject (null);
			fail ("NullPointerException should have been thrown for object"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		this.testImpl.addObject (testObject);
		assertTrue ("testImpl.containsObject (testObject)", //$NON-NLS-1$
				this.testImpl.containsObject (testObject));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#containsObject(java.lang.Object)}.
	 */
	@Test
	public void testContainsObject() {
		try {
			this.testImpl.containsObject (null);
			fail ("NullPointerException should have been thrown for object"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		assertFalse ("testImpl.containsObject (testObject1)", //$NON-NLS-1$
				this.testImpl.containsObject (testObject));
		this.testImpl.addObject (testObject);
		assertTrue ("testImpl.containsObject (testObject1)", //$NON-NLS-1$
				this.testImpl.containsObject (testObject));
		this.testImpl.removeObject (testObject);
		assertFalse ("testImpl.containsObject (testObject1)", //$NON-NLS-1$
				this.testImpl.containsObject (testObject));
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
		this.testImpl.setProperty(testObject1, testKey1, testValue);
		this.testImpl.setProperty(testObject1, testKey2, testValue);
		this.testImpl.setProperty(testObject2, testKey3, testValue);
		
		Set<PropertyKey> keys = this.testImpl.getAllPropertyKeys();
		assertEquals ("keys.size()", 3, keys.size()); //$NON-NLS-1$
		assertTrue("keys.contains(testKey1)", keys.contains(testKey1)); //$NON-NLS-1$
		assertTrue("keys.contains(testKey2)", keys.contains(testKey2)); //$NON-NLS-1$
		assertTrue("keys.contains(testKey3)", keys.contains(testKey3)); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getAllPropertyValues(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testGetAllPropertyValues() {
		try {
			this.testImpl.getAllPropertyValues(null);
			fail("NullPointerException should have been thrown"); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject1 = createObject ();
		ExampleObject testObject2 = createObject ();
		ExampleObject testObject3 = createObject ();
		this.testImpl.addObject(testObject1);
		this.testImpl.addObject(testObject2);
		this.testImpl.addObject(testObject3);
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		PropertyValue testValue3 = createValue();
		this.testImpl.setProperty (testObject1, testKey, testValue1);
		this.testImpl.setProperty (testObject2, testKey, testValue2);
		this.testImpl.setProperty (testObject3, testKey, testValue3);
		
		Set<PropertyValue> values = this.testImpl.getAllPropertyValues(testKey);
		assertEquals ("values.size()", 3, values.size()); //$NON-NLS-1$
		assertTrue("values.contains(testValue1)", values.contains(testValue1)); //$NON-NLS-1$
		assertTrue("values.contains(testValue2)", values.contains(testValue2)); //$NON-NLS-1$
		assertTrue("values.contains(testValue3)", values.contains(testValue3)); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getObjects()}.
	 */
	@Test
	public void testGetObjects() {
		ExampleObject testObject1 = createObject ();
		ExampleObject testObject2 = createObject ();
		ExampleObject testObject3 = createObject ();
		this.testImpl.addObject(testObject1);
		this.testImpl.addObject(testObject2);
		this.testImpl.addObject(testObject3);
		Set<ExampleObject> objects = this.testImpl.getObjects();
		assertEquals ("objects.size()", 3, objects.size()); //$NON-NLS-1$
		assertTrue("objects.contains(testObject1)", objects.contains(testObject1)); //$NON-NLS-1$
		assertTrue("objects.contains(testObject2)", objects.contains(testObject2)); //$NON-NLS-1$
		assertTrue("objects.contains(testObject3)", objects.contains(testObject3)); //$NON-NLS-1$
		
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeAllObjectsListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testRemoveAllObjectsListener() {
		try {
			this.testImpl.removeAllObjectsListener(null);
			fail("NullPointerException should have been thrown for listener"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectListener objectListener = createObjectListener();
		this.testImpl.addAllObjectsListener (objectListener);
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		this.testImpl.setProperty(testObject1, testKey1, testValue1);
		this.testImpl.removeAllObjectsListener(objectListener);
		this.testImpl.setProperty(testObject2, testKey1, testValue1);
		this.testImpl.setProperty(testObject1, testKey1, testValue2);
		this.testImpl.removeProperty(testObject1, testKey1);
		assertEquals("addedPropertyCount", 1, this.addedPropertyCount); //$NON-NLS-1$
		assertEquals("changedPropertyCount", 0, this.changedPropertyCount); //$NON-NLS-1$
		assertEquals("removedPropertyCount", 0, this.removedPropertyCount); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener)}.
	 */
	@Test
	public void testRemoveListener() {
		try {
			this.testImpl.removeListener (null);
			fail ("NullPointerException should have been thrown for listener"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectSetListener listener = createObjectSetListener();
		this.testImpl.addListener(listener);
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		this.testImpl.addObject (testObject1);
		this.testImpl.removeListener(listener);
		this.testImpl.addObject (testObject2);
		this.testImpl.removeObject (testObject1);
		assertEquals ("addedCount", 1, this.addedCount); //$NON-NLS-1$
		assertEquals ("removedCount", 0, this.removedCount); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeObject(java.lang.Object)}.
	 */
	@Test
	public void testRemoveObject() {
		try {
			this.testImpl.removeObject (null);
			fail ("NullPointerException should have been thrown for object"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		this.testImpl.addObject(testObject);
		assertTrue ("testImpl.containsObject (testObject)", //$NON-NLS-1$
				this.testImpl.containsObject (testObject));
		this.testImpl.removeObject (testObject);
		assertFalse ("testImpl.containsObject (testObject)", //$NON-NLS-1$
				this.testImpl.containsObject (testObject));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeProperty(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testRemoveProperty() {
		try {
			this.testImpl.removeProperty(null, null);
			fail ("NullPointerException should have been thrown for object or key"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		PropertyKey testKey1 = createKey();
		PropertyValue testValue = createValue();
		PropertyKey testKey2 = createKey();
		try {
			this.testImpl.removeProperty(testObject, null);
			fail ("NullPointerException should have been thrown for key"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			this.testImpl.removeProperty(null, testKey1);
			fail ("NullPointerException should have been thrown for object"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		this.testImpl.setProperty (testObject, testKey1, testValue);
		this.testImpl.setProperty (testObject, testKey2, testValue);
		this.testImpl.removeProperty (testObject, testKey1);
		assertFalse(this.testImpl.hasProperty(testObject, testKey1));
		assertTrue(this.testImpl.hasProperty(testObject,testKey2));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#setProperty(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public void testSetProperty() {
		try {
			this.testImpl.setProperty(null, null, null);
			fail ("NullPointerException should have been thrown for object, key or value"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		ExampleObject testObject = createObject();
		PropertyKey testKey1 = createKey();
		PropertyValue testValue = createValue();
		PropertyKey testKey2 = createKey();
		try {
			this.testImpl.setProperty(null, testKey1, testValue);
			fail ("NullPointerException should have been thrown for object"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			this.testImpl.setProperty(testObject, null, testValue);
			fail ("NullPointerException should have been thrown for key"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			this.testImpl.setProperty(testObject, testKey1, null);
			fail ("NullPointerException should have been thrown for value"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		this.testImpl.addObject(testObject);
		this.testImpl.setProperty (testObject, testKey1, testValue);
		this.testImpl.setProperty (testObject, testKey2, testValue);
		assertTrue(this.testImpl.hasProperty(testObject, testKey1));
		assertTrue(this.testImpl.hasProperty(testObject, testKey2));
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
		this.testImpl.setProperty (testObject1, testKey1, testValue1);
		this.testImpl.setProperty (testObject2, testKey1, testValue1);
		this.testImpl.setProperty (testObject2, testKey2, testValue2);
		
/*		PropertiedObjectSetBean<ExampleObject> testBean = this.testImpl.getAsBean();
		HashMap<ExampleObject, PropertiedObjectBean> beanedMap =
			testBean.getPropertiedObjectMap();
		assertEquals("beanedMap.size()", beanedMap.size(),2); //$NON-NLS-1$
		assertTrue("beanedMap.containsKey(testObject1)", beanedMap.containsKey(testObject1)); //$NON-NLS-1$
		assertTrue("beanedMap.containsKey(testObject2)", beanedMap.containsKey(testObject2)); //$NON-NLS-1$
		
		PropertiedObjectBean beanedObject1 = beanedMap.get(testObject1);
		HashMap<PropertyKey, PropertyValue> beanedProperties1 = beanedObject1.getProperties();
		assertEquals("beanedProperties1.size()", 1, beanedProperties1.size()); //$NON-NLS-1$
		assertTrue("beanedProperties1.containsKey(testKey1)", //$NON-NLS-1$
				beanedProperties1.containsKey(testKey1));
		assertEquals("beanedProperties1.get(testKey1)", //$NON-NLS-1$
				testValue1, beanedProperties1.get(testKey1));

		PropertiedObjectBean beanedObject2 = beanedMap.get(testObject2);
		HashMap<PropertyKey, PropertyValue> beanedProperties2 = beanedObject2.getProperties();
		assertEquals("beanedProperties2.size()", 2, beanedProperties2.size()); //$NON-NLS-1$
		assertTrue("beanedProperties2.containsKey(testKey1)", //$NON-NLS-1$
				beanedProperties2.containsKey(testKey1));
		assertEquals("beanedProperties2.get(testKey1)", //$NON-NLS-1$
				testValue1, beanedProperties2.get(testKey1));
		assertTrue("beanedProperties2.containsKey(testKey2)", //$NON-NLS-1$
				beanedProperties2.containsKey(testKey2));
		assertEquals("beanedProperties2.get(testKey2)",   //$NON-NLS-1$
				testValue2, beanedProperties2.get(testKey2));*/
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#setFromBean(net.sf.taverna.t2.drizzle.bean.PropertiedObjectSetBean)}.
	 */
	@Test
	@Ignore
	public void testSetFromBean() {
		ExampleObject testObject1 = createObject();
		ExampleObject testObject2 = createObject();
		PropertyKey testKey1 = createKey();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		this.testImpl.setProperty (testObject1, testKey1, testValue1);
		this.testImpl.setProperty (testObject2, testKey1, testValue1);
		this.testImpl.setProperty (testObject2, testKey2, testValue2);
		
		PropertiedObjectSetBean<ExampleObject> testBean = this.testImpl.getAsBean();
		PropertiedObjectSetImpl<ExampleObject> backFromBean =
			new PropertiedObjectSetImpl<ExampleObject>();
		backFromBean.setFromBean (testBean);
		
		assertEquals("backFromBean.getObjects.size()", //$NON-NLS-1$
				2, backFromBean.getObjects().size());
		assertTrue("backFromBean.containsObject(testObject1)", //$NON-NLS-1$
				backFromBean.containsObject(testObject1));
		assertTrue("backFromBean.containsObject(testObject2)", //$NON-NLS-1$
				backFromBean.containsObject(testObject2));
/*		PropertiedObject<ExampleObject> po1 =
			backFromBean.getPropertiedObject(testObject1);
		assertEquals("po1.getPropertyKeys().size()", //$NON-NLS-1$
				1, po1.getPropertyKeys().size());
		assertEquals("po1.getPropertyValue(testKey1)", //$NON-NLS-1$
				testValue1, po1.getPropertyValue(testKey1));
		
		PropertiedObject<ExampleObject> po2 =
			backFromBean.getPropertiedObject(testObject2);
		assertEquals("po2.getPropertyKeys().size()", //$NON-NLS-1$
				2, po2.getPropertyKeys().size());
		assertEquals("po2.getPropertyValue(testKey1)", //$NON-NLS-1$
				testValue1, po2.getPropertyValue(testKey1));
		assertEquals("po2.getPropertyValue(testKey2)", //$NON-NLS-1$
				testValue2, po2.getPropertyValue(testKey2));*/
	}

}
