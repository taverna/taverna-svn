/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.text.Collator;
import java.util.Comparator;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class PropertyKeySettingImplTest {

	private PropertyKeySetting testImpl;
	
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
		testImpl = new PropertyKeySettingImpl();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#PropertyKeySettingImpl()}.
	 */
	@Test
	public final void testPropertyKeySettingImpl() {
		assertNull(testImpl.getPropertyKey());
		assertNull(testImpl.getComparator());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#getComparator()}.
	 */
	@Test
	public final void testGetComparator() {
		assertNull(testImpl.getComparator());
		Comparator comparator = Collator.getInstance();
		testImpl.setComparator(comparator);
		assertEquals(comparator, testImpl.getComparator());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#getPropertyKey()}.
	 */
	@Test
	public final void testGetPropertyKey() {
		assertNull(testImpl.getPropertyKey());
		PropertyKey key = new ExampleKey();
		testImpl.setPropertyKey(key);
		assertEquals(key, testImpl.getPropertyKey());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#setComparator(java.util.Comparator)}.
	 */
	@Test
	public final void testSetComparator() {
		try {
			testImpl.setComparator(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		Comparator comparator = Collator.getInstance();
		testImpl.setComparator(comparator);
		assertEquals(comparator, testImpl.getComparator());
		
		try {
			testImpl.setComparator(comparator);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is OK
		}
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#setPropertyKey(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public final void testSetPropertyKey() {
		try {
			testImpl.setPropertyKey(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertyKey key = new ExampleKey();
		testImpl.setPropertyKey(key);
		assertEquals(key, testImpl.getPropertyKey());
		
		try {
			testImpl.setPropertyKey(key);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// This is OK
		}
	}

}
