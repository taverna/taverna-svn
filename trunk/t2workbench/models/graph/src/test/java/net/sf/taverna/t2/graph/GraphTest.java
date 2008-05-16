package net.sf.taverna.t2.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.graph.Edge;
import net.sf.taverna.t2.graph.Graph;
import net.sf.taverna.t2.graph.Node;
import net.sf.taverna.t2.graph.Graph.Alignment;

import org.junit.Before;
import org.junit.Test;

public class GraphTest {

	private Graph graph;
	
	private Alignment alignment;
	
	@Before
	public void setUp() throws Exception {
		alignment = Alignment.VERTICAL;
		graph = new Graph();
	}

	@Test
	public void testGraph() {
		assertNotNull(new Graph());
	}

	@Test
	public void testAddEdge() {
		Edge newEdge = new Edge();
		graph.addEdge(newEdge);
		assertEquals(1, graph.getEdges().size());
		assertTrue(graph.getEdges().contains(newEdge));
	}

	@Test
	public void testAddNode() {
		Node newNode = new Node();
		graph.addNode(newNode);
		assertEquals(1, graph.getNodes().size());
		assertTrue(graph.getNodes().contains(newNode));
		assertEquals(graph, newNode.getParent());
	}

	@Test
	public void testAddSubgraph() {
		Graph newGraph = new Graph();
		graph.addSubgraph(newGraph);
		assertEquals(1, graph.getSubgraphs().size());
		assertTrue(graph.getSubgraphs().contains(newGraph));
		assertEquals(graph, newGraph.getParent());
	}

	@Test
	public void testGetAlignment() {
		assertEquals(alignment, graph.getAlignment());
	}

	@Test
	public void testGetEdges() {
		assertNotNull(graph.getNodes());
		assertEquals(0, graph.getNodes().size());
	}

	@Test
	public void testGetNodes() {
		assertNotNull(graph.getEdges());
		assertEquals(0, graph.getEdges().size());
	}

	@Test
	public void testGetSubgraphs() {
		assertNotNull(graph.getSubgraphs());
		assertEquals(0, graph.getSubgraphs().size());
	}

	@Test
	public void testRemoveEdge() {
		Edge newEdge = new Edge();
		assertFalse(graph.removeEdge(newEdge));
		graph.addEdge(newEdge);
		assertTrue(graph.removeEdge(newEdge));
		assertFalse(graph.getNodes().contains(newEdge));
	}

	@Test
	public void testRemoveNode() {
		Node newNode = new Node();
		assertFalse(graph.removeNode(newNode));
		graph.addNode(newNode);
		assertTrue(graph.removeNode(newNode));
		assertFalse(graph.getNodes().contains(newNode));
	}

	@Test
	public void testRemoveSubgraph() {
		Graph newGraph = new Graph();
		assertFalse(graph.removeSubgraph(newGraph));
		graph.addSubgraph(newGraph);
		assertTrue(graph.removeSubgraph(newGraph));
		assertFalse(graph.getSubgraphs().contains(newGraph));
	}

	@Test
	public void testSetAlignment() {
		graph.setAlignment(Alignment.VERTICAL);
		assertEquals(Alignment.VERTICAL, graph.getAlignment());
		graph.setAlignment(Alignment.HORIZONTAL);
		assertEquals(Alignment.HORIZONTAL, graph.getAlignment());
	}

}
