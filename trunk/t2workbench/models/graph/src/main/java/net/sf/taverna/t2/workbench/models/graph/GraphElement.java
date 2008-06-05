package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Color;

import net.sf.taverna.t2.workbench.models.graph.Graph.LineStyle;

/**
 * An element of a graph.
 * 
 * @author David Withers
 */
public abstract class GraphElement {

	private String id;
	
	private String label;
	
	private LineStyle lineStyle;
	
	private Color color;
	
	private Color fillColor;
	
	private GraphElement parent;

	/**
	 * Returns the parent.
	 *
	 * @return the parent
	 */
	public GraphElement getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	protected void setParent(GraphElement parent) {
		this.parent = parent;
	}

	/**
	 * Returns the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the colour.
	 *
	 * @return the colour
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the colour.
	 *
	 * @param color the new colour
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Returns the fillColor.
	 *
	 * @return the fillColor
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Sets the fillColor.
	 *
	 * @param fillColor the new fillColor
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * Returns the lineStyle.
	 *
	 * @return the lineStyle
	 */
	public LineStyle getLineStyle() {
		return lineStyle;
	}

	/**
	 * Sets the lineStyle.
	 *
	 * @param lineStyle the new lineStyle
	 */
	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	public String toString() {
		return id + "[" + label + "]";
	}
}