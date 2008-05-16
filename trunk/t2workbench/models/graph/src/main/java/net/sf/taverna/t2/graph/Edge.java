package net.sf.taverna.t2.graph;

import java.awt.Color;

import net.sf.taverna.t2.graph.Graph.LineStyle;

/**
 * An edge connecting two nodes in a graph.
 * 
 * @author David Withers
 */
public class Edge {

	public enum ArrowStyle {NONE, NORMAL, DOT, ODOT}
	
	private String label;
	
	private Node source;
	
	private Node sink;
	
	private LineStyle lineStyle;
	
	private Color color;
	
	private ArrowStyle arrowHeadStyle = ArrowStyle.NORMAL;

	private ArrowStyle arrowTailStyle = ArrowStyle.NONE;

	/**
	 * Constructs a new instance of Edge.
	 *
	 */
	public Edge() {
	}
	
	/**
	 * Constructs a new instance of Edge.
	 *
	 * @param source
	 * @param sink
	 */
	public Edge(Node source, Node sink) {
		this.source = source;
		this.sink = sink;
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
	 * Returns the source.
	 *
	 * @return the source
	 */
	public Node getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(Node source) {
		this.source = source;
	}

	/**
	 * Returns the sink.
	 *
	 * @return the sink
	 */
	public Node getSink() {
		return sink;
	}

	/**
	 * Sets the sink.
	 *
	 * @param sink the new sink
	 */
	public void setSink(Node sink) {
		this.sink = sink;
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

	/**
	 * Returns the color.
	 *
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color.
	 *
	 * @param color the new color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Returns the arrowHeadStyle.
	 *
	 * @return the arrowHeadStyle
	 */
	public ArrowStyle getArrowHeadStyle() {
		return arrowHeadStyle;
	}

	/**
	 * Sets the arrowHeadStyle.
	 *
	 * @param arrowHeadStyle the new arrowHeadStyle
	 */
	public void setArrowHeadStyle(ArrowStyle arrowHeadStyle) {
		this.arrowHeadStyle = arrowHeadStyle;
	}

	/**
	 * Returns the arrowTailStyle.
	 *
	 * @return the arrowTailStyle
	 */
	public ArrowStyle getArrowTailStyle() {
		return arrowTailStyle;
	}

	/**
	 * Sets the arrowTailStyle.
	 *
	 * @param arrowTailStyle the new arrowTailStyle
	 */
	public void setArrowTailStyle(ArrowStyle arrowTailStyle) {
		this.arrowTailStyle = arrowTailStyle;
	}
	
}
