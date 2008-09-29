package net.sf.taverna.t2.workbench.models.graph;

/**
 * An edge connecting two nodes in a graph.
 * 
 * @author David Withers
 */
public class GraphEdge extends GraphElement {

	public enum ArrowStyle {NONE, NORMAL, DOT, ODOT}
	
	private GraphNode source;
	
	private GraphNode sink;
	
	private ArrowStyle arrowHeadStyle = ArrowStyle.NORMAL;

	private ArrowStyle arrowTailStyle = ArrowStyle.NONE;
	
	private boolean active;

	/**
	 * Constructs a new instance of Edge.
	 *
	 */
	public GraphEdge(GraphEventManager eventManager) {
		super(eventManager);
	}
	
	/**
	 * Returns the source.
	 *
	 * @return the source
	 */
	public GraphNode getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(GraphNode source) {
		this.source = source;
	}

	/**
	 * Returns the sink.
	 *
	 * @return the sink
	 */
	public GraphNode getSink() {
		return sink;
	}

	/**
	 * Sets the sink.
	 *
	 * @param sink the new sink
	 */
	public void setSink(GraphNode sink) {
		this.sink = sink;
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

	/**
	 * Sets the active state of the Edge.
	 * 
	 * @param active the active state of the Edge
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
}
