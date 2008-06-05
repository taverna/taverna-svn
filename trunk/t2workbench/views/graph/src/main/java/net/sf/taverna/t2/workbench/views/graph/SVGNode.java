package net.sf.taverna.t2.workbench.views.graph;

import net.sf.taverna.t2.workbench.models.graph.GraphElement;

import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.UIEvent;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

/**
 * SVG representation of a graph node.
 * 
 * @author David Withers
 */
public class SVGNode extends SVGElement {

	private SVGGraph graph;

	private GraphElement graphElement;

	private SVGOMGElement g;

	private SVGOMPolygonElement polygon;

	private SVGOMTextElement text;

	private SVGOMPolygonElement completedBox;

	private Text iterationText;

	private SVGPoint iterationPosition;

	private Text errorsText;

	private SVGPoint errorsPosition;

	private String originalStyle;

	private String errorStyle;

	private String selectedStyle;

	private boolean nested;

	private boolean selected;

	/**
	 * Constructs a new instance of SVGNode.
	 * 
	 * @param g
	 *            the g element
	 * @param polygon
	 *            the polygon element
	 * @param text
	 *            the test element
	 * @param nested
	 *            true if this node contains a nested node
	 * @param graph TODO
	 */
	public SVGNode(SVGGraph graph, GraphElement graphElement, SVGOMGElement g, final SVGOMPolygonElement polygon,
			SVGOMTextElement text, boolean nested) {
		this.graph = graph;
		this.graphElement = graphElement;
		this.g = g;
		this.polygon = polygon;
		this.text = text;
		this.nested = nested;
		if (nested) {
			iterationPosition = polygon.getPoints().getItem(2);
		} else {
			iterationPosition = polygon.getPoints().getItem(0);
		}
		errorsPosition = polygon.getPoints().getItem(3);
		originalStyle = polygon.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		errorStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraph.ERROR_COLOUR + ";");
		selectedStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraph.SELECTED_COLOUR + ";");

		EventTarget t = (EventTarget) g;
//		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, new EventListener() {
//		public void handleEvent(Event evt) {
////		JOptionPane.showMessageDialog(SVGDiagram.this, ((Text) SVGProcessor.this.text.getFirstChild()).getData());
//		setSelected(true);
//		}
//		}, false);

