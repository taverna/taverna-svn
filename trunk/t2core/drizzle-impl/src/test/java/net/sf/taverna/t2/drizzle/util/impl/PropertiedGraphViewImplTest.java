/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphNode;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
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
public class PropertiedGraphViewImplTest {

	private PropertiedGraphView<ExampleObject> testImpl;

	private int addedNodeCount;

	private int removedNodeCount;

	private int addedEdgeCount;

	private int removedEdgeCount;

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
		addedNodeCount = 0;
		removedNodeCount = 0;
		addedEdgeCount = 0;
		removedEdgeCount = 0;
		testImpl = new PropertiedGraphViewImpl<ExampleObject>();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	private PropertiedGraphViewListener<ExampleObject> createListener() {
		return new PropertiedGraphViewListener<ExampleObject>() {
			public void edgeAdded(PropertiedGraphView view,
					PropertiedGraphEdge edge, PropertiedGraphNode node) {
				addedEdgeCount++;
			}

			public void edgeRemoved(PropertiedGraphView view,
					PropertiedGraphEdge edge, PropertiedGraphNode node) {
				removedEdgeCount++;
			}

			public void nodeAdded(PropertiedGraphView view,
					PropertiedGraphNode node) {
				addedNodeCount++;
			}

			public void nodeRemoved(PropertiedGraphView view,
					PropertiedGraphNode node) {
				removedNodeCount++;
			}
		};
	}

	private PropertiedObjectSet<ExampleObject> createPos() {
		return new PropertiedObjectSetImpl<ExampleObject>();
	}
	
	private PropertyKey createKey() {
		return new ExampleKey();
	}

