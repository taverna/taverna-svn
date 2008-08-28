package net.sf.taverna.t2.workbench.models.graph.svg;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.models.graph.DotWriter;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphNode.Shape;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.GenericText;
import org.apache.batik.dom.svg.SVGOMAElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMPolylineElement;
import org.apache.batik.dom.svg.SVGOMSVGElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.dom.svg.SVGOMTitleElement;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

public abstract class SVGGraphController extends GraphController {

	private static Logger logger = Logger.getLogger(SVGGraphController.class);

	private Map<String, SVGShape> processorMap = new HashMap<String, SVGShape>();

	private Map<String, List<SVGGraphEdge>> datalinkMap = new HashMap<String, List<SVGGraphEdge>>();

	SVGDocument svgDocument;

	private EdgeLine edgeLine;
	
	UpdateManager updateManager;

	public static final String OUTPUT_COLOUR = "blue";

	public static final int OUTPUT_FLASH_PERIOD = 200;

	static final Timer timer = new Timer(true);

	public SVGGraphController(Dataflow dataflow, JComponent component) {
		super(dataflow, new SVGGraphModelFactory(), component);
	}

	public SVGGraphController(Dataflow dataflow, GraphEventManager graphEventManager, JComponent component) {
		super(dataflow, new SVGGraphModelFactory(), graphEventManager, component);
	}

	public SVGDocument generateSVGDocument() {
		svgDocument = null;
		updateManager = null;
		processorMap.clear();
		datalinkMap.clear();

		Graph graph = generateGraph();
		try {
			StringWriter stringWriter = new StringWriter();
			DotWriter dotWriter = new DotWriter(stringWriter);
			dotWriter.writeGraph(graph);
//			System.out.println(stringWriter.toString());
			svgDocument = SVGUtil.getSVG(stringWriter.toString());
			edgeLine = EdgeLine.createAndAdd(svgDocument, this);
			mapNodes(svgDocument.getChildNodes());
		} catch (IOException e) {
			logger.error("Couldn't generate svg", e);
		}
		return svgDocument;
	}
	
	public void setUpdateManager(UpdateManager updateManager) {
		this.updateManager = updateManager;
		resetSelection();
	}

	public boolean startEdgeCreation(GraphElement graphElement, Point point) {
		boolean alreadyStarted = edgeCreationFromSource || edgeCreationFromSink;
		boolean started = super.startEdgeCreation(graphElement, point);
		if (!alreadyStarted && started) {
			if (edgeMoveElement instanceof SVGGraphEdge) {
				SVGGraphEdge svgGraphEdge = (SVGGraphEdge) edgeMoveElement;
				SVGPoint sourcePoint = svgGraphEdge.getPath().getPointAtLength(0f);
				edgeLine.setSourcePoint(new Point((int) sourcePoint.getX(), (int) sourcePoint.getY()));
			} else {
				edgeLine.setSourcePoint(point);
			}
			edgeLine.setTargetPoint(point);
			edgeLine.setColour(Color.BLACK);
			// edgeLine.setVisible(true);
		}
		return started;
	}

	public boolean moveEdgeCreationTarget(GraphElement graphElement, Point point) {
		boolean linkValid = super.moveEdgeCreationTarget(graphElement, point);
		if (edgeMoveElement instanceof SVGGraphEdge) {
			((SVGGraphEdge) edgeMoveElement).setVisible(false);
		}
		if (edgeCreationFromSink) {
			edgeLine.setSourcePoint(point);
			if (linkValid) {
				edgeLine.setColour(Color.GREEN);
			} else {
				edgeLine.setColour(Color.BLACK);
			}
			edgeLine.setVisible(true);
		} else if (edgeCreationFromSource) {
			edgeLine.setTargetPoint(point);
			if (linkValid) {
				edgeLine.setColour(Color.GREEN);
			} else {
				edgeLine.setColour(Color.BLACK);
			}
			edgeLine.setVisible(true);
		}
		return linkValid;
	}