//		t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, new EventListener() {
//		public void handleEvent(Event evt) {
//		setSelected(!selected);
//		}
//		}, false);

		t.addEventListener(SVGConstants.SVG_DOMACTIVATE_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (evt instanceof UIEvent) {
					UIEvent uiEvent = (UIEvent) evt;
					if (uiEvent.getDetail() == 1) {
						selected = !selected;
						SVGNode.this.graph.graphController.setSelected(SVGNode.this.graphElement, selected);
						System.out.println(SVGNode.this.graphElement + "selected");
					} else if (uiEvent.getDetail() == 2) {
						polygon.setAttribute(
								SVGConstants.SVG_STYLE_ATTRIBUTE, errorStyle);
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
			polygon.setAttribute(
					SVGConstants.SVG_STYLE_ATTRIBUTE, selectedStyle);
		} else {
			polygon.setAttribute(
					SVGConstants.SVG_STYLE_ATTRIBUTE, originalStyle);			
		}
	}

	public void setIteration(final int iteration) {
		if (this.graph.updateManager != null) {
			if (iterationText == null) {
				addIterationText();
			}
			this.graph.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							if (iteration > 0) {
								iterationText.setData(String
										.valueOf(iteration));
							} else {
								iterationText.setData("");
							}
						}
					});
		}
	}

	public void setErrors(final int errors) {
		if (this.graph.updateManager != null) {
			if (errorsText == null) {
				addErrorsText();
			}
			this.graph.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							if (errors > 0) {
								errorsText.setData(String.valueOf(errors));
								polygon.setAttribute(
										SVGConstants.SVG_STYLE_ATTRIBUTE, errorStyle);

							} else {
								errorsText.setData("");
								polygon.setAttribute(
										SVGConstants.SVG_STYLE_ATTRIBUTE, originalStyle);
							}
						}
					});
		}
	}

	public void setCompleted(final float complete) {
		if (this.graph.updateManager != null) {
			if (completedBox == null) {
				addCompletedBox();
			}
			this.graph.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							completedBox.setAttribute(
									SVGConstants.SVG_POINTS_ATTRIBUTE,
									calculatePoints(complete));
							if (complete == 0f) {
								completedBox
								.setAttribute(
										SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
										"0");
							} else {
								completedBox
								.setAttribute(
										SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
										"1");
							}
						}
					});
		}
	}

	private void addIterationText() {
		if (this.graph.updateManager != null) {
			this.graph.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							Element text = SVGNode.this.graph.svgDocument.createElementNS(
									SVGGraph.svgNS, SVGConstants.SVG_TEXT_TAG);
							text.setAttribute(SVGConstants.SVG_X_ATTRIBUTE,
									String
									.valueOf(iterationPosition
											.getX() - 1.5));
							text.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE,
									String
									.valueOf(iterationPosition
											.getY() + 5.5));
							text.setAttribute(
									SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE,
									"end");
							text.setAttribute(
									SVGConstants.SVG_FONT_SIZE_ATTRIBUTE,
									"5.5");
							text.setAttribute(
									SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE,
									"sans-serif");
							synchronized (g) {
								if (iterationText == null) {
									iterationText = SVGNode.this.graph.svgDocument
									.createTextNode("");
									text.appendChild(iterationText);
									g.appendChild(text);
								}
							}
						}
					});
		}
	}

	private void addErrorsText() {
		if (this.graph.updateManager != null) {
			this.graph.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							Element text = SVGNode.this.graph.svgDocument.createElementNS(
									SVGGraph.svgNS, SVGConstants.SVG_TEXT_TAG);
							text
							.setAttribute(
									SVGConstants.SVG_X_ATTRIBUTE,
									String.valueOf(errorsPosition
											.getX() - 1.5));
							text
							.setAttribute(
									SVGConstants.SVG_Y_ATTRIBUTE,
									String.valueOf(errorsPosition
											.getY() - 1.0));
							text.setAttribute(
									SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE,
									"end");
							text.setAttribute(
									SVGConstants.SVG_FONT_SIZE_ATTRIBUTE,
									"5.5");
							text.setAttribute(
									SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE,
									"sans-serif");
							text.setAttribute(
									SVGConstants.SVG_FILL_ATTRIBUTE, "red");
							synchronized (g) {
								if (errorsText == null) {
									errorsText = SVGNode.this.graph.svgDocument
									.createTextNode("");
									text.appendChild(errorsText);
									g.appendChild(text);
								}
							}
						}
					});
		}
	}

	private void addCompletedBox() {
		if (this.graph.updateManager != null) {
			this.graph.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							synchronized (g) {
								if (completedBox == null) {
									completedBox = (SVGOMPolygonElement) SVGNode.this.graph.svgDocument
									.createElementNS(
											SVGGraph.svgNS,
											SVGConstants.SVG_POLYGON_TAG);
									completedBox
									.setAttribute(
											SVGConstants.SVG_POINTS_ATTRIBUTE,
											calculatePoints(0f));
									completedBox
									.setAttribute(
											SVGConstants.SVG_FILL_ATTRIBUTE,
											SVGGraph.COMPLETED_COLOUR);
									completedBox
									.setAttribute(
											SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE,
											"0.8");
//									completedBox
//.setAttribute(
//									SVGConstants.SVG_STROKE_ATTRIBUTE,
//									"black");
//									completedBox
//									.setAttribute(
//									SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE,
//									"0.6");
									g.insertBefore(completedBox, text);
								}
							}
						}
					});
		}
	}

	/**
	 * Calculates the points that specify the proportion completed polygon.
	 * 
	 * @param complete
	 *            the proportion completed
	 * @return the points that specify the proportion completed polygon
	 */
	private String calculatePoints(float complete) {
		StringBuffer sb = new StringBuffer();
		SVGPointList points = polygon.getPoints();
		float x1, x2, y1, y2;
		if (nested) {
			x1 = points.getItem(2).getX() - 0.4f;
			x2 = points.getItem(0).getX() + 0.4f;
			y1 = points.getItem(2).getY() + 0.4f;
			y2 = points.getItem(0).getY() - 0.4f;
		} else {
			x1 = points.getItem(0).getX() - 0.4f;
			x2 = points.getItem(1).getX() + 0.4f;
			y1 = points.getItem(0).getY() + 0.4f;
			y2 = points.getItem(2).getY() - 0.4f;
		}
		x1 = x2 + ((x1 - x2) * complete);
		sb.append(x1 + "," + y1 + " ");
		sb.append(x2 + "," + y1 + " ");
		sb.append(x2 + "," + y2 + " ");
		sb.append(x1 + "," + y2 + " ");
		sb.append(x1 + "," + y1);

		return sb.toString();
	}
}