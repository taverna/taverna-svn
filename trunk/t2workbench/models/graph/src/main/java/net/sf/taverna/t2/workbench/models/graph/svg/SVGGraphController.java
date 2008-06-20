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

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.models.graph.DotWriter;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphNode.Shape;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.GenericText;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMPolylineElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.dom.svg.SVGOMTitleElement;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPointList;

public class SVGGraphController extends GraphController {

	private static Logger logger = Logger.getLogger(SVGGraphController.class);

	private Map<String, SVGShape> processorMap = new HashMap<String, SVGShape>();

	private Map<String, List<SVGGraphEdge>> datalinkMap = new HashMap<String, List<SVGGraphEdge>>();

	private Map<String, GraphElement> graphElementMap = new HashMap<String, GraphElement>();

	private Map<String, List<GraphElement>> graphEdgeMap = new HashMap<String, List<GraphElement>>();

	private SVGDocument svgDocument;

	private EdgeLine edgeLine;
	
	UpdateManager updateManager;

	public SVGGraphController(Dataflow dataflow, JComponent xcomponent) {
		super(dataflow, new SVGGraphModelFactory(), xcomponent);
	}

	public SVGDocument generateSVGDocument() {
		svgDocument = null;
		updateManager = null;
		processorMap.clear();
		datalinkMap.clear();

		Graph graph = generateGraph();
		graphElementMap.clear();
		mapGraphElements(graph);
		try {
			StringWriter stringWriter = new StringWriter();
			DotWriter dotWriter = new DotWriter(stringWriter);
			dotWriter.writeGraph(graph);
			svgDocument = SVGUtil.getSVG(stringWriter.toString());
			edgeLine = EdgeLine.createAndAdd(svgDocument);
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
			edgeLine.setSourcePoint(point);
			edgeLine.setTargetPoint(point);
			edgeLine.setColour(Color.BLACK);
			// edgeLine.setVisible(true);
		}
		return started;
	}

	public boolean moveEdgeCreationTarget(GraphElement graphElement, Point point) {
		boolean linkValid = super.moveEdgeCreationTarget(graphElement, point);
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

	public void stopEdgeCreation(GraphElement graphElement, Point point) {
		super.stopEdgeCreation(graphElement, point);
		edgeLine.setVisible(false);
	}

	private void mapGraphElements(Graph graph) {
		graphElementMap.put(graph.getId(), graph);
		for (Graph subgraph : graph.getSubgraphs()) {
			mapGraphElements(subgraph);
		}
		for (GraphNode node : graph.getNodes()) {
			if (node.isExpanded()) {
				mapGraphElements(node.getGraph());
			} else {
				graphElementMap.put(node.getId(), node);
			}
		}
		for (GraphEdge edge : graph.getEdges()) {
			if (!graphEdgeMap.containsKey(edge.getId())) {
				graphEdgeMap.put(edge.getId(), new ArrayList<GraphElement>());
			}
			graphEdgeMap.get(edge.getId()).add(edge);
		}
	}

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
				} else {
					svgGraphNode.setEllipse(ellipse);
				}
				if (svgGraphNode.getShape().equals(Shape.BOX)) {
					if (text.size() == 1) {
						svgGraphNode.setText(text.get(0));
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
			}
			child = child.getNextSibling();
		}
		if (title != null && path != null
				&& (polygon != null || ellipse != null)) {
			List<GraphElement> graphElementList = graphEdgeMap.get(title);
			if (graphElementList.size() > 0) {
				GraphElement graphElement = graphElementList.remove(0);
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

	private EdgeLine() {
	}

	public static EdgeLine createAndAdd(SVGDocument svgDocument) {
		EdgeLine edgeLine = new EdgeLine();
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

	public void setSourcePoint(Point point) {
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

	public void setTargetPoint(Point point) {
		line.setAttribute(SVGConstants.SVG_X2_ATTRIBUTE, String.valueOf(point
				.getX()));
		line.setAttribute(SVGConstants.SVG_Y2_ATTRIBUTE, String.valueOf(point
				.getY()));

		double angle = SVGUtil.calculateAngle(line);
		pointer.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
				+ point.x + " " + point.y + ") rotate(" + angle + " 0 0) ");
	}

	public void setColour(Color colour) {
		String hexColour = SVGUtil.getHexValue(colour);
		line.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:"
				+ hexColour + ";");
		pointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:"
				+ hexColour + ";stroke:" + hexColour + ";");
	}

	public void setVisible(boolean visible) {
		if (visible) {
			line.setAttribute("visibility", "visible");
			pointer.setAttribute("visibility", "visible");
		} else {
			line.setAttribute("visibility", "hidden");
			pointer.setAttribute("visibility", "hidden");
		}
	}
}
