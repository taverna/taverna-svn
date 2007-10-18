package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		testImpl = new PropertiedGraphEdgeImpl<ExampleObject> ();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testPropertiedGraphEdgeImpl() {
		assertEquals("testImpl.getNodes().size()", 0, testImpl.getNodes().size());
		assertNull("testImpl.getKey()", testImpl.getKey());
		assertNull("testImpl.getValue()", testImpl.getValue());
	}

	@Test
	public final void testGetKey() {
		PropertyKey testKey = createKey();
		testImpl.setKey(testKey);
		assertEquals("testImpl.getKey()", testKey, testImpl.getKey());
	}

	@Test
	public final void testGetNodes() {
		PropertiedGraphNode<ExampleObject> testNode1 = createNode();
		PropertiedGraphNode<ExampleObject> testNode2 = createNode();
		testImpl.addNode(testNode1);
		testImpl.addNode(testNode2);
		assertEquals("testImpl.getNodes().size()", 2, testImpl.getNodes().size());
		assertTrue("testImpl.getNodes(),contains(testNode1)",
				testImpl.getNodes().contains(testNode1));
		assertTrue("testImpl.getNodes(),contains(testNode2)",
				testImpl.getNodes().contains(testNode2));
	}

	@Test
	public final void testGetValue() {
		PropertyValue testValue = createValue();
		testImpl.setValue(testValue);
		assertEquals("testImpl.getValue()", testValue, testImpl.getValue());
	}

	@Test
	public final void testAddNode() {
		try {
			testImpl.addNode(null);
			fail("NullPointerException should have been thrown for node");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedGraphNode<ExampleObject> testNode1 = createNode();
		PropertiedGraphNode<ExampleObject> testNode2 = createNode();
		testImpl.addNode(testNode1);
		assertEquals("testImpl.getNodes().size()", 1, testImpl.getNodes().size());
		assertTrue("testImpl.getNodes(),contains(testNode1)",
				testImpl.getNodes().contains(testNode1));
		assertFalse("testImpl.getNodes(),contains(testNode2)",
				testImpl.getNodes().contains(testNode2));
		
		// Check adding twice has no effect
		testImpl.addNode(testNode1);
		assertEquals("testImpl.getNodes().size()", 1, testImpl.getNodes().size());
		assertTrue("testImpl.getNodes(),contains(testNode1)",
				testImpl.getNodes().contains(testNode1));
		assertFalse("testImpl.getNodes(),contains(testNode2)",
				testImpl.getNodes().contains(testNode2));
		
		testImpl.addNode(testNode2);
		assertEquals("testImpl.getNodes().size()", 2, testImpl.getNodes().size());
		assertTrue("testImpl.getNodes(),contains(testNode1)",
				testImpl.getNodes().contains(testNode1));
		assertTrue("testImpl.getNodes(),contains(testNode2)",
				testImpl.getNodes().contains(testNode2));
	}

	@Test
	public final void testRemoveNode() {
		try {
			testImpl.removeNode(null);
			fail("NullPointerException should have been thrown for node");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedGraphNode<ExampleObject> testNode1 = createNode();
		PropertiedGraphNode<ExampleObject> testNode2 = createNode();
		testImpl.addNode(testNode1);
		testImpl.addNode(testNode2);
		Set<PropertiedGraphNode<ExampleObject>> nodes = testImpl.getNodes();
		assertEquals("testImpl.getNodes().size", 2, nodes.size());
		assertTrue("testImpl.getNodes().contains(testNode1)", nodes.contains(testNode1));
		assertTrue("testImpl.getNodes().contains(testNode2)", nodes.contains(testNode2));
		
		testImpl.removeNode(testNode1);
		nodes = testImpl.getNodes();
		assertEquals("testImpl.getNodes().size", 1, nodes.size());
		assertFalse("testImpl.getNodes().contains(testNode1)", nodes.contains(testNode1));
		assertTrue("testImpl.getNodes().contains(testNode2)", nodes.contains(testNode2));

		// Check that removing twice does nothing
		testImpl.removeNode(testNode1);
		nodes = testImpl.getNodes();
		assertEquals("testImpl.getNodes().size", 1, nodes.size());
		assertFalse("testImpl.getNodes().contains(testNode1)", nodes.contains(testNode1));
		assertTrue("testImpl.getNodes().contains(testNode2)", nodes.contains(testNode2));
		
		testImpl.removeNode(testNode2);
		nodes = testImpl.getNodes();
		assertEquals("testImpl.getNodes().size", 0, nodes.size());
		assertFalse("testImpl.getNodes().contains(testNode1)", nodes.contains(testNode1));
		assertFalse("testImpl.getNodes().contains(testNode2)", nodes.contains(testNode2));
	}

	@Test
	public final void testSetKey() {
		PropertyKey testKey = createKey();
		try {
			testImpl.setKey(null);
			fail("NullPointerException should have been thrown for key");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			testImpl.setKey(testKey);
			testImpl.setKey(testKey);
			fail("IllegalStateException should have been thrown for key");
		}
		catch (IllegalStateException e) {
			// This is expected
		}
		assertEquals("testImpl.getKey()", testKey, testImpl.getKey());
	}

	@Test
	public final void testSetValue() {
		PropertyValue testValue = createValue();
		try {
			testImpl.setValue(null);
			fail("NullPointerException should have been thrown for value");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			testImpl.setValue(testValue);
			testImpl.setValue(testValue);
			fail("IllegalStateException should have been thrown for value");
		}
		catch (IllegalStateException e) {
			// This is expected
		}
		assertEquals("testImpl.getValue()", testValue, testImpl.getValue());
	}

}