	private PropertyValue createValue() {
		return new ExampleValue();
	}
	
	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#PropertiedGraphViewImpl()}.
	 */
	@Test
	public final void testPropertiedGraphViewImpl() {
		assertEquals("testImpl.getEdges().size()", 0, testImpl.getEdges()
				.size());
		assertEquals("testImpl.getNodes().size()", 0, testImpl.getNodes()
				.size());
		assertNull("testImpl.getPropertiedObjectSet()", testImpl
				.getPropertiedObjectSet());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#addListener(net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener)}.
	 */
	@Test
	public final void testAddListener() {
		try {
			testImpl.addListener(null);
			fail("NullPointerException should be thrown for listener");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		testImpl.setPropertiedObjectSet(testPos);
		PropertiedGraphViewListener<ExampleObject> testListener = createListener();
		testImpl.addListener(testListener);

		ExampleObject testObject = new ExampleObject();
		
		// Should add a node
		testPos.addObject(testObject);
		
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		
		// Should add an edge)
		testPos.setProperty(testObject, testKey, testValue1);
		
		// Should remove an edge and add a new one
		testPos.setProperty(testObject, testKey, testValue2);
		
		// Should remove a node and its edge
		testPos.removeObject(testObject);
		assertEquals("addedNodeCount", 1, addedNodeCount);
		assertEquals("removedNodeCount", 1, removedNodeCount);
		assertEquals("addedEdgeCount", 2, addedEdgeCount);
		assertEquals("removedEdgeCount", 2, removedEdgeCount);
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#getEdges()}.
	 */
	@Test
	public final void testGetEdges() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		testImpl.setPropertiedObjectSet(testPos);

		// Testing could be done by using graph methods but it seems better to
		// check via the PropertiedObjectSet
		ExampleObject testObject1 = new ExampleObject();
		ExampleObject testObject2 = new ExampleObject();
		
		testPos.addObject(testObject1);
		testPos.addObject(testObject2);
		
		PropertyKey testKey1 = createKey();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();

		testPos.setProperty(testObject1, testKey1, testValue1);
		testPos.setProperty(testObject1, testKey2, testValue2);
		testPos.setProperty(testObject2, testKey2, testValue2);
		testPos.setProperty(testObject2, testKey1, testValue2);
		
		// There should be three edges
		Set<PropertiedGraphEdge<ExampleObject>> edges = testImpl.getEdges();
		assertEquals("edges.size()", 3, edges.size());
		
		PropertiedGraphNode<ExampleObject> node1 = testImpl.getNode(testObject1);
		PropertiedGraphNode<ExampleObject> node2 = testImpl.getNode(testObject2);
		
		PropertiedGraphEdge<ExampleObject> edge1 = testImpl.getEdge(testKey1, testValue1);
		assertTrue("edges.contains(edge1)", edges.contains(edge1));
		assertFalse("edge1 == null", edge1 == null);
		assertEquals("edge1.getNodes().size()", 1, edge1.getNodes().size());
		assertTrue("edge1.getNodes().contains(node1)", edge1.getNodes().contains(node1));
		
		PropertiedGraphEdge<ExampleObject> edge2 = testImpl.getEdge(testKey1, testValue2);
		assertTrue("edges.contains(edge2)", edges.contains(edge2));
		assertFalse("edge2 == null", edge2 == null);
		assertEquals("edge2.getNodes().size()", 1, edge2.getNodes().size());
		assertTrue("edge2.getNodes().contains(node2)", edge2.getNodes().contains(node2));

		PropertiedGraphEdge<ExampleObject> edge3 = testImpl.getEdge(testKey2, testValue2);
		assertTrue("edges.contains(edge3)", edges.contains(edge3));
		assertFalse("edge3 == null", edge3 == null);
		assertEquals("edge3.getNodes().size()", 2, edge3.getNodes().size());
		assertTrue("edge3.getNodes().contains(node1)", edge3.getNodes().contains(node1));
		assertTrue("edge3.getNodes().contains(node2)", edge3.getNodes().contains(node2));
		
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#getNodes()}.
	 */
	@Test
	public final void testGetNodes() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		testImpl.setPropertiedObjectSet(testPos);

		ExampleObject testObject1 = new ExampleObject();
		ExampleObject testObject2 = new ExampleObject();
		
		testPos.addObject(testObject1);
		testPos.addObject(testObject2);
		
		PropertyKey testKey1 = createKey();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();

		testPos.setProperty(testObject1, testKey1, testValue1);	
		testPos.setProperty(testObject1, testKey2, testValue2);
		testPos.setProperty(testObject2, testKey2, testValue2);
		testPos.setProperty(testObject2, testKey1, testValue2);
		
		// There should be two nodes
		Set<PropertiedGraphNode<ExampleObject>> nodes = testImpl.getNodes();
		assertEquals("nodes.size()", 2, nodes.size());
		
		PropertiedGraphNode<ExampleObject> node1 = testImpl.getNode(testObject1);
		PropertiedGraphNode<ExampleObject> node2 = testImpl.getNode(testObject2);
		assertFalse("node1 == null", node1 == null);
		assertFalse("node2 == null", node2 == null);
		assertTrue("nodes.contains(node1)", nodes.contains(node1));
		assertTrue("nodes.contains(node2)", nodes.contains(node2));
		assertEquals("node1.getEdges().size()", 2, node1.getEdges().size());
		assertEquals("node2.getEdges().size()", 2, node2.getEdges().size());
		
		PropertiedGraphEdge<ExampleObject> edge1 = testImpl.getEdge(testKey1, testValue1);
		assertFalse("edge1 == null", edge1 == null);
		assertTrue("node1.getEdges().contains(edge1)", node1.getEdges().contains(edge1));
		
		PropertiedGraphEdge<ExampleObject> edge2 = testImpl.getEdge(testKey1, testValue2);
		assertFalse("edge2 == null", edge2 == null);
		assertTrue("node2.getEdges().contains(edge2)", node2.getEdges().contains(edge2));

		PropertiedGraphEdge<ExampleObject> edge3 = testImpl.getEdge(testKey2, testValue2);
		assertFalse("edge3 == null", edge3 == null);
		assertTrue("node1.getEdges().contains(edge3)", node1.getEdges().contains(edge3));
		assertTrue("node2.getEdges().contains(edge3)", node2.getEdges().contains(edge3));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#getPropertiedObjectSet()}.
	 */
	@Test
	public final void testGetPropertiedObjectSet() {
		assertNull ("testImpl.getPropertiedObjectSet()",
				testImpl.getPropertiedObjectSet());
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		testImpl.setPropertiedObjectSet(testPos);
		assertEquals("testImpl.getPropertiedObjectSet()", testPos,
				testImpl.getPropertiedObjectSet());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#objectAdded(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)}.
	 */
	@Test
	public final void testObjectAdded() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject = new ExampleObject();
		
		testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject);
		assertTrue ("testImpl.getNode(testObject) != null",
				testImpl.getNode(testObject) != null);
		assertEquals("testImpl.getNodes.size()", 1, testImpl.getNodes().size());
		
		// Check that adding twice does nothing
		testPos.addObject(testObject);
		assertTrue ("testImpl.getNode(testObject) != null",
				testImpl.getNode(testObject) != null);
		assertEquals("testImpl.getNodes.size()", 1, testImpl.getNodes().size());
		
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#objectRemoved(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)}.
	 */
	@Test
	public final void testObjectRemoved() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject = new ExampleObject();
		
		testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject);
		assertTrue ("testImpl.getNode(testObject) != null",
				testImpl.getNode(testObject) != null);
		assertEquals("testImpl.getNodes.size()", 1, testImpl.getNodes().size());
		
		testPos.removeObject(testObject);
		assertNull ("testImpl.getNode(testObject)",
				testImpl.getNode(testObject));
		assertEquals("testImpl.getNodes.size()", 0, testImpl.getNodes().size());
		
		// Check removing twice is OK
		testPos.removeObject(testObject);
		assertNull ("testImpl.getNode(testObject)",
				testImpl.getNode(testObject));
		assertEquals("testImpl.getNodes.size()", 0, testImpl.getNodes().size());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#propertyAdded(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public final void testPropertyAdded() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject = new ExampleObject();
		PropertyKey testKey = createKey();
		PropertyValue testValue = createValue();
		testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject);
		testPos.setProperty(testObject, testKey, testValue);

		PropertiedGraphEdge<ExampleObject> testEdge =
			testImpl.getEdge(testKey, testValue);
		PropertiedGraphNode<ExampleObject> testNode =
			testImpl.getNode(testObject);
		assertTrue(testNode != null);
		assertTrue(testEdge != null);
		assertTrue(testNode.getEdges() != null);
		assertEquals(1, testNode.getEdges().size());
		assertTrue(testNode.getEdges().contains(testEdge));
		assertTrue(testEdge.getNodes() != null);
		assertEquals(1, testEdge.getNodes().size());
		assertTrue(testEdge.getNodes().contains(testNode));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#propertyChanged(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public final void testPropertyChanged() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject = new ExampleObject();
		PropertyKey testKey = createKey();
		PropertyValue testOldValue = createValue();
		PropertyValue testNewValue = createValue();
		testImpl.setPropertiedObjectSet(testPos);

		testPos.addObject(testObject);
		testPos.setProperty(testObject, testKey, testOldValue);
		testPos.setProperty(testObject, testKey, testNewValue);

		PropertiedGraphEdge<ExampleObject> testOldEdge =
			testImpl.getEdge(testKey, testOldValue);
		PropertiedGraphEdge<ExampleObject> testNewEdge =
			testImpl.getEdge(testKey, testNewValue);
		PropertiedGraphNode<ExampleObject> testNode =
			testImpl.getNode(testObject);
		assertTrue(testNode != null);
		assertTrue(testOldEdge != null);
		assertTrue(testNewEdge != null);
		assertTrue(testNode.getEdges() != null);
		assertEquals(1, testNode.getEdges().size());
		assertTrue(testNode.getEdges().contains(testNewEdge));
		assertFalse(testNode.getEdges().contains(testOldEdge));
		assertTrue(testOldEdge.getNodes() != null);
		assertEquals(0, testOldEdge.getNodes().size());
		assertFalse(testOldEdge.getNodes().contains(testNode));
		assertTrue(testNewEdge.getNodes() != null);
		assertEquals(1, testNewEdge.getNodes().size());
		assertTrue(testNewEdge.getNodes().contains(testNode));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#propertyRemoved(java.lang.Object, net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public final void testPropertyRemoved() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject = new ExampleObject();
		PropertyKey testKey = createKey();
		PropertyValue testValue = createValue();
		testImpl.setPropertiedObjectSet(testPos);

		testPos.addObject(testObject);
		testPos.setProperty(testObject, testKey, testValue);
		testPos.removeProperty(testObject, testKey);

		PropertiedGraphEdge<ExampleObject> testEdge =
			testImpl.getEdge(testKey, testValue);
		PropertiedGraphNode<ExampleObject> testNode =
			testImpl.getNode(testObject);
		assertTrue(testNode != null);
		assertTrue(testEdge != null);
		assertTrue(testNode.getEdges() != null);
		assertEquals(0, testNode.getEdges().size());
		assertFalse(testNode.getEdges().contains(testEdge));
		assertTrue(testEdge.getNodes() != null);
		assertEquals(0, testEdge.getNodes().size());
		assertFalse(testEdge.getNodes().contains(testNode));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#removeListener(net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener)}.
	 */
	@Test
	public final void testRemoveListener() {
		try {
			testImpl.removeListener(null);
			fail("NullPointerException should be thrown for listener");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		testImpl.setPropertiedObjectSet(testPos);
		PropertiedGraphViewListener<ExampleObject> testListener = createListener();
		testImpl.addListener(testListener);

		ExampleObject testObject = new ExampleObject();
		
		// Should add a node
		testPos.addObject(testObject);
		
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		
		// Should add an edge
		testPos.setProperty(testObject, testKey, testValue1);
		
		testImpl.removeListener(testListener);
		
		// Should remove an edge and add a new one
		testPos.setProperty(testObject, testKey, testValue2);
		
		// Should remove a node and its edge
		testPos.removeObject(testObject);
		assertEquals("addedNodeCount", 1, addedNodeCount);
		assertEquals("removedNodeCount", 0, removedNodeCount);
		assertEquals("addedEdgeCount", 1, addedEdgeCount);
		assertEquals("removedEdgeCount", 0, removedEdgeCount);
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#setPropertiedObjectSet(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet)}.
	 */
	@Test
	public final void testSetPropertiedObjectSet() {
		try {
			testImpl.setPropertiedObjectSet(null);
			fail("NullPointerException should be thrown for PropertiedObjectSet");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject1 = new ExampleObject();
		PropertyKey testKey = createKey();
		PropertyValue testValue = createValue();
		ExampleObject testObject2 = new ExampleObject();
		testPos.addObject(testObject1);
		testPos.addObject(testObject2);
		testPos.setProperty(testObject1, testKey, testValue);
		testImpl.setPropertiedObjectSet(testPos);
		assertEquals(testPos, testImpl.getPropertiedObjectSet());
		assertEquals(2, testImpl.getNodes().size());
		assertEquals(1, testImpl.getEdges().size());
		PropertiedGraphNode testNode1 = testImpl.getNode(testObject1);
		PropertiedGraphNode testNode2 = testImpl.getNode(testObject2);
		assertFalse(testNode1 == null);
		assertFalse(testNode2 == null);
		PropertiedGraphEdge testEdge = testImpl.getEdge(testKey, testValue);
		assertFalse(testEdge == null);
		assertTrue(testNode1.getEdges().contains(testEdge));
		assertEquals(0, testNode2.getEdges().size());
		assertTrue(testEdge.getNodes().contains(testNode1));
		assertFalse(testEdge.getNodes().contains(testNode2));
	}
	
	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#getEdge(net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)}.
	 */
	@Test
	public final void testgetEdge() {
		PropertyKey testKey1 = createKey();
		PropertyValue testValue1 = createValue();
		PropertyKey testKey2 = createKey();
		PropertyValue testValue2 = createValue();
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject = new ExampleObject();
		try {
			testImpl.getEdge(null, null);
			fail("NullPointerException should be thrown for key or value");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			testImpl.getEdge(testKey1, null);
			fail("NullPointerException should be thrown for value");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			testImpl.getEdge(null, testValue1);
			fail("NullPointerException should be thrown for value");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		
		PropertiedGraphEdge<ExampleObject> testEdge = testImpl.getEdge(testKey1, testValue1);
		assertNull(testEdge);
		testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject);
		testPos.setProperty(testObject, testKey1, testValue1);
		testEdge = testImpl.getEdge(testKey1, testValue1);
		assertFalse(testEdge == null);
		
		assertNull(testImpl.getEdge(testKey2, testValue2));
		assertNull(testImpl.getEdge(testKey1, testValue2));
		assertNull(testImpl.getEdge(testKey2, testValue1));
		testPos.setProperty(testObject, testKey2, testValue2);
		assertFalse(testImpl.getEdge(testKey2, testValue2) == null);
	}
	
	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#getNode(java.lang.Object)}.
	 */
	@Test
	public final void testgetNode() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject1 = new ExampleObject();
		ExampleObject testObject2 = new ExampleObject();
		try {
			testImpl.getNode(null);
			fail("NullPointerException should be thrown for object");
		}
		catch (NullPointerException e) {
			// This is expected
		}
		
		testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject1);
		assertFalse(testImpl.getNode(testObject1) == null);
		assertNull(testImpl.getNode(testObject2));

	}
}