	public boolean stopEdgeCreation(GraphElement graphElement, Point point) {
		GraphEdge movedEdge = edgeMoveElement;
		boolean edgeCreated = super.stopEdgeCreation(graphElement, point);
		if (!edgeCreated && movedEdge instanceof SVGGraphEdge) {
			((SVGGraphEdge) movedEdge).setVisible(true);
		}
		edgeLine.setVisible(false);
		return edgeCreated;
	}

//	private void mapGraphElements(Graph graph) {
//		graphElementMap.put(graph.getId(), graph);
//		for (Graph subgraph : graph.getSubgraphs()) {
//			mapGraphElements(subgraph);
//		}
//		for (GraphNode node : graph.getNodes()) {
//			if (node.isExpanded()) {
//				mapGraphElements(node.getGraph());
//			} else {
//				graphElementMap.put(node.getId(), node);
//			}
//		}
//		for (GraphEdge edge : graph.getEdges()) {
//			if (!graphEdgeMap.containsKey(edge.getId())) {
//				graphEdgeMap.put(edge.getId(), new ArrayList<GraphElement>());
//			}
//			graphEdgeMap.get(edge.getId()).add(edge);
//		}
//	}

	/**
	 * Traverses nodes in the SVG DOM and maps to SVGGraphNode and
	 * SVGGraphEdges.
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
					mapGraph(node, gElement);
					mapNodes(node.getChildNodes());
				} else if ("cluster".equals(gElementClass)) {
					mapCluster(node, gElement);
				} else if ("node".equals(gElementClass)) {
					mapNode(node, gElement);
				} else if ("edge".equals(gElementClass)) {
					mapEdge(node);
				}
			} else if (node instanceof SVGOMSVGElement) {
//				((SVGOMSVGElement) node).setAttribute("width", "10");
//				((SVGOMSVGElement) node).setAttribute("height", "10");
//				((SVGOMSVGElement) node).setAttribute("preserveAspectRatio", "none");
//				((SVGOMSVGElement) node).setAttribute("viewBox", "0, 0, "+size.width+", "+size.height);
				mapNodes(node.getChildNodes());
			} else {
				mapNodes(node.getChildNodes());
			}
		}
	}

	private void mapGraph(Node node, SVGOMGElement gElement) {
		String title = null;
		SVGOMPolygonElement polygon = null;
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof SVGOMTitleElement) {
				title = getTitle((SVGOMTitleElement) child);
				// } else if (child instanceof SVGOMPolygonElement) {
				// polygon = (SVGOMPolygonElement) child;
			}
			child = child.getNextSibling();
		}
		if (title != null/* && polygon != null */) {
			GraphElement graphElement = graphElementMap.get(title);
			if (graphElement instanceof SVGGraph) {
				SVGGraph svgGraph = (SVGGraph) graphElement;
				svgGraph.setGraphController(this);
				svgGraph.setG(gElement);
				// svgGraph.setPolygon(polygon);
			}
		}
	}

	private void mapCluster(Node node, SVGOMGElement gElement) {
		String title = null;
		SVGOMPolygonElement polygon = null;
		SVGOMTextElement text = null;
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof SVGOMTitleElement) {
				title = getTitle((SVGOMTitleElement) child);
			} else if (child instanceof SVGOMPolygonElement) {
				polygon = (SVGOMPolygonElement) child;
			} else if (child instanceof SVGOMTextElement) {
				text = (SVGOMTextElement) child;
			}
			child = child.getNextSibling();
		}
		if (title != null && polygon != null) {
			if (title.startsWith("cluster_")) {
				title = title.substring(8);
			}
			GraphElement graphElement = graphElementMap.get(title);
			if (graphElement instanceof SVGGraph) {
				SVGGraph svgGraph = (SVGGraph) graphElement;
				svgGraph.setGraphController(this);
				svgGraph.setG(gElement);
				svgGraph.setPolygon(polygon);
				svgGraph.setText(text);
				SVGOMPolygonElement progressPolygon = createProgressPolygon();
				gElement.insertBefore(progressPolygon, text);
				svgGraph.setCompletedPolygon(progressPolygon);

				Element textElement = createIterationTextElement(polygon.getPoints().getItem(2));
				gElement.appendChild(textElement);
				Text textNode = svgDocument.createTextNode("");
				textElement.appendChild(textNode);
				svgGraph.setIterationText(textNode);

				processorMap.put(title, svgGraph);
			}
		}
	}

	private void mapNode(Node node, SVGOMGElement gElement) {
		String title = null;
		SVGOMPolygonElement polygon = null;
		SVGOMEllipseElement ellipse = null;
		List<SVGOMTextElement> text = new ArrayList<SVGOMTextElement>();
		List<SVGOMPolylineElement> lines = new ArrayList<SVGOMPolylineElement>();
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof SVGOMTitleElement) {
				title = getTitle((SVGOMTitleElement) child);
			} else if (child instanceof SVGOMPolygonElement) {
				polygon = (SVGOMPolygonElement) child;
			} else if (child instanceof SVGOMEllipseElement) {
				ellipse = (SVGOMEllipseElement) child;
			} else if (child instanceof SVGOMTextElement) {
				text.add((SVGOMTextElement) child);
			} else if (child instanceof SVGOMPolylineElement) {
				lines.add((SVGOMPolylineElement) child);
			}
			child = child.getNextSibling();
		}
		if (title != null && (polygon != null || ellipse != null)) {
			GraphElement graphElement = graphElementMap.get(title);
			if (graphElement instanceof SVGGraphNode) {
				SVGGraphNode svgGraphNode = (SVGGraphNode) graphElement;
				svgGraphNode.setGraphController(this);
				svgGraphNode.setG(gElement);
				if (polygon != null) {
					svgGraphNode.setPolygon(polygon);
					SVGOMPolygonElement progressPolygon = createProgressPolygon();
					if (text.size() > 0) {
						gElement.insertBefore(progressPolygon, text.get(0));
					} else {
						gElement.insertBefore(progressPolygon, null);
					}
					svgGraphNode.setCompletedPolygon(progressPolygon);
				} else {
					svgGraphNode.setEllipse(ellipse);
				}
				if (svgGraphNode.getShape().equals(Shape.BOX)) {
					if (text.size() == 1) {
						svgGraphNode.setText(text.get(0));
						
						Element textElement = createIterationTextElement(polygon.getPoints().getItem(0));
						gElement.appendChild(textElement);
						Text textNode = svgDocument.createTextNode("");
						textElement.appendChild(textNode);
						svgGraphNode.setIterationText(textNode);
					}
				} else if (svgGraphNode.getShape().equals(Shape.RECORD)) {
					addPortBoxes(gElement, polygon, text, lines, svgGraphNode);
				}
				processorMap.put(title, svgGraphNode);
			}
		}
	}

	private void mapEdge(Node node) {
		String title = null;
		SVGOMPathElement path = null;
		SVGOMPolygonElement polygon = null;
		SVGOMEllipseElement ellipse = null;
		SVGOMAElement a = null;
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof SVGOMTitleElement) {
				title = getTitle((SVGOMTitleElement) child);
			} else if (child instanceof SVGOMPolygonElement) {
				polygon = (SVGOMPolygonElement) child;
			} else if (child instanceof SVGOMEllipseElement) {
				ellipse = (SVGOMEllipseElement) child;
			} else if (child instanceof SVGOMPathElement) {
				path = (SVGOMPathElement) child;
			} else if (child instanceof SVGOMAElement) {
				a = (SVGOMAElement) child;
				Node aChild = a.getFirstChild();
				while (aChild != null) {
					if (aChild instanceof SVGOMPolygonElement) {
						polygon = (SVGOMPolygonElement) aChild;
					} else if (aChild instanceof SVGOMEllipseElement) {
						ellipse = (SVGOMEllipseElement) aChild;
					} else if (aChild instanceof SVGOMPathElement) {
						path = (SVGOMPathElement) aChild;
					}
					Node nextChild = aChild.getNextSibling();
					a.removeChild(aChild);
					node.appendChild(aChild);
					aChild = nextChild;
				}
				node.removeChild(a);
			}
			child = child.getNextSibling();
		}
		if (title != null && path != null && a != null
				&& (polygon != null || ellipse != null)) {
			String ports = a.getHref().getBaseVal();
			GraphElement graphElement = graphElementMap.get(title + ports);
			if (graphElement instanceof SVGGraphEdge) {
				SVGGraphEdge svgGraphEdge = (SVGGraphEdge) graphElement;
				svgGraphEdge.setGraphController(this);
				if (polygon != null) {
					svgGraphEdge.setPolygon(polygon);
				} else {
					svgGraphEdge.setEllipse(ellipse);
				}
				svgGraphEdge.setPath(path);
				mapDatalink(title, svgGraphEdge);
			}
		}
	}

	private void mapDatalink(String title, SVGGraphEdge datalink) {
		String sinkProcessor = title.substring(title.indexOf("->") + 2);
		int index = sinkProcessor.indexOf("WORKFLOWINTERNALSOURCE_");
		if (index > 0) {
			sinkProcessor = sinkProcessor.substring(0, index);
		}
		if (!datalinkMap.containsKey(sinkProcessor)) {
			datalinkMap.put(sinkProcessor, new ArrayList<SVGGraphEdge>());
		}
		datalinkMap.get(sinkProcessor).add(datalink);
	}

	public void setEdgeActive(String edgeId, boolean active) {
		if (datalinkMap.containsKey(edgeId)) {
			for (GraphEdge datalink : datalinkMap.get(edgeId)) {
				datalink.setActive(active);
			}
		}
	}

	private void addPortBoxes(SVGOMGElement gElement,
			SVGOMPolygonElement polygon, List<SVGOMTextElement> text,
			List<SVGOMPolylineElement> lines, SVGGraphNode svgGraphNode) {
		List<GraphNode> inputs = svgGraphNode.getSinkNodes();
		List<GraphNode> outputs = svgGraphNode.getSourceNodes();
		int ports = Math.max(1, inputs.size()) + Math.max(1, outputs.size());
		if (lines.size() == ports && text.size() == ports + 1) {
			SVGPointList polygonPoints = polygon.getPoints();
			Iterator<SVGOMPolylineElement> linesIterator = lines.iterator();
			Iterator<SVGOMTextElement> textIterator = text.iterator();
			if (getAlignment().equals(Alignment.VERTICAL)) {
				if (inputs.size() == 0) {
					linesIterator.next();
					textIterator.next();
				} else {
					Iterator<GraphNode> inputsIterator = inputs.iterator();
					SVGPointList portLine = null;
					while (inputsIterator.hasNext()) {
						SVGGraphNode inputNode = (SVGGraphNode) inputsIterator
								.next();
						inputNode.setGraphController(this);
						SVGPointList lastPortLine = portLine;
						portLine = linesIterator.next().getPoints();
						textIterator.next();
						StringBuilder portPoints = new StringBuilder();
						if (inputs.size() == 1) {// first and only
							portPoints.append(portLine.getItem(0).getX() + ","
									+ portLine.getItem(0).getY() + " ");
							portPoints.append(polygonPoints.getItem(3).getX()
									+ "," + polygonPoints.getItem(3).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(2).getX()
									+ "," + polygonPoints.getItem(2).getY()
									+ " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX() + ","
									+ portLine.getItem(0).getY());
						} else if (lastPortLine == null) {// first
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + portLine.getItem(0).getY() + " ");
							portPoints.append(polygonPoints.getItem(3).getX()
									+ "," + polygonPoints.getItem(3).getY()
									+ " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX() + ","
									+ portLine.getItem(0).getY() + " ");
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + portLine.getItem(0).getY());
						} else if (inputsIterator.hasNext()) {// mid
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX() + ","
									+ portLine.getItem(0).getY() + " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY());
						} else {// last
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ lastPortLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY());
						}
						SVGOMPolygonElement portPolygon = createPolygon(portPoints
								.toString());
						gElement.insertBefore(portPolygon, null);
						inputNode.setPolygon(portPolygon);
					}
				}
				svgGraphNode.setText(textIterator.next());
				if (outputs.size() > 0) {
					Iterator<GraphNode> outputsIterator = outputs.iterator();
					SVGPointList portLine = linesIterator.next().getPoints();
					while (outputsIterator.hasNext()) {
						SVGGraphNode outputNode = (SVGGraphNode) outputsIterator
								.next();
						outputNode.setGraphController(this);
						SVGPointList lastPortLine = portLine;
						if (linesIterator.hasNext()) {
							portLine = linesIterator.next().getPoints();
						} else {
							portLine = null;
						}
						textIterator.next();
						StringBuilder portPoints = new StringBuilder();
						if (outputs.size() == 1) {// first and
							// only
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + polygonPoints.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(1).getX()
									+ "," + polygonPoints.getItem(1).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + polygonPoints.getItem(0).getY());
						} else if (outputs.indexOf(outputNode) == 0) {// first
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + polygonPoints.getItem(0).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX() + ","
									+ portLine.getItem(0).getY() + " ");
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + polygonPoints.getItem(0).getY());
						} else if (portLine == null) {// last
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(1).getX()
									+ "," + polygonPoints.getItem(1).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY());
						} else {// mid
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX() + ","
									+ portLine.getItem(0).getY() + " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY());
						}
						SVGOMPolygonElement portPolygon = createPolygon(portPoints
								.toString());
						gElement.insertBefore(portPolygon, null);
						outputNode.setPolygon(portPolygon);
					}
				}
			} else {
				svgGraphNode.setText(textIterator.next());
				SVGPointList portLine = linesIterator.next().getPoints();
				if (inputs.size() == 0) {
					portLine = linesIterator.next().getPoints();
					textIterator.next();
				} else {
					Iterator<GraphNode> inputsIterator = inputs.iterator();
					while (inputsIterator.hasNext()) {
						SVGGraphNode inputNode = (SVGGraphNode) inputsIterator
								.next();
						inputNode.setGraphController(this);
						SVGPointList lastPortLine = portLine;
						portLine = linesIterator.next().getPoints();
						textIterator.next();
						StringBuilder portPoints = new StringBuilder();
						if (inputs.size() == 1) {// first and only
							portPoints.append(polygonPoints.getItem(0).getX() + ","
									+ polygonPoints.getItem(0).getY() + " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(portLine.getItem(1).getX()
									+ "," + portLine.getItem(1).getY()
									+ " ");
							portPoints.append(portLine.getItem(0).getX() + ","
									+ portLine.getItem(0).getY() + " ");
							portPoints.append(polygonPoints.getItem(0).getX() + ","
									+ polygonPoints.getItem(0).getY());
						} else if (inputs.indexOf(inputNode) == 0) {// first
							portPoints.append(portLine.getItem(0).getX()
									+ "," + portLine.getItem(0).getY() + " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ lastPortLine.getItem(0).getY() + " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX()
									+ "," + portLine.getItem(0).getY());
						} else if (inputsIterator.hasNext()) {// mid
							portPoints.append(portLine.getItem(0).getX()
									+ "," + portLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX() + ","
									+ lastPortLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX()
									+ "," + portLine.getItem(0).getY());
						} else {// last
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + polygonPoints.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX() + ","
									+ lastPortLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX() + ","
									+ portLine.getItem(0).getY() + " ");
							portPoints.append(polygonPoints.getItem(0).getX()
									+ "," + polygonPoints.getItem(0).getY());
						}
						SVGOMPolygonElement portPolygon = createPolygon(portPoints
								.toString());
						gElement.insertBefore(portPolygon, null);
						inputNode.setPolygon(portPolygon);
					}
				}
				if (outputs.size() > 0) {
					Iterator<GraphNode> outputsIterator = outputs.iterator();
					while (outputsIterator.hasNext()) {
						SVGGraphNode outputNode = (SVGGraphNode) outputsIterator
								.next();
						outputNode.setGraphController(this);
						SVGPointList lastPortLine = portLine;
						if (linesIterator.hasNext()) {
							portLine = linesIterator.next().getPoints();
						} else {
							portLine = null;
						}
						textIterator.next();
						StringBuilder portPoints = new StringBuilder();
						if (outputs.size() == 1) {// first and only
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(1).getX()
									+ "," + polygonPoints.getItem(1).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY());
						} else if (outputs.indexOf(outputNode) == 0) {// first
							portPoints.append(portLine.getItem(0).getX()
									+ "," + portLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ lastPortLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX()
									+ "," + portLine.getItem(0).getY());
						} else if (portLine == null) {// last
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + polygonPoints.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX()
									+ "," + lastPortLine.getItem(1).getY()
									+ " ");
							portPoints.append(polygonPoints.getItem(1).getX()
									+ "," + polygonPoints.getItem(1).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + polygonPoints.getItem(0).getY());
						} else {// mid
							portPoints.append(portLine.getItem(0).getX()
									+ "," + portLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(0).getX()
									+ "," + lastPortLine.getItem(0).getY()
									+ " ");
							portPoints.append(lastPortLine.getItem(1).getX() + ","
									+ lastPortLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(1).getX() + ","
									+ portLine.getItem(1).getY() + " ");
							portPoints.append(portLine.getItem(0).getX()
									+ "," + portLine.getItem(0).getY());
						}
						SVGOMPolygonElement portPolygon = createPolygon(portPoints
								.toString());
						gElement.insertBefore(portPolygon, null);
						outputNode.setPolygon(portPolygon);
					}
					
				}
			}
		} else {
			logger.debug("Sanity check failed when adding ports for "
					+ svgGraphNode);
		}
	}

	private SVGOMPolygonElement createPolygon(String points) {
		SVGOMPolygonElement polygon = (SVGOMPolygonElement) svgDocument
				.createElementNS(SVGUtil.svgNS, SVGConstants.SVG_POLYGON_TAG);
		polygon.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE,
				"fill:none;stroke:none;");
		polygon.setAttribute("pointer-events", "fill");
		polygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, points);

		return polygon;
	}

	private SVGOMPolygonElement createProgressPolygon() {
		SVGOMPolygonElement polygon = (SVGOMPolygonElement) svgDocument
		.createElementNS(SVGUtil.svgNS, SVGConstants.SVG_POLYGON_TAG);
		polygon.setAttribute(
				SVGConstants.SVG_POINTS_ATTRIBUTE, "");
		polygon.setAttribute(
				SVGConstants.SVG_FILL_ATTRIBUTE,
				SVGGraphComponent.COMPLETED_COLOUR);
		polygon.setAttribute(
				SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE,
				"0.8");
//		completedBox.setAttribute(
//		SVGConstants.SVG_STROKE_ATTRIBUTE,
//		"black");
//		completedBox.setAttribute(
//		SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE,
//		"0.6");
		return polygon;
	}

	private Element createIterationTextElement(SVGPoint iterationPosition) {
		Element text = svgDocument.createElementNS(SVGUtil.svgNS, SVGConstants.SVG_TEXT_TAG);
		text.setAttribute(SVGConstants.SVG_X_ATTRIBUTE,
				String.valueOf(iterationPosition.getX() - 1.5));
		text.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE,
				String.valueOf(iterationPosition.getY() + 5.5));
		text.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, "end");
		text.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "5.5");
		text.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		return text;
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
	
	private String getTitle(SVGOMTitleElement titleElement) {
		String title = null;
		Object titleElementChild = titleElement.getFirstChild();
		if (titleElementChild instanceof GenericText) {
			GenericText textElement = (GenericText) titleElementChild;
			title = textElement.getData();
		}
		return title;
	}

}

