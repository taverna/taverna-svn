package net.sf.taverna.t2.graph;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;

import net.sf.taverna.t2.graph.Element;
import net.sf.taverna.t2.graph.Node;
import net.sf.taverna.t2.graph.Graph.LineStyle;

import org.junit.Before;
import org.junit.Test;

public class ElementTest {
	
	private Element element;

	private String id;
	
	private String label;
	
	private LineStyle lineStyle;
	
	private Color color;
	
	private Color fillColor;
	
	private Element parent;

	@Before
	public void setUp() throws Exception {
		element = new Element() {};
		id = "element-id";
		label = "element-label";
		lineStyle = LineStyle.NONE;
		color = Color.BLUE;
		fillColor = Color.GREEN;
		parent = new Node();
		element.setId(id);
		element.setLabel(label);
		element.setLineStyle(lineStyle);
		element.setColor(color);
		element.setFillColor(fillColor);
		element.setParent(parent);
	}

	@Test
	public void testGetParent() {
		assertEquals(parent, element.getParent());
	}

	@Test
	public void testSetParent() {
		Node newParent = new Node();
		element.setParent(newParent);
		assertEquals(newParent, element.getParent());
		element.setParent(null);
		assertNull(element.getParent());
	}

	@Test
	public void testGetLabel() {
		assertEquals(label, element.getLabel());
	}

	@Test
	public void testSetLabel() {
		element.setLabel("new-label");
		assertEquals("new-label", element.getLabel());
		element.setLabel(null);
		assertNull(element.getLabel());
	}

	@Test
	public void testGetId() {
		assertEquals(id, element.getId());
	}

	@Test
	public void testSetId() {
		element.setId("new-id");
		assertEquals("new-id", element.getId());
		element.setId(null);
		assertNull(element.getId());
	}

	@Test
	public void testGetColor() {
		assertEquals(color, element.getColor());
	}

	@Test
	public void testSetColor() {
		element.setColor(Color.RED);
		assertEquals(Color.RED, element.getColor());
		element.setColor(null);
		assertNull(element.getColor());
	}

	@Test
	public void testGetFillColor() {
		assertEquals(fillColor, element.getFillColor());
	}

	@Test
	public void testSetFillColor() {
		element.setFillColor(Color.RED);
		assertEquals(Color.RED, element.getFillColor());
		element.setFillColor(null);
		assertNull(element.getFillColor());
	}

	@Test
	public void testGetLineStyle() {
		assertEquals(lineStyle, element.getLineStyle());
	}

	@Test
	public void testSetLineStyle() {
		element.setLineStyle(LineStyle.DOTTED);
		assertEquals(LineStyle.DOTTED, element.getLineStyle());
		element.setLineStyle(null);
		assertNull(element.getLineStyle());
	}

}
