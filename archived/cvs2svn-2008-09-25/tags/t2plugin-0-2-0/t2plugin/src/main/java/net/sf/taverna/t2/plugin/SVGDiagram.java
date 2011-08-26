package net.sf.taverna.t2.plugin;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.GenericText;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.dom.svg.SVGOMTitleElement;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.DotView;
import org.embl.ebi.escience.scuflui.ScuflSVGDiagram;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

/**
 * An SVG representation of a ScuflModel.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class SVGDiagram extends JComponent {

	private static Logger logger = Logger.getLogger(SVGDiagram.class);

	private static final String COMPLETED_COLOUR = "grey";

	private static final String OUTPUT_COLOUR = "blue";

	private static final String ERROR_COLOUR = "red";

	private static final int OUTPUT_FLASH_PERIOD = 200;

	private static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

	private static Timer timer = new Timer(true);

	static SAXSVGDocumentFactory docFactory = null;

	static {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		logger.info("Using XML parser " + parser);
		docFactory = new SAXSVGDocumentFactory(parser);
	}

	private DotView dot;

	private SVGDocument svgDocument;

	private JSVGCanvas svgCanvas;

	private UpdateManager updateManager;

	private Map<String, SVGProcessor> processorMap = new HashMap<String, SVGProcessor>();

	private Map<String, List<SVGDatalink>> datalinkMap = new HashMap<String, List<SVGDatalink>>();

	/**
	 * Constructs a new instance of SVGDiagram.
	 * 
	 */
	public SVGDiagram() {
		setBackground(Color.white);
		setOpaque(false);
		setLayout(new GridLayout());
		svgCanvas = new JSVGCanvas();
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		svgCanvas.setOpaque(false);
		add(svgCanvas);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				updateManager = svgCanvas.getUpdateManager();
			}
		});
	}

	/**
	 * Traverses nodes in the SVG DOM and creates SVGProcessors and
	 * SVGDatalinks.
	 * 
	 * @param nodes
	 *            SVG diagram nodes
	 */
	private void mapNodes(NodeList nodes) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof SVGOMGElement) {
				SVGOMGElement gElement = (SVGOMGElement) node;
				String gElementClass = gElement
						.getAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE);
				if ("graph".equals(gElementClass)) {
					mapNodes(node.getChildNodes());
				} else if ("node".equals(gElementClass)
						|| "cluster".equals(gElementClass)) {
					String title = null;
					SVGOMPolygonElement polygon = null;
					SVGOMTextElement text = null;
					Node child = node.getFirstChild();
					while (child != null) {
						if (child instanceof SVGOMTitleElement) {
							SVGOMTitleElement titleElement = (SVGOMTitleElement) child;
							Object titleElementChild = titleElement
									.getFirstChild();
							if (titleElementChild instanceof GenericText) {
								GenericText textElement = (GenericText) titleElementChild;
								title = textElement.getData();
							}
						} else if (child instanceof SVGOMPolygonElement) {
							polygon = (SVGOMPolygonElement) child;
						} else if (child instanceof SVGOMTextElement) {
							text = (SVGOMTextElement) child;
						}
						child = child.getNextSibling();
					}
					if (title != null && polygon != null && text != null) {
						boolean nested = "cluster".equals(gElementClass);
						if (nested) {
							// if this is a nested workflow remove 'cluster_'
							// from the title
							Object textElementChild = text.getFirstChild();
							if (textElementChild instanceof GenericText) {
								GenericText textElement = (GenericText) textElementChild;
								String textData = textElement.getData();
								if (title.startsWith("cluster_")
										&& title.endsWith(textData)) {
									title = title.substring(8, title
											.lastIndexOf(textData));
								}
							}
						}
						processorMap.put(title, new SVGProcessor(gElement,
								polygon, text, nested));
					}
				} else if ("edge".equals(gElementClass)) {
					String title = null;
					SVGOMPathElement path = null;
					SVGOMPolygonElement polygon = null;
					Node child = node.getFirstChild();
					while (child != null) {
						if (child instanceof SVGOMTitleElement) {
							SVGOMTitleElement titleElement = (SVGOMTitleElement) child;
							Object titleElementChild = titleElement
									.getFirstChild();
							if (titleElementChild instanceof GenericText) {
								GenericText text = (GenericText) titleElementChild;
								title = text.getData();
							}
						} else if (child instanceof SVGOMPolygonElement) {
							polygon = (SVGOMPolygonElement) child;
						} else if (child instanceof SVGOMPathElement) {
							path = (SVGOMPathElement) child;
						}
						child = child.getNextSibling();
					}
					if (title != null && path != null && polygon != null) {
						mapDatalink(title, new SVGDatalink(path, polygon));
					}
				}
			} else {
				mapNodes(node.getChildNodes());
			}
		}
	}

	private void mapDatalink(String title, SVGDatalink datalink) {
		String sinkProcessor = title.substring(title.indexOf("->") + 2);
		int index = sinkProcessor.indexOf("WORKFLOWINTERNALSOURCE_");
		if (index > 0) {
			sinkProcessor = sinkProcessor.substring(0, index);
		}
		if (!datalinkMap.containsKey(sinkProcessor)) {
			datalinkMap.put(sinkProcessor, new ArrayList<SVGDatalink>());
		}
		datalinkMap.get(sinkProcessor).add(datalink);
	}

	/**
	 * Resets the diagram to its original appearance.
	 * 
	 */
	public void reset() {
		for (SVGProcessor node : processorMap.values()) {
			node.setCompleted(0f);
			node.setIteration(0);
			node.setErrors(0);
		}
	}

	/**
	 * Returns <code>true</code> if this diagrams contains a Processor with
	 * the given name.
	 * 
	 * @param processorId
	 *            the id of the Processor
	 * @return <code>true</code> if this diagrams contains a Processor with
	 *         the given id.
	 */
	public boolean containsProcessor(String processorId) {
		return processorMap.containsKey(processorId);
	}

	/**
	 * Returns <code>true</code> if this diagrams contains a Datalink with the
	 * given name.
	 * 
	 * @param datalinkId
	 *            the id of the Datalink
	 * @return <code>true</code> if this diagrams contains a Datalink with the
	 *         given id.
	 */
	public boolean containsDatalink(String datalinkId) {
		return datalinkMap.containsKey(datalinkId);
	}

	/**
	 * Sets the proportion of the processor's jobs that have been completed.
	 * 
	 * @param processorId
	 *            the id of the processor
	 * @param complete
	 *            the proportion of the processor's jobs that have been
	 *            completed, a value between 0.0 and 1.0
	 */
	public void setProcessorCompleted(String processorId, float complete) {
		if (processorMap.containsKey(processorId)) {
			processorMap.get(processorId).setCompleted(complete);
		}
	}

	/**
	 * Sets the processor's iteration count.
	 * 
	 * @param processorId
	 *            the id of the processor
	 * @param iteration
	 *            the number of iteration count
	 */
	public void setIteration(String processorId, int iteration) {
		if (processorMap.containsKey(processorId)) {
			processorMap.get(processorId).setIteration(iteration);
		}
	}

	/**
	 * Sets the processor's error count.
	 * 
	 * @param processorId
	 *            the id of the processor
	 * @param errors
	 *            the number of error count
	 */
	public void setErrors(String processorId, int errors) {
		if (processorMap.containsKey(processorId)) {
			processorMap.get(processorId).setErrors(errors);
		}
	}

	public void fireDatalink(final String datalinkId) {
		if (datalinkMap.containsKey(datalinkId)) {
			for (SVGDatalink datalink : datalinkMap.get(datalinkId)) {
				datalink.setColour(OUTPUT_COLOUR);
			}
			timer.schedule(new TimerTask() {
				public void run() {
					for (SVGDatalink datalink : datalinkMap.get(datalinkId)) {
						datalink.resetStyle();
					}
				}
			}, OUTPUT_FLASH_PERIOD);
		}
	}

	/**
	 * Sets the model for this diagram and generates the SVG Document.
	 * 
	 * @param model
	 */
	public void setModel(ScuflModel model) {
		updateManager = null;
		processorMap.clear();
		datalinkMap.clear();
		dot = new DotView(model);
		dot.setPortDisplay(DotView.NONE);
		dot.setTypeLabelDisplay(false);

		try {
			svgDocument = ScuflSVGDiagram.getSVG(dot.getDot());
			mapNodes(svgDocument.getChildNodes());
			svgCanvas.setSVGDocument(svgDocument);
		} catch (IOException e) {
			logger.error("Couldn't generate svg", e);
		}

	}

	public JSVGCanvas getSvgCanvas() {
		return svgCanvas;
	}

	/**
	 * SVG representation of a processor.
	 * 
	 * @author David Withers
	 */
	public class SVGProcessor {

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

		private boolean nested;

		/**
		 * Constructs a new instance of SVGProcessor.
		 * 
		 * @param g
		 *            the g element
		 * @param polygon
		 *            the polygon element
		 * @param text
		 *            the test element
		 * @param nested
		 *            true if this processor contains a nested dataflow
		 */
		public SVGProcessor(SVGOMGElement g, SVGOMPolygonElement polygon,
				SVGOMTextElement text, boolean nested) {
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
			errorStyle = originalStyle.replaceFirst("stroke:[^;]*;", "stroke:" + ERROR_COLOUR + ";");
		}

		public void setIteration(final int iteration) {
			if (updateManager != null) {
				if (iterationText == null) {
					addIterationText();
				}
				updateManager.getUpdateRunnableQueue().invokeLater(
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
			if (updateManager != null) {
				if (errorsText == null) {
					addErrorsText();
				}
				updateManager.getUpdateRunnableQueue().invokeLater(
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
			if (updateManager != null) {
				if (completedBox == null) {
					addCompletedBox();
				}
				updateManager.getUpdateRunnableQueue().invokeLater(
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
			if (updateManager != null) {
				updateManager.getUpdateRunnableQueue().invokeLater(
						new Runnable() {
							public void run() {
								Element text = svgDocument.createElementNS(
										svgNS, SVGConstants.SVG_TEXT_TAG);
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
										iterationText = svgDocument
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
			if (updateManager != null) {
				updateManager.getUpdateRunnableQueue().invokeLater(
						new Runnable() {
							public void run() {
								Element text = svgDocument.createElementNS(
										svgNS, SVGConstants.SVG_TEXT_TAG);
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
										errorsText = svgDocument
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
			if (updateManager != null) {
				updateManager.getUpdateRunnableQueue().invokeLater(
						new Runnable() {
							public void run() {
								synchronized (g) {
									if (completedBox == null) {
										completedBox = (SVGOMPolygonElement) svgDocument
												.createElementNS(
														svgNS,
														SVGConstants.SVG_POLYGON_TAG);
										completedBox
												.setAttribute(
														SVGConstants.SVG_POINTS_ATTRIBUTE,
														calculatePoints(0f));
										completedBox
												.setAttribute(
														SVGConstants.SVG_FILL_ATTRIBUTE,
														COMPLETED_COLOUR);
										completedBox
												.setAttribute(
														SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE,
														"0.8");
//										completedBox
//												.setAttribute(
//														SVGConstants.SVG_STROKE_ATTRIBUTE,
//														"black");
//										completedBox
//												.setAttribute(
//														SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE,
//														"0.6");
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

	/**
	 * SVG representation of a datalink
	 * 
	 * @author David Withers
	 */
	public class SVGDatalink {

		private SVGOMPathElement path;

		private SVGOMPolygonElement polygon;

		private String originalPathStyle;

		private String originalPolygonStyle;

		/**
		 * Constructs a new instance of an SVGDatalink.
		 * 
		 * @param path
		 *            the path element
		 * @param polygon
		 *            the polygon element
		 */
		public SVGDatalink(SVGOMPathElement path, SVGOMPolygonElement polygon) {
			this.path = path;
			this.polygon = polygon;
			originalPathStyle = path
					.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
			originalPolygonStyle = polygon
					.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		}

		/**
		 * Set the SVG colour attributes for the datalink.
		 * 
		 * @param colour
		 *            the new colour
		 */
		public void setColour(final String colour) {
			if (updateManager != null) {
				updateManager.getUpdateRunnableQueue().invokeLater(
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
		 * Resets the SVG style attributes of the datalink to their original
		 * values.
		 * 
		 */
		public void resetStyle() {
			if (updateManager != null) {
				updateManager.getUpdateRunnableQueue().invokeLater(
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

}
