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
		this.testImpl = new PropertiedTreeObjectNodeImpl<ExampleObject> ();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// Nothing to do
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeObjectNodeImpl#getAllObjects()}.
	 */
	@Test
	public final void testGetAllObjects() {
		Set<ExampleObject> allObjects = this.testImpl.getAllObjects();
		assertEquals (0, allObjects.size());
		
		ExampleObject object = new ExampleObject();
		this.testImpl.setObject(object);
		allObjects = this.testImpl.getAllObjects();
		assertEquals(1, allObjects.size());
		assertEquals(object, this.testImpl.getObject());
		assertTrue(allObjects.contains(object));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeObjectNodeImpl#PropertiedTreeObjectNodeImpl()}.
	 */
	@Test
	public final void testPropertiedTreeObjectNodeImpl() {
		assertNull(this.testImpl.getObject());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeObjectNodeImpl#setObject(java.lang.Object)}.
	 */
	@Test
	public final void testSetObject() {
		try {
			this.testImpl.setObject(null);
			fail("NullPointerException expected"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is OK
		}
		ExampleObject object = new ExampleObject();
		this.testImpl.setObject(object);
		assertEquals(object, this.testImpl.getObject());
		
		try {
			this.testImpl.setObject(object);
			fail("IllegalStateException expected"); //$NON-NLS-1$
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
		assertNull(this.testImpl.getObject());
	}

}
