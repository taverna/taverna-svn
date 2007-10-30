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
		this.testImpl = new PropertiedTreePropertyValueNodeImpl<ExampleObject> ();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// Nothing to do
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#PropertiedTreePropertyValueNodeImpl()}.
	 */
	@Test
	public final void testPropertiedTreePropertyValueNodeImpl() {
		assertNull(this.testImpl.getKey());
		assertNull(this.testImpl.getValue());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#getKey()}.
	 */
	@Test
	public final void testGetKey() {
		assertNull(this.testImpl.getKey());
		
		PropertyKey key = new ExampleKey();
		this.testImpl.setKey(key);
		assertEquals(key, this.testImpl.getKey());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#getValue()}.
	 */
	@Test
	public final void testGetValue() {
		assertNull(this.testImpl.getValue());
		
		PropertyValue value = new ExampleValue();
		this.testImpl.setValue(value);
		assertEquals(value, this.testImpl.getValue());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreePropertyValueNodeImpl#setKey(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public final void testSetKey() {
		try {
			this.testImpl.setKey(null);
			fail("NullPointerException excpected"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertyKey key = new ExampleKey();
		this.testImpl.setKey(key);
		assertEquals(key, this.testImpl.getKey());
		
		try {
			this.testImpl.setKey(new ExampleKey());
			fail("IllegalStateException expected"); //$NON-NLS-1$
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
			this.testImpl.setValue(null);
			// This is OK
		}
		catch (NullPointerException e) {
			fail ("NullPointer should be OK"); //$NON-NLS-1$
		}
		PropertyValue value = new ExampleValue();
		this.testImpl.setValue(value);
		assertEquals(value, this.testImpl.getValue());
		
		try {
			this.testImpl.setValue(new ExampleValue());
			fail("IllegalStateException expected"); //$NON-NLS-1$
		}
		catch (IllegalStateException e) {
			// This is OK
		}
		
		try {
			this.testImpl.setValue(null);
			fail("IllegalStateException expected"); //$NON-NLS-1$
		}
		catch (IllegalStateException e) {
			// This is OK
		}	}

}
