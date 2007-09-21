/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectListener;
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
	
	private PropertiedObjectSetImpl testImpl;

	private int addedCount;

	private int changedCount;

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
		testImpl = new PropertiedObjectSetImpl ();
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
						addedCount++;
					}
					public void propertyRemoved (Object o, PropertyKey key, PropertyValue value) {
						removedCount++;
					}
					public void propertyChanged (Object o, PropertyKey key, PropertyValue oldValue, PropertyValue newValue) {
						changedCount++;
					}
		};
	}
	
	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#PropertiedObjectSetImpl()}.
	 */
	@Test
	public void testPropertiedObjectSetImpl() {
		assertEquals(testImpl.getObjects().size(), 0);
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
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#addListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener)}.
	 */
	@Test
	public void testAddListener() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#addObject(java.lang.Object)}.
	 */
	@Test
	public void testAddObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#containsObject(java.lang.Object)}.
	 */
	@Test
	public void testContainsObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#containsPropertiedObject(net.sf.taverna.t2.drizzle.util.PropertiedObject)}.
	 */
	@Test
	public void testContainsPropertiedObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getAllPropertyKeys()}.
	 */
	@Test
	public void testGetAllPropertyKeys() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getAllPropertyValues(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testGetAllPropertyValues() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getObjects()}.
	 */
	@Test
	public void testGetObjects() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getPropertiedObject(java.lang.Object)}.
	 */
	@Test
	public void testGetPropertiedObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getPropertiedObjects()}.
	 */
	@Test
	public void testGetPropertiedObjects() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeAllObjectsListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectListener)}.
	 */
	@Test
	public void testRemoveAllObjectsListener() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeListener(net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener)}.
	 */
	@Test
	public void testRemoveListener() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeObject(java.lang.Object)}.
	 */
	@Test
	public void testRemoveObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#removeProperty(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public void testRemoveProperty() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#setProperty(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public void testSetProperty() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#getAsBean()}.
	 */
	@Test
	public void testGetAsBean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl#setFromBean(net.sf.taverna.t2.drizzle.bean.PropertiedObjectSetBean)}.
	 */
	@Test
	public void testSetFromBean() {
		fail("Not yet implemented"); // TODO
	}

}
