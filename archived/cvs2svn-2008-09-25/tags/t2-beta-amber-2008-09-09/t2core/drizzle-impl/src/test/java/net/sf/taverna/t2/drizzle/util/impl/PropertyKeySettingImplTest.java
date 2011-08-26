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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
		this.testImpl = new PropertyKeySettingImpl();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// Nothing to do
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#PropertyKeySettingImpl()}.
	 */
	@Test
	public final void testPropertyKeySettingImpl() {
		assertNull(this.testImpl.getPropertyKey());
		assertNull(this.testImpl.getComparator());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#getComparator()}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public final void testGetComparator() {
		assertNull(this.testImpl.getComparator());
		Comparator comparator = Collator.getInstance();
		this.testImpl.setComparator(comparator);
		assertEquals(comparator, this.testImpl.getComparator());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#getPropertyKey()}.
	 */
	@Test
	public final void testGetPropertyKey() {
		assertNull(this.testImpl.getPropertyKey());
		PropertyKey key = new ExampleKey();
		this.testImpl.setPropertyKey(key);
		assertEquals(key, this.testImpl.getPropertyKey());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl#setComparator(java.util.Comparator)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public final void testSetComparator() {
		try {
			this.testImpl.setComparator(null);
			fail("NullPointerException expected"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is OK
		}
		Comparator comparator = Collator.getInstance();
		this.testImpl.setComparator(comparator);
		assertEquals(comparator, this.testImpl.getComparator());
		
		try {
			this.testImpl.setComparator(comparator);
			fail("IllegalStateException expected"); //$NON-NLS-1$
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
			this.testImpl.setPropertyKey(null);
			fail("NullPointerException expected"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertyKey key = new ExampleKey();
		this.testImpl.setPropertyKey(key);
		assertEquals(key, this.testImpl.getPropertyKey());
		
		try {
			this.testImpl.setPropertyKey(key);
			fail("IllegalStateException expected"); //$NON-NLS-1$
		}
		catch (IllegalStateException e) {
			// This is OK
		}
	}

}
