package net.sf.taverna.t2.graph;

import static org.junit.Assert.*;

import java.awt.Color;

import net.sf.taverna.t2.graph.Edge;
import net.sf.taverna.t2.graph.Node;
import net.sf.taverna.t2.graph.Edge.ArrowStyle;
import net.sf.taverna.t2.graph.Graph.LineStyle;

import org.junit.Before;
import org.junit.Test;

public class EdgeTest {

	private Edge edge;
	
	private String label;
	
	private Node source;
	
	private Node sink;
	
	private LineStyle lineStyle;
	
	private Color color;
	
	private ArrowStyle arrowHeadStyle;

	private ArrowStyle arrowTailStyle;
	
	@Before
	public void setUp() throws Exception {
		source = new Node();
		sink = new Node();
		label = "edge-label";
		lineStyle = LineStyle.NONE;
		color = Color.BLUE;
		arrowHeadStyle = ArrowStyle.ODOT;
		arrowTailStyle = ArrowStyle.NORMAL;
		edge = new Edge();
		edge.setArrowHeadStyle(arrowHeadStyle);
		edge.setArrowTailStyle(arrowTailStyle);
		edge.setColor(color);
		edge.setLabel(label);
		edge.setLineStyle(lineStyle);
		edge.setSink(sink);
		edge.setSource(source);
	}

	@Test
	public void testEdge() {
		edge = new Edge();
		assertNull(edge.getSource());
		assertNull(edge.getSink());
		assertNull(edge.getLabel());
	}

	@Test
	public void testEdgeNodeNode() {
		edge = new Edge(source, sink);
		assertEquals(source, edge.getSource());
		assertEquals(sink, edge.getSink());
		assertNull(edge.getLabel());
	}

	@Test
	public void testGetLabel() {
		assertEquals(label, edge.getLabel());
	}

	@Test
	public void testSetLabel() {
		edge.setLabel("new-label");
		assertEquals("new-label", edge.getLabel());
		edge.setLabel(null);
		assertNull(edge.getLabel());
	}

	@Test
	public void testGetSource() {
		assertEquals(source, edge.getSource());
	}

	@Test
	public void testSetSource() {
		Node node = new Node();
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
		Node node = new Node();
		edge.setSink(node);
		assertEquals(node, edge.getSink());
		edge.setSink(null);
		assertNull(edge.getSink());
	}

	@Test
	public void testGetLineStyle() {
		assertEquals(lineStyle, edge.getLineStyle());
	}

	@Test
	public void testSetLineStyle() {
		edge.setLineStyle(LineStyle.DOTTED);
		assertEquals(LineStyle.DOTTED, edge.getLineStyle());
		edge.setLineStyle(null);
		assertNull(edge.getLineStyle());
	}

	@Test
	public void testGetColor() {
		assertEquals(color, edge.getColor());
	}

	@Test
	public void testSetColor() {
		edge.setColor(Color.RED);
		assertEquals(Color.RED, edge.getColor());
		edge.setColor(null);
		assertNull(edge.getColor());
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
