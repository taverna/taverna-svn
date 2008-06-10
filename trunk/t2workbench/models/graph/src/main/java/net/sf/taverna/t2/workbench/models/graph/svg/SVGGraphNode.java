package net.sf.taverna.t2.workbench.models.graph.svg;

import net.sf.taverna.t2.workbench.models.graph.GraphNode;

import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.events.UIEvent;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

/**
 * SVG representation of a graph node.
 * 
 * @author David Withers
 */
/**
 * @author David Withers
 *
 */
public class SVGGraphNode extends GraphNode implements SVGShape {

	private SVGGraphComponent graphComponent;

	private SVGOMGElement g;

	private SVGOMPolygonElement polygon;

	private SVGOMEllipseElement ellipse;

	private SVGOMTextElement text;

	private SVGOMPolygonElement completedBox;

	private Text iterationText;

	private SVGPoint iterationPosition;

	private Text errorsText;

	private SVGPoint errorsPosition;

	private String originalStyle;

	private String errorStyle;

	private String selectedStyle;

	public SVGGraphNode() {
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGShape#getGraphComponent()
	 */
	public SVGGraphComponent getGraphComponent() {
		return graphComponent;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGShape#setGraphComponent(net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphComponent)
	 */
	public void setGraphComponent(SVGGraphComponent graphComponent) {
		this.graphComponent = graphComponent;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#getG()
	 */
	public SVGOMGElement getG() {
		return g;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setG(org.apache.batik.dom.svg.SVGOMGElement)
	 */
	public void setG(SVGOMGElement g) {
		this.g = g;
		if (getDataflowObject() != null) {
			EventTarget t = (EventTarget) g;
//			t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, new EventListener() {
//			public void handleEvent(Event evt) {
////			JOptionPane.showMessageDialog(SVGDiagram.this, ((Text) SVGProcessor.this.text.getFirstChild()).getData());
//			setSelected(true);
//			}
//			}, false);

//			t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, new EventListener() {
//			public void handleEvent(Event evt) {
//			setSelected(!selected);
//			}
//			}, false);

			t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, new EventListener() {
				public void handleEvent(Event evt) {
					if (evt instanceof MouseEvent) {
						MouseEvent uiEvent = (MouseEvent) evt;
						if (uiEvent.getButton() == 0) {
							getSelectionModel().addSelection(getDataflowObject());
						} else if (uiEvent.getButton() == 2 ) {
							getSelectionModel().removeSelection(getDataflowObject());
						}
					}
				}
			}, false);
		}

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#getPolygon()
	 */
	public SVGOMPolygonElement getPolygon() {
		return polygon;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setPolygon(org.apache.batik.dom.svg.SVGOMPolygonElement)
	 */
	public void setPolygon(SVGOMPolygonElement polygon) {
		this.polygon = polygon;
		errorsPosition = polygon.getPoints().getItem(3);
		originalStyle = polygon.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		errorStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.ERROR_COLOUR + ";");
		selectedStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.SELECTED_COLOUR + ";" +
				"stroke-width:2");
		
//		if (isExpanded()) {
//		iterationPosition = polygon.getPoints().getItem(2);
//		} else {
		iterationPosition = polygon.getPoints().getItem(0);
//		}
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
		originalStyle = ellipse.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		errorStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.ERROR_COLOUR + ";");
		selectedStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.SELECTED_COLOUR + ";");
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#getText()
	 */
	public SVGOMTextElement getText() {
		return text;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setText(org.apache.batik.dom.svg.SVGOMTextElement)
	 */
	public void setText(SVGOMTextElement text) {
		this.text = text;
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
								if (polygon != null) {
									polygon.setAttribute(
											SVGConstants.SVG_STYLE_ATTRIBUTE, selectedStyle);
								} else {
									ellipse.setAttribute(
											SVGConstants.SVG_STYLE_ATTRIBUTE, selectedStyle);
								}
							} else {
								if (polygon != null) {
									polygon.setAttribute(
											SVGConstants.SVG_STYLE_ATTRIBUTE, originalStyle);	
								} else {
									ellipse.setAttribute(
											SVGConstants.SVG_STYLE_ATTRIBUTE, originalStyle);	
								}
							}
						}
					});
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setIteration(int)
	 */
	public void setIteration(final int iteration) {
		if (this.graphComponent.updateManager != null) {
			if (iterationText == null) {
				addIterationText();
			}
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setErrors(int)
	 */
	public void setErrors(final int errors) {
		if (this.graphComponent.updateManager != null) {
			if (errorsText == null) {
				addErrorsText();
			}
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setCompleted(float)
	 */
	public void setCompleted(final float complete) {
		if (this.graphComponent.updateManager != null) {
			if (completedBox == null) {
				addCompletedBox();
			}
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
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
		if (this.graphComponent.updateManager != null) {
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							Element text = SVGGraphNode.this.graphComponent.getSvgCanvas().getSVGDocument().createElementNS(
									SVGGraphComponent.svgNS, SVGConstants.SVG_TEXT_TAG);
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
									iterationText = SVGGraphNode.this.graphComponent.getSvgCanvas().getSVGDocument()
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
		if (this.graphComponent.updateManager != null) {
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							Element text = SVGGraphNode.this.graphComponent.getSvgCanvas().getSVGDocument().createElementNS(
									SVGGraphComponent.svgNS, SVGConstants.SVG_TEXT_TAG);
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
									errorsText = SVGGraphNode.this.graphComponent.getSvgCanvas().getSVGDocument()
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
		if (this.graphComponent.updateManager != null) {
			this.graphComponent.updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							synchronized (g) {
								if (completedBox == null) {
									completedBox = (SVGOMPolygonElement) SVGGraphNode.this.graphComponent.getSvgCanvas().getSVGDocument()
									.createElementNS(
											SVGGraphComponent.svgNS,
											SVGConstants.SVG_POLYGON_TAG);
									completedBox
									.setAttribute(
											SVGConstants.SVG_POINTS_ATTRIBUTE,
											calculatePoints(0f));
									completedBox
									.setAttribute(
											SVGConstants.SVG_FILL_ATTRIBUTE,
											SVGGraphComponent.COMPLETED_COLOUR);
									completedBox
									.setAttribute(
											SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE,
									"0.8");
//									completedBox
//									.setAttribute(
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
//		if (isExpanded()) {
//		x1 = points.getItem(2).getX() - 0.4f;
//		x2 = points.getItem(0).getX() + 0.4f;
//		y1 = points.getItem(2).getY() + 0.4f;
//		y2 = points.getItem(0).getY() - 0.4f;
//		} else {
		x1 = points.getItem(0).getX() - 0.4f;
		x2 = points.getItem(1).getX() + 0.4f;
		y1 = points.getItem(0).getY() + 0.4f;
		y2 = points.getItem(2).getY() - 0.4f;
//		}
		x1 = x2 + ((x1 - x2) * complete);
		sb.append(x1 + "," + y1 + " ");
		sb.append(x2 + "," + y1 + " ");
		sb.append(x2 + "," + y2 + " ");
		sb.append(x1 + "," + y2 + " ");
		sb.append(x1 + "," + y1);

		return sb.toString();
	}
}