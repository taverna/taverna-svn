/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class PropertiedTreeObjectNodeImplTest {
	
	private PropertiedTreeObjectNodeImpl<ExampleObject> testImpl;

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
		testImpl = new PropertiedTreeObjectNodeImpl<ExampleObject> ();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeObjectNodeImpl#getAllObjects()}.
	 */
	@Test
	public final void testGetAllObjects() {
		Set<ExampleObject> allObjects = testImpl.getAllObjects();
		assertEquals (0, allObjects.size());
		
		ExampleObject object = new ExampleObject();
		testImpl.setObject(object);
		allObjects = testImpl.getAllObjects();
		assertEquals(1, allObjects.size());
		assertEquals(object, testImpl.getObject());
		assertTrue(allObjects.contains(object));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeObjectNodeImpl#PropertiedTreeObjectNodeImpl()}.
	 */
	@Test
	public final void testPropertiedTreeObjectNodeImpl() {
		assertNull(testImpl.getObject());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeObjectNodeImpl#setObject(java.lang.Object)}.
	 */
	@Test
	public final void testSetObject() {
		try {
			testImpl.setObject(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		ExampleObject object = new ExampleObject();
		testImpl.setObject(object);
		assertEquals(object, testImpl.getObject());
		
		try {
			testImpl.setObject(object);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is OK
		}
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeObjectNodeImpl#getObject()}.
	 */
	@Test
	public final void testGetObject() {
		// Covered by testSetObject except for
		assertNull(testImpl.getObject());
	}

}
