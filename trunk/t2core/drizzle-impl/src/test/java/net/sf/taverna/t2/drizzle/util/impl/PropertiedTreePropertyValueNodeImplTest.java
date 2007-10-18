/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
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
public class PropertiedTreePropertyValueNodeImplTest {
	
	private PropertiedTreePropertyValueNode<ExampleObject> testImpl;

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
		testImpl = new PropertiedTreePropertyValueNodeImpl<ExampleObject> ();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#PropertiedTreePropertyValueNodeImpl()}.
	 */
	@Test
	public final void testPropertiedTreePropertyValueNodeImpl() {
		assertNull(testImpl.getKey());
		assertNull(testImpl.getValue());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#getKey()}.
	 */
	@Test
	public final void testGetKey() {
		assertNull(testImpl.getKey());
		
		PropertyKey key = new ExampleKey();
		testImpl.setKey(key);
		assertEquals(key, testImpl.getKey());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#getValue()}.
	 */
	@Test
	public final void testGetValue() {
		assertNull(testImpl.getValue());
		
		PropertyValue value = new ExampleValue();
		testImpl.setValue(value);
		assertEquals(value, testImpl.getValue());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#setKey(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public final void testSetKey() {
		try {
			testImpl.setKey(null);
			fail("NullPointerException excpected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertyKey key = new ExampleKey();
		testImpl.setKey(key);
		assertEquals(key, testImpl.getKey());
		
		try {
			testImpl.setKey(new ExampleKey());
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is OK
		}
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#setValue(net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public final void testSetValue() {
		try {
			testImpl.setValue(null);
			// This is OK
		}
		catch (NullPointerException e) {
			fail ("NullPointer should be OK");
		}
		PropertyValue value = new ExampleValue();
		testImpl.setValue(value);
		assertEquals(value, testImpl.getValue());
		
		try {
			testImpl.setValue(new ExampleValue());
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is OK
		}
		
		try {
			testImpl.setValue(null);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is OK
		}	}

}
