package net.sf.taverna.t2.workbench.models.graph;

import static org.junit.Assert.*;

import java.awt.Color;

import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge.ArrowStyle;
import net.sf.taverna.t2.workbench.models.graph.Graph.LineStyle;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionModelImpl;

import org.junit.Before;
import org.junit.Test;

public class GraphEdgeTest {

	private GraphEdge edge;
	
	private GraphNode source;
	
	private GraphNode sink;
	
	private ArrowStyle arrowHeadStyle;

	private ArrowStyle arrowTailStyle;
	
	@Before
	public void setUp() throws Exception {
		source = new GraphNode(null);
		sink = new GraphNode(null);
		arrowHeadStyle = ArrowStyle.ODOT;
		arrowTailStyle = ArrowStyle.NORMAL;
		edge = new GraphEdge(null);
		edge.setArrowHeadStyle(arrowHeadStyle);
		edge.setArrowTailStyle(arrowTailStyle);
		edge.setSink(sink);
		edge.setSource(source);
	}

	@Test
	public void testEdge() {
		edge = new GraphEdge(null);
		assertNull(edge.getSource());
		assertNull(edge.getSink());
		assertNull(edge.getLabel());
	}

	@Test
	public void testEdgeNodeNode() {
		edge = new GraphEdge(null);
		edge.setSource(source);
		edge.setSink(sink);
		assertEquals(source, edge.getSource());
		assertEquals(sink, edge.getSink());
		assertNull(edge.getLabel());
	}

	@Test
	public void testGetSource() {
		assertEquals(source, edge.getSource());
	}

	@Test
	public void testSetSource() {
		GraphNode node = new GraphNode(null);
		edge.setSource(node);
		assertEquals(node, edge.getSource());
		edge.setSource(null);
		assertNull(edge.getSource());
	}

	@Test
	public void testGetSink() {
		assertEquals(sink, edge.getSink());
	}

	@Test
	public void testSetSink() {
		GraphNode node = new GraphNode(null);
		edge.setSink(node);
		assertEquals(node, edge.getSink());
		edge.setSink(null);
		assertNull(edge.getSink());
	}

	@Test
	public void testGetArrowHeadStyle() {
		assertEquals(arrowHeadStyle, edge.getArrowHeadStyle());
	}

	@Test
	public void testSetArrowHeadStyle() {
		edge.setArrowHeadStyle(ArrowStyle.DOT);
		assertEquals(ArrowStyle.DOT, edge.getArrowHeadStyle());
		edge.setArrowHeadStyle(null);
		assertNull(edge.getArrowHeadStyle());
	}

	@Test
	public void testGetArrowTailStyle() {
		assertEquals(arrowTailStyle, edge.getArrowTailStyle());
	}

	@Test
	public void testSetArrowTailStyle() {
		edge.setArrowTailStyle(ArrowStyle.NORMAL);
		assertEquals(ArrowStyle.NORMAL, edge.getArrowTailStyle());
		edge.setArrowTailStyle(null);
		assertNull(edge.getArrowTailStyle());
	}

}
