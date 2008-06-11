package net.sf.taverna.t2.workbench.models.graph.svg;

import net.sf.taverna.t2.workbench.models.graph.GraphEdge;

import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;

/**
 * SVG representation of a graph edge.
 * 
 * @author David Withers
 */
public class SVGGraphEdge extends GraphEdge {

	private SVGGraphComponent graphComponent;

	private SVGOMPathElement path;

	private SVGOMPolygonElement polygon;

	private SVGOMEllipseElement ellipse;

	private String originalPathStyle;

	private String originalPolygonStyle;

	private String originalEllipseStyle;

	private String selectedPathStyle;

	private String selectedPolygonStyle;

	private String selectedEllipseStyle;

	public SVGGraphEdge() {
	}
		
	/**
	 * Returns the graphComponent.
	 *
	 * @return the graphComponent
	 */
	public SVGGraphComponent getGraphComponent() {
		return graphComponent;
	}

	/**
	 * Sets the graphComponent.
	 *
	 * @param graphComponent the new graphComponent
	 */
	public void setGraphComponent(SVGGraphComponent graphComponent) {
		this.graphComponent = graphComponent;
	}

	/**
	 * Returns the path.
	 *
	 * @return the path
	 */
	public SVGOMPathElement getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 *
	 * @param path the new path
	 */
	public void setPath(SVGOMPathElement path) {
		this.path = path;
		originalPathStyle = path.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		selectedPathStyle = originalPathStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.SELECTED_COLOUR + ";");

		EventTarget t = (EventTarget) path;		
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (evt instanceof MouseEvent) {
					MouseEvent mouseEvent = (MouseEvent) evt;
					getEventManager().mouseClicked(SVGGraphEdge.this, mouseEvent.getButton(),
							mouseEvent.getAltKey(), mouseEvent.getCtrlKey(), mouseEvent.getMetaKey(),
							mouseEvent.getScreenX(), mouseEvent.getScreenY());
				}
			}
		}, false);

	}

	/**
	 * Returns the polygon.
	 *
	 * @return the polygon
	 */
	public SVGOMPolygonElement getPolygon() {
		return polygon;
	}

	/**
	 * Sets the polygon.
	 *
	 * @param polygon the new polygon
	 */
	public void setPolygon(SVGOMPolygonElement polygon) {
		this.polygon = polygon;
		originalPolygonStyle = polygon.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		selectedPolygonStyle = originalPolygonStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.SELECTED_COLOUR + ";");
		selectedPolygonStyle = selectedPolygonStyle.replaceFirst("fill:[^;]*;", "fill:" + SVGGraphComponent.SELECTED_COLOUR + ";");

		EventTarget t = (EventTarget) polygon;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (evt instanceof MouseEvent) {
					MouseEvent mouseEvent = (MouseEvent) evt;
					getEventManager().mouseClicked(SVGGraphEdge.this, mouseEvent.getButton(),
							mouseEvent.getAltKey(), mouseEvent.getCtrlKey(), mouseEvent.getMetaKey(),
							mouseEvent.getScreenX(), mouseEvent.getScreenY());
				}
			}
		}, false);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGShape#getEllipse()
	 */
	public SVGOMEllipseElement getEllipse() {
		return ellipse;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGShape#setEllipse(org.apache.batik.dom.svg.SVGOMEllipseElement)
	 */
	public void setEllipse(SVGOMEllipseElement ellipse) {
		this.ellipse = ellipse;
		originalEllipseStyle = ellipse.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		selectedEllipseStyle = originalEllipseStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.SELECTED_COLOUR + ";");

		EventTarget t = (EventTarget) ellipse;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (evt instanceof MouseEvent) {
					MouseEvent mouseEvent = (MouseEvent) evt;
					getEventManager().mouseClicked(SVGGraphEdge.this, mouseEvent.getButton(),
							mouseEvent.getAltKey(), mouseEvent.getCtrlKey(), mouseEvent.getMetaKey(),
							mouseEvent.getScreenX(), mouseEvent.getScreenY());
				}
			}
		}, false);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.GraphElement#setSelected(boolean)
	 */
	public void setSelected(final boolean selected) {
		super.setSelected(selected);
		if (this.graphComponent.updateManager != null) {
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							if (selected) {
								path.setAttribute(
										SVGConstants.SVG_STYLE_ATTRIBUTE, selectedPathStyle);
								if (polygon != null) {
									polygon.setAttribute(
											SVGConstants.SVG_STYLE_ATTRIBUTE, selectedPolygonStyle);
								} else {
									ellipse.setAttribute(
											SVGConstants.SVG_STYLE_ATTRIBUTE, selectedEllipseStyle);
								}
							} else {
								path.setAttribute(
										SVGConstants.SVG_STYLE_ATTRIBUTE, originalPathStyle);
								if (polygon != null) {
									polygon.setAttribute(
											SVGConstants.SVG_STYLE_ATTRIBUTE, originalPolygonStyle);
								} else {
									ellipse.setAttribute(
											SVGConstants.SVG_STYLE_ATTRIBUTE, originalEllipseStyle);
								}
							}
						}
					});
		}
	}

	/**
	 * Set the SVG colour attribute for the edge.
	 * 
	 * @param colour
	 *            the new colour
	 */
	public void setColour(final String colour) {
		if (this.graphComponent.updateManager != null) {
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
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
		if (this.graphComponent.updateManager != null) {
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
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