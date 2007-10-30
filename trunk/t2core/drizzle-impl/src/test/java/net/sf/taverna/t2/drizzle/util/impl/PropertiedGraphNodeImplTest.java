package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class PropertiedGraphNodeImplTest {
	
	private PropertiedGraphNodeImpl<ExampleObject> testImpl;
	
	private PropertiedGraphEdge<ExampleObject> createEdge () {
		return new PropertiedGraphEdgeImpl<ExampleObject> ();
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
		this.testImpl = new PropertiedGraphNodeImpl<ExampleObject> ();
	}

	@After
	public void tearDown() throws Exception {
		// Nothing to do
	}

	@Test
	public final void testPropertiedGraphNodeImpl() {
		Set<PropertiedGraphEdge<ExampleObject>> edges = this.testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 0, edges.size()); //$NON-NLS-1$
	}

	@Test
	public final void testGetEdges() {
		PropertiedGraphEdge<ExampleObject> testEdge1 = createEdge();
		PropertiedGraphEdge<ExampleObject> testEdge2 = createEdge();
		this.testImpl.addEdge(testEdge1);
		this.testImpl.addEdge(testEdge2);
		Set<PropertiedGraphEdge<ExampleObject>> edges = this.testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 2, edges.size()); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1)); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2)); //$NON-NLS-1$
	}

	@Test
	public final void testGetObject() {
		assertNull("testImpl.getObject()", this.testImpl.getObject()); //$NON-NLS-1$
		ExampleObject testObject = new ExampleObject();
		this.testImpl.setObject(testObject);
		assertEquals("testImpl.getObject()", testObject, this.testImpl.getObject()); //$NON-NLS-1$
	}

	@Test
	public final void testSetObject() {
		ExampleObject testObject = new ExampleObject();
		try {
			this.testImpl.setObject(null);
			fail("NullPointerException should have been thrown for object"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			this.testImpl.setObject(testObject);
			this.testImpl.setObject(testObject);
			fail("IllegalStateException should have been thrown for object"); //$NON-NLS-1$
		}
		catch (IllegalStateException e) {
			// This is expected
		}
		assertEquals("testImpl.getObject()", testObject, this.testImpl.getObject()); //$NON-NLS-1$
	}

	@Test
	public final void testAddEdge() {
		try {
			this.testImpl.addEdge(null);
			fail("NullPointerException should have been thrown for edge"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedGraphEdge<ExampleObject> testEdge1 = createEdge();
		this.testImpl.addEdge(testEdge1);
		Set<PropertiedGraphEdge<ExampleObject>> edges = this.testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 1, edges.size()); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1)); //$NON-NLS-1$
		PropertiedGraphEdge<ExampleObject> testEdge2 = createEdge();
		this.testImpl.addEdge(testEdge2);
		edges = this.testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 2, edges.size()); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1)); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2)); //$NON-NLS-1$
	}

	@Test
	public final void testRemoveEdge() {
		try {
			this.testImpl.removeEdge(null);
			fail("NullPointerException should have been thrown for edge"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedGraphEdge<ExampleObject> testEdge1 = createEdge();
		PropertiedGraphEdge<ExampleObject> testEdge2 = createEdge();
		this.testImpl.addEdge(testEdge1);
		this.testImpl.addEdge(testEdge2);
		Set<PropertiedGraphEdge<ExampleObject>> edges = this.testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 2, edges.size()); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1)); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2)); //$NON-NLS-1$
		
		this.testImpl.removeEdge(testEdge1);
		edges = this.testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 1, edges.size()); //$NON-NLS-1$
		assertFalse("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1)); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2)); //$NON-NLS-1$

		// Check that removing twice does nothing
		this.testImpl.removeEdge(testEdge1);
		edges = this.testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 1, edges.size()); //$NON-NLS-1$
		assertFalse("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1)); //$NON-NLS-1$
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2)); //$NON-NLS-1$
		
		this.testImpl.removeEdge(testEdge2);
		edges = this.testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 0, edges.size()); //$NON-NLS-1$
		assertFalse("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1)); //$NON-NLS-1$
		assertFalse("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2)); //$NON-NLS-1$
	}

}
