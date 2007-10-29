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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		testImpl = new PropertiedGraphNodeImpl<ExampleObject> ();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testPropertiedGraphNodeImpl() {
		Set<PropertiedGraphEdge<ExampleObject>> edges = testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 0, edges.size());
	}

	@Test
	public final void testGetEdges() {
		PropertiedGraphEdge<ExampleObject> testEdge1 = createEdge();
		PropertiedGraphEdge<ExampleObject> testEdge2 = createEdge();
		testImpl.addEdge(testEdge1);
		testImpl.addEdge(testEdge2);
		Set<PropertiedGraphEdge<ExampleObject>> edges = testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 2, edges.size());
		assertTrue("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1));
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2));
	}

	@Test
	public final void testGetObject() {
		assertNull("testImpl.getObject()", testImpl.getObject());
		ExampleObject testObject = new ExampleObject();
		testImpl.setObject(testObject);
		assertEquals("testImpl.getObject()", testObject, testImpl.getObject());
	}

	@Test
	public final void testSetObject() {
		ExampleObject testObject = new ExampleObject();
		try {
			testImpl.setObject(null);
			fail("NullPointerException should have been thrown for object");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			testImpl.setObject(testObject);
			testImpl.setObject(testObject);
			fail("IllegalStateException should have been thrown for object");
		}
		catch (IllegalStateException e) {
			// This is expected
		}
		assertEquals("testImpl.getObject()", testObject, testImpl.getObject());
	}

	@Test
	public final void testAddEdge() {
		try {
			testImpl.addEdge(null);
			fail("NullPointerException should have been thrown for edge");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedGraphEdge<ExampleObject> testEdge1 = createEdge();
		testImpl.addEdge(testEdge1);
		Set<PropertiedGraphEdge<ExampleObject>> edges = testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 1, edges.size());
		assertTrue("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1));
		PropertiedGraphEdge<ExampleObject> testEdge2 = createEdge();
		testImpl.addEdge(testEdge2);
		edges = testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 2, edges.size());
		assertTrue("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1));
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2));
	}

	@Test
	public final void testRemoveEdge() {
		try {
			testImpl.removeEdge(null);
			fail("NullPointerException should have been thrown for edge");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedGraphEdge<ExampleObject> testEdge1 = createEdge();
		PropertiedGraphEdge<ExampleObject> testEdge2 = createEdge();
		testImpl.addEdge(testEdge1);
		testImpl.addEdge(testEdge2);
		Set<PropertiedGraphEdge<ExampleObject>> edges = testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 2, edges.size());
		assertTrue("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1));
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2));
		
		testImpl.removeEdge(testEdge1);
		edges = testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 1, edges.size());
		assertFalse("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1));
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2));

		// Check that removing twice does nothing
		testImpl.removeEdge(testEdge1);
		edges = testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 1, edges.size());
		assertFalse("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1));
		assertTrue("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2));
		
		testImpl.removeEdge(testEdge2);
		edges = testImpl.getEdges();
		assertEquals("testImpl.getEdges().size", 0, edges.size());
		assertFalse("testImpl.getEdges().contains(testEdge1)", edges.contains(testEdge1));
		assertFalse("testImpl.getEdges().contains(testEdge2)", edges.contains(testEdge2));
	}

}
