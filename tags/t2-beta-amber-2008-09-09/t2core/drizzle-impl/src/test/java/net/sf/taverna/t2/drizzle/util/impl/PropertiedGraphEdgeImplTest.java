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
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphNode;
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
public class PropertiedGraphEdgeImplTest {
	
	private PropertiedGraphEdgeImpl<ExampleObject> testImpl;
	
	private PropertiedGraphNode<ExampleObject> createNode() {
		return new PropertiedGraphNodeImpl<ExampleObject> ();
	}
	
	private PropertyKey createKey() {
		return new ExampleKey();
	}
	
	private PropertyValue createValue() {
		return new ExampleValue();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Nothing to do
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Nothing to do
	}

	@Before
	public void setUp() throws Exception {
		this.testImpl = new PropertiedGraphEdgeImpl<ExampleObject> ();
	}

	@After
	public void tearDown() throws Exception {
		// Nothing to do
	}

	@Test
	public final void testPropertiedGraphEdgeImpl() {
		assertEquals(0, this.testImpl.getNodes().size());
		assertNull(this.testImpl.getKey());
		assertNull(this.testImpl.getValue());
	}

	@Test
	public final void testGetKey() {
		PropertyKey testKey = createKey();
		this.testImpl.setKey(testKey);
		assertEquals(testKey, this.testImpl.getKey());
	}

	@Test
	public final void testGetNodes() {
		PropertiedGraphNode<ExampleObject> testNode1 = createNode();
		PropertiedGraphNode<ExampleObject> testNode2 = createNode();
		this.testImpl.addNode(testNode1);
		this.testImpl.addNode(testNode2);
		assertEquals(2, this.testImpl.getNodes().size());
		assertTrue(this.testImpl.getNodes().contains(testNode1));
		assertTrue(this.testImpl.getNodes().contains(testNode2));
	}

	@Test
	public final void testGetValue() {
		PropertyValue testValue = createValue();
		this.testImpl.setValue(testValue);
		assertEquals(testValue, this.testImpl.getValue());
	}

	@Test
	public final void testAddNode() {
		try {
			this.testImpl.addNode(null);
			fail("NullPointerException should have been thrown for node"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedGraphNode<ExampleObject> testNode1 = createNode();
		PropertiedGraphNode<ExampleObject> testNode2 = createNode();
		this.testImpl.addNode(testNode1);
		assertEquals("testImpl.getNodes().size()", 1, this.testImpl.getNodes().size()); //$NON-NLS-1$
		assertTrue("testImpl.getNodes(),contains(testNode1)", //$NON-NLS-1$
				this.testImpl.getNodes().contains(testNode1));
		assertFalse("testImpl.getNodes(),contains(testNode2)", //$NON-NLS-1$
				this.testImpl.getNodes().contains(testNode2));
		
		// Check adding twice has no effect
		this.testImpl.addNode(testNode1);
		assertEquals("testImpl.getNodes().size()", 1, this.testImpl.getNodes().size()); //$NON-NLS-1$
		assertTrue("testImpl.getNodes(),contains(testNode1)", //$NON-NLS-1$
				this.testImpl.getNodes().contains(testNode1));
		assertFalse("testImpl.getNodes(),contains(testNode2)", //$NON-NLS-1$
				this.testImpl.getNodes().contains(testNode2));
		
		this.testImpl.addNode(testNode2);
		assertEquals("testImpl.getNodes().size()", 2, this.testImpl.getNodes().size()); //$NON-NLS-1$
		assertTrue("testImpl.getNodes(),contains(testNode1)", //$NON-NLS-1$
				this.testImpl.getNodes().contains(testNode1));
		assertTrue("testImpl.getNodes(),contains(testNode2)", //$NON-NLS-1$
				this.testImpl.getNodes().contains(testNode2));
	}

	@Test
	public final void testRemoveNode() {
		try {
			this.testImpl.removeNode(null);
			fail("NullPointerException should have been thrown for node"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedGraphNode<ExampleObject> testNode1 = createNode();
		PropertiedGraphNode<ExampleObject> testNode2 = createNode();
		this.testImpl.addNode(testNode1);
		this.testImpl.addNode(testNode2);
		Set<PropertiedGraphNode<ExampleObject>> nodes = this.testImpl.getNodes();
		assertEquals("testImpl.getNodes().size", 2, nodes.size()); //$NON-NLS-1$
		assertTrue("testImpl.getNodes().contains(testNode1)", nodes.contains(testNode1)); //$NON-NLS-1$
		assertTrue("testImpl.getNodes().contains(testNode2)", nodes.contains(testNode2)); //$NON-NLS-1$
		
		this.testImpl.removeNode(testNode1);
		nodes = this.testImpl.getNodes();
		assertEquals("testImpl.getNodes().size", 1, nodes.size()); //$NON-NLS-1$
		assertFalse("testImpl.getNodes().contains(testNode1)", nodes.contains(testNode1)); //$NON-NLS-1$
		assertTrue("testImpl.getNodes().contains(testNode2)", nodes.contains(testNode2)); //$NON-NLS-1$

		// Check that removing twice does nothing
		this.testImpl.removeNode(testNode1);
		nodes = this.testImpl.getNodes();
		assertEquals("testImpl.getNodes().size", 1, nodes.size()); //$NON-NLS-1$
		assertFalse("testImpl.getNodes().contains(testNode1)", nodes.contains(testNode1)); //$NON-NLS-1$
		assertTrue("testImpl.getNodes().contains(testNode2)", nodes.contains(testNode2)); //$NON-NLS-1$
		
		this.testImpl.removeNode(testNode2);
		nodes = this.testImpl.getNodes();
		assertEquals("testImpl.getNodes().size", 0, nodes.size()); //$NON-NLS-1$
		assertFalse("testImpl.getNodes().contains(testNode1)", nodes.contains(testNode1)); //$NON-NLS-1$
		assertFalse("testImpl.getNodes().contains(testNode2)", nodes.contains(testNode2)); //$NON-NLS-1$
	}

	@Test
	public final void testSetKey() {
		PropertyKey testKey = createKey();
		try {
			this.testImpl.setKey(null);
			fail("NullPointerException should have been thrown for key"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			this.testImpl.setKey(testKey);
			this.testImpl.setKey(testKey);
			fail("IllegalStateException should have been thrown for key"); //$NON-NLS-1$
		}
		catch (IllegalStateException e) {
			// This is expected
		}
		assertEquals("testImpl.getKey()", testKey, this.testImpl.getKey()); //$NON-NLS-1$
	}

	@Test
	public final void testSetValue() {
		PropertyValue testValue = createValue();
		try {
			this.testImpl.setValue(null);
			fail("NullPointerException should have been thrown for value"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			this.testImpl.setValue(testValue);
			this.testImpl.setValue(testValue);
			fail("IllegalStateException should have been thrown for value"); //$NON-NLS-1$
		}
		catch (IllegalStateException e) {
			// This is expected
		}
		assertEquals("testImpl.getValue()", testValue, this.testImpl.getValue()); //$NON-NLS-1$
	}

}
