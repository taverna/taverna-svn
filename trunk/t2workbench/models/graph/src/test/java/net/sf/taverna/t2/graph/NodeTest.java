package net.sf.taverna.t2.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.graph.Graph;
import net.sf.taverna.t2.graph.Node;
import net.sf.taverna.t2.graph.Node.Shape;

import org.junit.Before;
import org.junit.Test;

public class NodeTest {

	private Node node;
	
	private Shape shape;
		
	private float width;
	
	private float height;
	
	private Graph graph;
	
	private boolean expanded;
	
	@Before
	public void setUp() throws Exception {
		shape = Shape.HOUSE;
		width = 0.5f;
		height = 1.5f;
		graph = new Graph();
		expanded = false;
		node = new Node();
		node.setShape(shape);
		node.setWidth(width);
		node.setHeight(height);
		node.setGraph(graph);
		node.setExpanded(expanded);
	}

	@Test
	public void testNode() {
		assertNotNull(new Node());
	}

	@Test
	public void testAddSinkNode() {
		Node newNode = new Node();
		node.addSinkNode(newNode);
		assertEquals(1, node.getSinkNodes().size());
		assertTrue(node.getSinkNodes().contains(newNode));
		assertEquals(node, newNode.getParent());
	}

	@Test
	public void testAddSourceNode() {
		Node newNode = new Node();
		node.addSourceNode(newNode);
		assertEquals(1, node.getSourceNodes().size());
		assertTrue(node.getSourceNodes().contains(newNode));
		assertEquals(node, newNode.getParent());
	}

	@Test
	public void testGetGraph() {
		assertEquals(graph, node.getGraph());
	}

	@Test
	public void testGetHeight() {
		assertEquals(height, node.getHeight(), 0);
	}

	@Test
	public void testGetShape() {
		assertEquals(shape, node.getShape());
	}

	@Test
	public void testGetSinkNodes() {
		assertNotNull(node.getSinkNodes());
		assertEquals(0, node.getSinkNodes().size());
	}

	@Test
	public void testGetSourceNodes() {
		assertNotNull(node.getSourceNodes());
		assertEquals(0, node.getSourceNodes().size());
	}

	@Test
	public void testGetWidth() {
		assertEquals(width, node.getWidth(), 0);
	}

	@Test
	public void testIsExpanded() {
		assertEquals(expanded, node.isExpanded());
	}

	@Test
	public void testRemoveSinkNode() {
		Node newNode = new Node();
		assertFalse(node.removeSinkNode(newNode));
		node.addSinkNode(newNode);
		assertTrue(node.removeSinkNode(newNode));
		assertFalse(node.getSinkNodes().contains(newNode));
	}

	@Test
	public void testRemoveSourceNode() {
		Node newNode = new Node();
		assertFalse(node.removeSourceNode(newNode));
		node.addSourceNode(newNode);
		assertTrue(node.removeSourceNode(newNode));
		assertFalse(node.getSourceNodes().contains(newNode));
	}

	@Test
	public void testSetExpanded() {
		node.setExpanded(true);
		assertEquals(true, node.isExpanded());
		node.setExpanded(false);
		assertEquals(false, node.isExpanded());
	}

	@Test
	public void testSetGraph() {
		Graph newGraph = new Graph();
		node.setGraph(newGraph);
		assertEquals(newGraph, node.getGraph());
		node.setGraph(null);
		assertNull(node.getGraph());
	}

	@Test
	public void testSetHeight() {
		node.setHeight(5.7f);
		assertEquals(5.7f, node.getHeight(), 0);
		node.setHeight(4.3f);
		assertEquals(4.3f, node.getHeight(), 0);
	}

	@Test
	public void testSetShape() {
		node.setShape(Shape.INVTRIANGLE);
		assertEquals(Shape.INVTRIANGLE, node.getShape());
		node.setShape(Shape.TRIANGLE);
		assertEquals(Shape.TRIANGLE, node.getShape());
	}

	@Test
	public void testSetWidth() {
		node.setWidth(0f);
		assertEquals(0f, node.getWidth(), 0);
		node.setWidth(-4.3f);
		assertEquals(-4.3f, node.getWidth(), 0);
	}

}
