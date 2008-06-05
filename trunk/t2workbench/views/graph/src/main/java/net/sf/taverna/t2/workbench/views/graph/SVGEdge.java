package net.sf.taverna.t2.workbench.views.graph;

import net.sf.taverna.t2.workbench.models.graph.GraphElement;

import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.UIEvent;

/**
 * SVG representation of a graph edge.
 * 
 * @author David Withers
 */
public class SVGEdge extends SVGElement {

	private final SVGGraph graph;

	private GraphElement graphElement;
	
	private SVGOMPathElement path;

	private SVGOMPolygonElement polygon;

	private String originalPathStyle;

	private String originalPolygonStyle;

	private String selectedPathStyle;

	private String selectedPolygonStyle;

	/**
	 * Constructs a new instance of an SVGEdge.
	 * 
	 * @param path
	 *            the path element
	 * @param polygon
	 *            the polygon element
	 * @param graph TODO
	 */
	public SVGEdge(SVGGraph graph, GraphElement graphElement, SVGOMPathElement path, SVGOMPolygonElement polygon) {
		this.graph = graph;
		this.graphElement = graphElement;
		this.path = path;
		this.polygon = polygon;
		originalPathStyle = path
				.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		originalPolygonStyle = polygon
				.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		selectedPathStyle = originalPathStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraph.SELECTED_COLOUR + ";");
		selectedPolygonStyle = originalPolygonStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraph.SELECTED_COLOUR + ";");

		EventTarget t = (EventTarget) path;
		t.addEventListener(SVGConstants.SVG_DOMACTIVATE_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (evt instanceof UIEvent) {
					UIEvent uiEvent = (UIEvent) evt;
					if (uiEvent.getDetail() == 1) {
						SVGEdge.this.graph.graphController.setSelected(SVGEdge.this.graphElement, true);
					} 
				}
			}
		}, false);
		
		t = (EventTarget) polygon;
		t.addEventListener(SVGConstants.SVG_DOMACTIVATE_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (evt instanceof UIEvent) {
					UIEvent uiEvent = (UIEvent) evt;
					if (uiEvent.getDetail() == 1) {
						SVGEdge.this.graph.graphController.setSelected(SVGEdge.this.graphElement, true);
					} 
				}
			}
		}, false);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.views.graph.SVGElement#setSelected(boolean)
	 */
	public void setSelected(boolean selected) {
		if (selected) {
			path.setAttribute(
					SVGConstants.SVG_STYLE_ATTRIBUTE, selectedPathStyle);
			polygon.setAttribute(
					SVGConstants.SVG_STYLE_ATTRIBUTE, selectedPolygonStyle);
		} else {
			path.setAttribute(
					SVGConstants.SVG_STYLE_ATTRIBUTE, originalPathStyle);			
			polygon.setAttribute(
					SVGConstants.SVG_STYLE_ATTRIBUTE, originalPolygonStyle);			
		}
	}

	/**
	 * Set the SVG colour attribute for the edge.
	 * 
	 * @param colour
	 *            the new colour
	 */
	public void setColour(final String colour) {
		if (this.graph.updateManager != null) {
			this.graph.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							path.setAttribute(
									SVGConstants.SVG_STYLE_ATTRIBUTE,
									"fill:none;stroke:" + colour + ";");
							polygon.setAttribute(
									SVGConstants.SVG_STYLE_ATTRIBUTE,
									"fill:" + colour + ";stroke:" + colour
											+ ";");
						}
					});
		}
	}

	/**
	 * Resets the SVG style attributes of the edge to their original
	 * values.
	 * 
	 */
	public void resetStyle() {
		if (this.graph.updateManager != null) {
			this.graph.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							path.setAttribute(
									SVGConstants.SVG_STYLE_ATTRIBUTE,
									originalPathStyle);
							polygon.setAttribute(
									SVGConstants.SVG_STYLE_ATTRIBUTE,
									originalPolygonStyle);
						}
					});
		}
	}

}