class EdgeLine {

	private static final float arrowLength = 10f;

	private static final float arrowWidth = 3f;

	private Element line;

	private Element pointer;
	
	private SVGGraphController graphController;

	private EdgeLine(SVGGraphController graphController) {
		this.graphController = graphController;
	}

	public static EdgeLine createAndAdd(SVGDocument svgDocument, SVGGraphController graphController) {
		EdgeLine edgeLine = new EdgeLine(graphController);
		edgeLine.line = svgDocument.createElementNS(SVGUtil.svgNS,
				SVGConstants.SVG_LINE_TAG);
		edgeLine.line.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE,
				"fill:none;stroke:black");
		edgeLine.line.setAttribute("pointer-events", "none");
		edgeLine.line.setAttribute("visibility", "hidden");
		edgeLine.line.setAttribute(SVGConstants.SVG_X1_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVGConstants.SVG_Y1_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVGConstants.SVG_X2_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVGConstants.SVG_Y2_ATTRIBUTE, "0");

		edgeLine.pointer = svgDocument.createElementNS(SVGUtil.svgNS,
				SVGConstants.SVG_POLYGON_TAG);
		edgeLine.pointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE,
				"fill:black;stroke:black");
		edgeLine.pointer.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, "0,0 "
				+ -arrowLength + "," + arrowWidth + " " + -arrowLength + ","
				+ -arrowWidth + " 0,0");
		edgeLine.pointer.setAttribute("pointer-events", "none");
		edgeLine.pointer.setAttribute("visibility", "hidden");

		Element svgRoot = svgDocument.getDocumentElement();
		svgRoot.insertBefore(edgeLine.line, null);
		svgRoot.insertBefore(edgeLine.pointer, null);

		return edgeLine;
	}

	public void setSourcePoint(final Point point) {
		UpdateManager updateManager = this.graphController.updateManager;
		if (updateManager != null) {
			updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							line.setAttribute(SVGConstants.SVG_X1_ATTRIBUTE, String.valueOf(point
									.getX()));
							line.setAttribute(SVGConstants.SVG_Y1_ATTRIBUTE, String.valueOf(point
									.getY()));

							float x = Float.parseFloat(line
									.getAttribute(SVGConstants.SVG_X2_ATTRIBUTE));
							float y = Float.parseFloat(line
									.getAttribute(SVGConstants.SVG_Y2_ATTRIBUTE));
							double angle = SVGUtil.calculateAngle(line);

							pointer.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
									+ x + " " + y + ") rotate(" + angle + " 0 0) ");
						}
					});
		}
	}

	public void setTargetPoint(final Point point) {
		UpdateManager updateManager = this.graphController.updateManager;
		if (updateManager != null) {
			updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							line.setAttribute(SVGConstants.SVG_X2_ATTRIBUTE, String.valueOf(point
									.getX()));
							line.setAttribute(SVGConstants.SVG_Y2_ATTRIBUTE, String.valueOf(point
									.getY()));

							double angle = SVGUtil.calculateAngle(line);
							pointer.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
									+ point.x + " " + point.y + ") rotate(" + angle + " 0 0) ");
						}
					});
		}
	}

	public void setColour(final Color colour) {
		UpdateManager updateManager = this.graphController.updateManager;
		if (updateManager != null) {
			updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							String hexColour = SVGUtil.getHexValue(colour);
							line.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:"
									+ hexColour + ";");
							pointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:"
									+ hexColour + ";stroke:" + hexColour + ";");
						}
					});
		}
	}

	public void setVisible(final boolean visible) {
		UpdateManager updateManager = this.graphController.updateManager;
		if (updateManager != null) {
			updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							if (visible) {
								line.setAttribute("visibility", "visible");
								pointer.setAttribute("visibility", "visible");
							} else {
								line.setAttribute("visibility", "hidden");
								pointer.setAttribute("visibility", "hidden");
							}
						}
					});
		}
	}
}
