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

	int addedNodeCount;

	int removedNodeCount;

	int addedEdgeCount;

	int removedEdgeCount;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Nopthing to be done
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Nopthing to be done
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.addedNodeCount = 0;
		this.removedNodeCount = 0;
		this.addedEdgeCount = 0;
		this.removedEdgeCount = 0;
		this.testImpl = new PropertiedGraphViewImpl<ExampleObject>();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// Nopthing to be done
	}

	private PropertiedGraphViewListener<ExampleObject> createListener() {
		return new PropertiedGraphViewListener<ExampleObject>() {
			public void edgeAdded(PropertiedGraphView<ExampleObject> view,
					PropertiedGraphEdge<ExampleObject> edge, PropertiedGraphNode<ExampleObject> node) {
				PropertiedGraphViewImplTest.this.addedEdgeCount++;
			}

			public void edgeRemoved(PropertiedGraphView<ExampleObject> view,
					PropertiedGraphEdge<ExampleObject> edge, PropertiedGraphNode<ExampleObject> node) {
				PropertiedGraphViewImplTest.this.removedEdgeCount++;
			}

			public void nodeAdded(PropertiedGraphView<ExampleObject> view,
					PropertiedGraphNode<ExampleObject> node) {
				PropertiedGraphViewImplTest.this.addedNodeCount++;
			}

			public void nodeRemoved(PropertiedGraphView<ExampleObject> view,
					PropertiedGraphNode<ExampleObject> node) {
				PropertiedGraphViewImplTest.this.removedNodeCount++;
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
		assertEquals("testImpl.getEdges().size()", 0, this.testImpl.getEdges() //$NON-NLS-1$
				.size());
		assertEquals("testImpl.getNodes().size()", 0, this.testImpl.getNodes() //$NON-NLS-1$
				.size());
		assertNull("testImpl.getPropertiedObjectSet()", this.testImpl //$NON-NLS-1$
				.getPropertiedObjectSet());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#addListener(net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener)}.
	 */
	@Test
	public final void testAddListener() {
		try {
			this.testImpl.addListener(null);
			fail("NullPointerException should be thrown for listener"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		this.testImpl.setPropertiedObjectSet(testPos);
		PropertiedGraphViewListener<ExampleObject> testListener = createListener();
		this.testImpl.addListener(testListener);

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
		assertEquals("addedNodeCount", 1, this.addedNodeCount); //$NON-NLS-1$
		assertEquals("removedNodeCount", 1, this.removedNodeCount); //$NON-NLS-1$
		assertEquals("addedEdgeCount", 2, this.addedEdgeCount); //$NON-NLS-1$
		assertEquals("removedEdgeCount", 2, this.removedEdgeCount); //$NON-NLS-1$
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#getEdges()}.
	 */
	@Test
	public final void testGetEdges() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		this.testImpl.setPropertiedObjectSet(testPos);

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
		Set<PropertiedGraphEdge<ExampleObject>> edges = this.testImpl.getEdges();
		assertEquals("edges.size()", 3, edges.size()); //$NON-NLS-1$
		
		PropertiedGraphNode<ExampleObject> node1 = this.testImpl.getNode(testObject1);
		PropertiedGraphNode<ExampleObject> node2 = this.testImpl.getNode(testObject2);
		
		PropertiedGraphEdge<ExampleObject> edge1 = this.testImpl.getEdge(testKey1, testValue1);
		assertTrue("edges.contains(edge1)", edges.contains(edge1)); //$NON-NLS-1$
		assertNotNull(edge1);
		assertEquals("edge1.getNodes().size()", 1, edge1.getNodes().size()); //$NON-NLS-1$
		assertTrue("edge1.getNodes().contains(node1)", edge1.getNodes().contains(node1)); //$NON-NLS-1$
		
		PropertiedGraphEdge<ExampleObject> edge2 = this.testImpl.getEdge(testKey1, testValue2);
		assertTrue("edges.contains(edge2)", edges.contains(edge2)); //$NON-NLS-1$
		assertNotNull(edge2);
		assertEquals("edge2.getNodes().size()", 1, edge2.getNodes().size()); //$NON-NLS-1$
		assertTrue("edge2.getNodes().contains(node2)", edge2.getNodes().contains(node2)); //$NON-NLS-1$

		PropertiedGraphEdge<ExampleObject> edge3 = this.testImpl.getEdge(testKey2, testValue2);
		assertTrue("edges.contains(edge3)", edges.contains(edge3)); //$NON-NLS-1$
		assertNotNull(edge3);
		assertEquals("edge3.getNodes().size()", 2, edge3.getNodes().size()); //$NON-NLS-1$
		assertTrue("edge3.getNodes().contains(node1)", edge3.getNodes().contains(node1)); //$NON-NLS-1$
		assertTrue("edge3.getNodes().contains(node2)", edge3.getNodes().contains(node2)); //$NON-NLS-1$
		
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#getNodes()}.
	 */
	@Test
	public final void testGetNodes() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		this.testImpl.setPropertiedObjectSet(testPos);

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
		Set<PropertiedGraphNode<ExampleObject>> nodes = this.testImpl.getNodes();
		assertEquals("nodes.size()", 2, nodes.size()); //$NON-NLS-1$
		
		PropertiedGraphNode<ExampleObject> node1 = this.testImpl.getNode(testObject1);
		PropertiedGraphNode<ExampleObject> node2 = this.testImpl.getNode(testObject2);
		assertNotNull(node1);
		assertNotNull(node2);
		assertTrue("nodes.contains(node1)", nodes.contains(node1)); //$NON-NLS-1$
		assertTrue("nodes.contains(node2)", nodes.contains(node2)); //$NON-NLS-1$
		assertEquals("node1.getEdges().size()", 2, node1.getEdges().size()); //$NON-NLS-1$
		assertEquals("node2.getEdges().size()", 2, node2.getEdges().size()); //$NON-NLS-1$
		
		PropertiedGraphEdge<ExampleObject> edge1 = this.testImpl.getEdge(testKey1, testValue1);
		assertNotNull(edge1);
		assertTrue("node1.getEdges().contains(edge1)", node1.getEdges().contains(edge1)); //$NON-NLS-1$
		
		PropertiedGraphEdge<ExampleObject> edge2 = this.testImpl.getEdge(testKey1, testValue2);
		assertNotNull(edge2);
		assertTrue("node2.getEdges().contains(edge2)", node2.getEdges().contains(edge2)); //$NON-NLS-1$

		PropertiedGraphEdge<ExampleObject> edge3 = this.testImpl.getEdge(testKey2, testValue2);
		assertNotNull(edge3);
		assertTrue("node1.getEdges().contains(edge3)", node1.getEdges().contains(edge3)); //$NON-NLS-1$
		assertTrue("node2.getEdges().contains(edge3)", node2.getEdges().contains(edge3)); //$NON-NLS-1$
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#getPropertiedObjectSet()}.
	 */
	@Test
	public final void testGetPropertiedObjectSet() {
		assertNull ("testImpl.getPropertiedObjectSet()", //$NON-NLS-1$
				this.testImpl.getPropertiedObjectSet());
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		this.testImpl.setPropertiedObjectSet(testPos);
		assertEquals("this.testImpl.getPropertiedObjectSet()", testPos, //$NON-NLS-1$
				this.testImpl.getPropertiedObjectSet());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#objectAdded(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)}.
	 */
	@Test
	public final void testObjectAdded() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject = new ExampleObject();
		
		this.testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject);
		assertTrue ("testImpl.getNode(testObject) != null", //$NON-NLS-1$
				this.testImpl.getNode(testObject) != null);
		assertEquals("testImpl.getNodes.size()", 1, this.testImpl.getNodes().size()); //$NON-NLS-1$
		
		// Check that adding twice does nothing
		testPos.addObject(testObject);
		assertTrue ("testImpl.getNode(testObject) != null", //$NON-NLS-1$
				this.testImpl.getNode(testObject) != null);
		assertEquals("testImpl.getNodes.size()", 1, this.testImpl.getNodes().size()); //$NON-NLS-1$
		
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#objectRemoved(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)}.
	 */
	@Test
	public final void testObjectRemoved() {
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		ExampleObject testObject = new ExampleObject();
		
		this.testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject);
		assertTrue ("testImpl.getNode(testObject) != null", //$NON-NLS-1$
				this.testImpl.getNode(testObject) != null);
		assertEquals("testImpl.getNodes.size()", 1, this.testImpl.getNodes().size()); //$NON-NLS-1$
		
		testPos.removeObject(testObject);
		assertNull ("testImpl.getNode(testObject)", //$NON-NLS-1$
				this.testImpl.getNode(testObject));
		assertEquals("testImpl.getNodes.size()", 0, this.testImpl.getNodes().size()); //$NON-NLS-1$
		
		// Check removing twice is OK
		testPos.removeObject(testObject);
		assertNull ("testImpl.getNode(testObject)", //$NON-NLS-1$
				this.testImpl.getNode(testObject));
		assertEquals("testImpl.getNodes.size()", 0, this.testImpl.getNodes().size()); //$NON-NLS-1$
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
		this.testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject);
		testPos.setProperty(testObject, testKey, testValue);

		PropertiedGraphEdge<ExampleObject> testEdge =
			this.testImpl.getEdge(testKey, testValue);
		PropertiedGraphNode<ExampleObject> testNode =
			this.testImpl.getNode(testObject);
		assertNotNull(testNode);
		assertNotNull(testEdge);
		assertNotNull(testNode.getEdges());
		assertEquals(1, testNode.getEdges().size());
		assertTrue(testNode.getEdges().contains(testEdge));
		assertNotNull(testEdge.getNodes());
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
		this.testImpl.setPropertiedObjectSet(testPos);

		testPos.addObject(testObject);
		testPos.setProperty(testObject, testKey, testOldValue);
		testPos.setProperty(testObject, testKey, testNewValue);

		PropertiedGraphEdge<ExampleObject> testOldEdge =
			this.testImpl.getEdge(testKey, testOldValue);
		PropertiedGraphEdge<ExampleObject> testNewEdge =
			this.testImpl.getEdge(testKey, testNewValue);
		PropertiedGraphNode<ExampleObject> testNode =
			this.testImpl.getNode(testObject);
		assertNotNull(testNode);
		assertNotNull(testOldEdge);
		assertNotNull(testNewEdge);
		assertNotNull(testNode.getEdges());
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
		this.testImpl.setPropertiedObjectSet(testPos);

		testPos.addObject(testObject);
		testPos.setProperty(testObject, testKey, testValue);
		testPos.removeProperty(testObject, testKey);

		PropertiedGraphEdge<ExampleObject> testEdge =
			this.testImpl.getEdge(testKey, testValue);
		PropertiedGraphNode<ExampleObject> testNode =
			this.testImpl.getNode(testObject);
		assertNotNull(testNode);
		assertNotNull(testEdge);
		assertNotNull(testNode.getEdges());
		assertEquals(0, testNode.getEdges().size());
		assertFalse(testNode.getEdges().contains(testEdge));
		assertNotNull(testEdge.getNodes());
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
			this.testImpl.removeListener(null);
			fail("NullPointerException should be thrown for listener"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		PropertiedObjectSet<ExampleObject> testPos = createPos();
		this.testImpl.setPropertiedObjectSet(testPos);
		PropertiedGraphViewListener<ExampleObject> testListener = createListener();
		this.testImpl.addListener(testListener);

		ExampleObject testObject = new ExampleObject();
		
		// Should add a node
		testPos.addObject(testObject);
		
		PropertyKey testKey = createKey();
		PropertyValue testValue1 = createValue();
		PropertyValue testValue2 = createValue();
		
		// Should add an edge
		testPos.setProperty(testObject, testKey, testValue1);
		
		this.testImpl.removeListener(testListener);
		
		// Should remove an edge and add a new one
		testPos.setProperty(testObject, testKey, testValue2);
		
		// Should remove a node and its edge
		testPos.removeObject(testObject);
		assertEquals("addedNodeCount", 1, this.addedNodeCount); //$NON-NLS-1$
		assertEquals("removedNodeCount", 0, this.removedNodeCount); //$NON-NLS-1$
		assertEquals("addedEdgeCount", 1, this.addedEdgeCount); //$NON-NLS-1$
		assertEquals("removedEdgeCount", 0, this.removedEdgeCount); //$NON-NLS-1$
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedGraphViewImpl#setPropertiedObjectSet(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet)}.
	 */
	@Test
	public final void testSetPropertiedObjectSet() {
		try {
			this.testImpl.setPropertiedObjectSet(null);
			fail("NullPointerException should be thrown for PropertiedObjectSet"); //$NON-NLS-1$
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
		this.testImpl.setPropertiedObjectSet(testPos);
		assertEquals(testPos, this.testImpl.getPropertiedObjectSet());
		assertEquals(2, this.testImpl.getNodes().size());
		assertEquals(1, this.testImpl.getEdges().size());
		PropertiedGraphNode<ExampleObject> testNode1 = this.testImpl.getNode(testObject1);
		PropertiedGraphNode<ExampleObject> testNode2 = this.testImpl.getNode(testObject2);
		assertNotNull(testNode1);
		assertNotNull(testNode2);
		PropertiedGraphEdge<ExampleObject> testEdge = this.testImpl.getEdge(testKey, testValue);
		assertNotNull(testEdge);
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
			this.testImpl.getEdge(null, null);
			fail("NullPointerException should be thrown for key or value"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			this.testImpl.getEdge(testKey1, null);
			fail("NullPointerException should be thrown for value"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		try {
			this.testImpl.getEdge(null, testValue1);
			fail("NullPointerException should be thrown for value"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		
		PropertiedGraphEdge<ExampleObject> testEdge = this.testImpl.getEdge(testKey1, testValue1);
		assertNull(testEdge);
		this.testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject);
		testPos.setProperty(testObject, testKey1, testValue1);
		testEdge = this.testImpl.getEdge(testKey1, testValue1);
		assertFalse(testEdge == null);
		
		assertNull(this.testImpl.getEdge(testKey2, testValue2));
		assertNull(this.testImpl.getEdge(testKey1, testValue2));
		assertNull(this.testImpl.getEdge(testKey2, testValue1));
		testPos.setProperty(testObject, testKey2, testValue2);
		assertFalse(this.testImpl.getEdge(testKey2, testValue2) == null);
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
			this.testImpl.getNode(null);
			fail("NullPointerException should be thrown for object"); //$NON-NLS-1$
		}
		catch (NullPointerException e) {
			// This is expected
		}
		
		this.testImpl.setPropertiedObjectSet(testPos);
		testPos.addObject(testObject1);
		assertFalse(this.testImpl.getNode(testObject1) == null);
		assertNull(this.testImpl.getNode(testObject2));

	}
}
