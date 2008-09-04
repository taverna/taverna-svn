/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.models.graph.svg;

import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseClickEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseMovedEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseUpEventListener;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

/**
 * SVG representation of a graph.
 * 
 * @author David Withers
 */
public class SVGGraph extends Graph implements SVGMonitorShape {

	private SVGGraphController graphController;

	private SVGMouseClickEventListener mouseClickAction;

	private SVGMouseMovedEventListener mouseMovedAction;

	private SVGMouseUpEventListener mouseUpAction;

	private SVGOMGElement g;

	private SVGOMPolygonElement polygon;

	private SVGOMEllipseElement ellipse;

	private SVGOMTextElement text;

	private SVGOMPolygonElement completedPolygon;

	private Text iterationText;

//	private Text errorsText;
//
//	private SVGPoint errorsPosition;

	private String originalStyle;

//	private String errorStyle;

	private String selectedStyle;
	
	public SVGGraph(GraphEventManager eventManager) {
		super(eventManager);
		mouseClickAction = new SVGMouseClickEventListener(eventManager, this);
		mouseMovedAction = new SVGMouseMovedEventListener(eventManager, this);
		mouseUpAction = new SVGMouseUpEventListener(eventManager, this);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGShape#getGraphComponent()
	 */
	public SVGGraphController getGraphController() {
		return graphController;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGShape#setGraphComponent(net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphComponent)
	 */
	public void setGraphController(SVGGraphController graphController) {
		this.graphController = graphController;
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
//		if (getDataflowObject() != null) {
			EventTarget t = (EventTarget) g;
			t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
			t.addEventListener(SVGConstants.SVG_MOUSEMOVE_EVENT_TYPE, mouseMovedAction, false);
			t.addEventListener(SVGConstants.SVG_MOUSEUP_EVENT_TYPE, mouseUpAction, false);
//		}		
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
//		errorsPosition = polygon.getPoints().getItem(3);
		originalStyle = polygon.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
//		errorStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.ERROR_COLOUR + ";");
		selectedStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.SELECTED_COLOUR + ";" +
				"stroke-width:2");
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
//		errorStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + SVGGraphComponent.ERROR_COLOUR + ";");
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
		UpdateManager updateManager = this.graphController.updateManager;
		if (updateManager != null) {
			updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							if (selected) {
								polygon.setAttribute(
										SVGConstants.SVG_STYLE_ATTRIBUTE, selectedStyle);
							} else {
								polygon.setAttribute(
										SVGConstants.SVG_STYLE_ATTRIBUTE, originalStyle);			
							}
						}
					});
		}
	}

	public void setIteration(final int iteration) {
		if (iterationText != null && this.graphController.updateManager != null) {
			this.graphController.updateManager.getUpdateRunnableQueue().invokeLater(
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


//	/* (non-Javadoc)
//	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setErrors(int)
//	 */
//	public void setErrors(final int errors) {
//		if (this.graphController.updateManager != null) {
//			if (errorsText == null) {
//				addErrorsText();
//			}
//			this.graphController.updateManager.getUpdateRunnableQueue().invokeLater(
//					new Runnable() {
//						public void run() {
//							if (errors > 0) {
//								errorsText.setData(String.valueOf(errors));
//								polygon.setAttribute(
//										SVGConstants.SVG_STYLE_ATTRIBUTE, errorStyle);
//
//							} else {
//								errorsText.setData("");
//								polygon.setAttribute(
//										SVGConstants.SVG_STYLE_ATTRIBUTE, originalStyle);
//							}
//						}
//					});
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setCompleted(float)
//	 */
//	public void setCompleted(final float complete) {
//		if (this.graphController.updateManager != null) {
//			if (completedBox == null) {
//				addCompletedBox();
//			}
//			this.graphController.updateManager.getUpdateRunnableQueue().invokeLater(
//					new Runnable() {
//						public void run() {
//							completedBox.setAttribute(
//									SVGConstants.SVG_POINTS_ATTRIBUTE,
//									calculatePoints(complete));
//							if (complete == 0f) {
//								completedBox
//								.setAttribute(
//										SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
//										"0");
//							} else {
//								completedBox
//								.setAttribute(
//										SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
//										"1");
//							}
//						}
//					});
//		}
//	}
//
	
//	private void addErrorsText() {
//		if (this.graphController.updateManager != null) {
//			this.graphController.updateManager.getUpdateRunnableQueue().invokeLater(
//					new Runnable() {
//						public void run() {
//							Element text = SVGGraph.this.graphController.getSvgCanvas().getSVGDocument().createElementNS(
//									SVGUtil.svgNS, SVGConstants.SVG_TEXT_TAG);
//							text
//							.setAttribute(
//									SVGConstants.SVG_X_ATTRIBUTE,
//									String.valueOf(errorsPosition
//											.getX() - 1.5));
//							text
//							.setAttribute(
//									SVGConstants.SVG_Y_ATTRIBUTE,
//									String.valueOf(errorsPosition
//											.getY() - 1.0));
//							text.setAttribute(
//									SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE,
//									"end");
//							text.setAttribute(
//									SVGConstants.SVG_FONT_SIZE_ATTRIBUTE,
//									"5.5");
//							text.setAttribute(
//									SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE,
//									"sans-serif");
//							text.setAttribute(
//									SVGConstants.SVG_FILL_ATTRIBUTE, "red");
//							synchronized (g) {
//								if (errorsText == null) {
//									errorsText = SVGGraph.this.graphController.getSvgCanvas().getSVGDocument()
//									.createTextNode("");
//									text.appendChild(errorsText);
//									g.appendChild(text);
//								}
//							}
//						}
//					});
//		}
//	}
//
//	private void addCompletedBox() {
//		if (this.graphController.updateManager != null) {
//			this.graphController.updateManager.getUpdateRunnableQueue().invokeLater(
//					new Runnable() {
//						public void run() {
//							synchronized (g) {
//								if (completedBox == null) {
//									completedBox = (SVGOMPolygonElement) SVGGraph.this.graphController.getSvgCanvas().getSVGDocument()
//									.createElementNS(
//											SVGUtil.svgNS,
//											SVGConstants.SVG_POLYGON_TAG);
//									completedBox
//									.setAttribute(
//											SVGConstants.SVG_POINTS_ATTRIBUTE,
//											calculatePoints(0f));
//									completedBox
//									.setAttribute(
//											SVGConstants.SVG_FILL_ATTRIBUTE,
//											SVGGraphComponent.COMPLETED_COLOUR);
//									completedBox
//									.setAttribute(
//											SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE,
//											"0.8");
////									completedBox
////.setAttribute(
////									SVGConstants.SVG_STROKE_ATTRIBUTE,
////									"black");
////									completedBox
////									.setAttribute(
////									SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE,
////									"0.6");
//									g.insertBefore(completedBox, text);
//								}
//							}
//						}
//					});
//		}
//	}

	public void setCompleted(final float complete) {
		super.setCompleted(complete);
		UpdateManager updateManager = this.graphController.updateManager;
		if (updateManager != null) {
			updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							completedPolygon.setAttribute(
									SVGConstants.SVG_POINTS_ATTRIBUTE,
									calculatePoints(complete));
							if (complete == 0f) {
								completedPolygon
								.setAttribute(
										SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
								"0");
							} else {
								completedPolygon
								.setAttribute(
										SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
								"1");
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
			x1 = points.getItem(2).getX() - 0.4f;
			x2 = points.getItem(0).getX() + 0.4f;
			y1 = points.getItem(2).getY() + 0.4f;
			y2 = points.getItem(0).getY() - 0.4f;
//		} else {
//			x1 = points.getItem(0).getX() - 0.4f;
//			x2 = points.getItem(1).getX() + 0.4f;
//			y1 = points.getItem(0).getY() + 0.4f;
//			y2 = points.getItem(2).getY() - 0.4f;
//		}
		x1 = x2 + ((x1 - x2) * complete);
		sb.append(x1 + "," + y1 + " ");
		sb.append(x2 + "," + y1 + " ");
		sb.append(x2 + "," + y2 + " ");
		sb.append(x1 + "," + y2 + " ");
		sb.append(x1 + "," + y1);

		return sb.toString();
	}

	public SVGOMPolygonElement getCompletedPolygon() {
		return completedPolygon;
	}

	public void setCompletedPolygon(SVGOMPolygonElement polygon) {
		this.completedPolygon = polygon;
		completedPolygon.setAttribute(
				SVGConstants.SVG_POINTS_ATTRIBUTE,
				calculatePoints(complete));
	}

	/**
	 * Returns the iterationText.
	 *
	 * @return the iterationText
	 */
	public Text getIterationText() {
		return iterationText;
	}

	/**
	 * Sets the iterationText.
	 *
	 * @param iterationText the new iterationText
	 */
	public void setIterationText(Text iterationText) {
		this.iterationText = iterationText;
	}
	
}
