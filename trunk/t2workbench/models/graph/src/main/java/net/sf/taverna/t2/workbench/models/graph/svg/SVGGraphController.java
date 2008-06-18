package net.sf.taverna.t2.workbench.models.graph.svg;

import java.awt.Component;
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
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
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
		
	private SVGDocument svgDocument;
	
	private JSVGCanvas svgCanvas = new JSVGCanvas();
	
	private Element edgeLine;
	
	private Element edgePointer;
	
	UpdateManager updateManager;
	
	public SVGGraphController(Dataflow dataflow, JComponent xcomponent) {
		super(dataflow, new SVGGraphModelFactory(), xcomponent);

		svgCanvas = new JSVGCanvas();
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				updateManager = svgCanvas.getUpdateManager();
			}
		});
		
	}

	@Override
	public Component getComponent() {
		return svgCanvas;
	}

	public void redraw() {
		Graph graph = generateGraph();
		graphElementMap.clear();
		mapGraphElements(graph);
		try {
			StringWriter stringWriter = new StringWriter();
			DotWriter dotWriter = new DotWriter(stringWriter);
			dotWriter.writeGraph(graph);
			setSVGDocument(SVGUtil.getSVG(stringWriter.toString()));
		} catch (IOException e) {
			logger.error("Couldn't generate svg", e);
		}
	}
	
	public void setSVGDocument(SVGDocument svgDocument) {
		this.svgDocument = svgDocument;
		updateManager = null;
		processorMap.clear();
		datalinkMap.clear();

		addEdgeLine(svgDocument);
		mapNodes(svgDocument.getChildNodes());
		svgCanvas.setSVGDocument(svgDocument);
	}
	
	public void addEdgeLine(SVGDocument svgDocument) {
		edgeLine = svgDocument.createElementNS(SVGUtil.svgNS, SVGConstants.SVG_LINE_TAG);
		edgeLine.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:black");
		edgeLine.setAttribute("pointer-events", "none");
		edgeLine.setAttribute("visibility", "hidden");
		edgePointer = svgDocument.createElementNS(SVGUtil.svgNS, SVGConstants.SVG_ELLIPSE_TAG);
		edgePointer.setAttribute(SVGConstants.SVG_RX_ATTRIBUTE, "3");
		edgePointer.setAttribute(SVGConstants.SVG_RY_ATTRIBUTE, "3");
		edgePointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:black;stroke:black");
		edgePointer.setAttribute("pointer-events", "none");
		edgePointer.setAttribute("visibility", "hidden");

        Element svgRoot = svgDocument.getDocumentElement();
        svgRoot.insertBefore(edgeLine, null);		
        svgRoot.insertBefore(edgePointer, null);		
	}

	public void startEdgeCreation(GraphElement graphElement, Point point) {
		super.startEdgeCreation(graphElement, point);
		if (edgeCreationFromSource || edgeCreationFromSink) {
			edgeLine.setAttribute(SVGConstants.SVG_X1_ATTRIBUTE, String.valueOf(point.getX()));
			edgeLine.setAttribute(SVGConstants.SVG_Y1_ATTRIBUTE, String.valueOf(point.getY()));
			edgeLine.setAttribute(SVGConstants.SVG_X2_ATTRIBUTE, String.valueOf(point.getX()));
			edgeLine.setAttribute(SVGConstants.SVG_Y2_ATTRIBUTE, String.valueOf(point.getY()));
			edgePointer.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, String.valueOf(point.getX()));
			edgePointer.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, String.valueOf(point.getY()));
			edgeLine.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:black");
			edgePointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:black;stroke:black");
//			edgeLine.setAttribute("visibility", "visible");
//			edgePointer.setAttribute("visibility", "visible");
		}
	}
	
	public boolean moveEdgeCreationTarget(GraphElement graphElement, Point point) {
		boolean linkValid = super.moveEdgeCreationTarget(graphElement, point);
		if (edgeCreationFromSink) {
			edgeLine.setAttribute(SVGConstants.SVG_X1_ATTRIBUTE, String.valueOf(point.getX()));							
			edgeLine.setAttribute(SVGConstants.SVG_Y1_ATTRIBUTE, String.valueOf(point.getY()));
			if (linkValid) {
				edgeLine.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:green");
				edgePointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:green;stroke:green");
			} else {
				edgeLine.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:black");
				edgePointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:black;stroke:black");
			}
			edgeLine.setAttribute("visibility", "visible");
			edgePointer.setAttribute("visibility", "visible");
		} else if (edgeCreationFromSource) {
			edgeLine.setAttribute(SVGConstants.SVG_X2_ATTRIBUTE, String.valueOf(point.getX()));							
			edgeLine.setAttribute(SVGConstants.SVG_Y2_ATTRIBUTE, String.valueOf(point.getY()));							
			edgePointer.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, String.valueOf(point.getX()));
			edgePointer.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, String.valueOf(point.getY()));
			if (linkValid) {
				edgeLine.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:green");
				edgePointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:green;stroke:green");
			} else {
				edgeLine.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:black");
				edgePointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:black;stroke:black");
			}
			edgeLine.setAttribute("visibility", "visible");
			edgePointer.setAttribute("visibility", "visible");
		}
		return linkValid;
	}

	public void stopEdgeCreation(GraphElement graphElement, Point point) {
		super.stopEdgeCreation(graphElement, point);
		edgeLine.setAttribute("visibility", "hidden");
		edgePointer.setAttribute("visibility", "hidden");
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
			graphElementMap.put(edge.getId(), edge);
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
//			} else if (child instanceof SVGOMPolygonElement) {
//				polygon = (SVGOMPolygonElement) child;
			}
			child = child.getNextSibling();
		}
		if (title != null/* && polygon != null*/) {
			GraphElement graphElement = graphElementMap.get(title);
			if (graphElement instanceof SVGGraph) {
				SVGGraph svgGraph = (SVGGraph) graphElement;
				svgGraph.setGraphController(this);
				svgGraph.setG(gElement);
//				svgGraph.setPolygon(polygon);
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
					List<GraphNode> inputs = svgGraphNode.getSinkNodes();
					List<GraphNode> outputs = svgGraphNode.getSourceNodes();
					int ports = inputs.size() + outputs.size();
					if (lines.size() == Math.max(2, ports) && text.size() == Math.max(3, ports + 1)) {
//						String polygonStyle = polygon.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
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
									SVGGraphNode inputNode = (SVGGraphNode) inputsIterator.next();
									inputNode.setGraphController(this);
									SVGPointList lastPortLine = portLine;
									portLine = linesIterator.next().getPoints();
									textIterator.next();
									StringBuilder portPoints = new StringBuilder();
									if (inputs.size() == 1) {//first and only
										portPoints.append(portLine.getItem(0).getX()+","+portLine.getItem(0).getY()+" ");
										portPoints.append(polygonPoints.getItem(3).getX()+","+polygonPoints.getItem(3).getY()+" ");
										portPoints.append(polygonPoints.getItem(2).getX()+","+polygonPoints.getItem(2).getY()+" ");
										portPoints.append(portLine.getItem(1).getX()+","+portLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(0).getX()+","+portLine.getItem(0).getY());										
									} else if (lastPortLine == null) {//first
										portPoints.append(polygonPoints.getItem(0).getX()+","+portLine.getItem(0).getY()+" ");
										portPoints.append(polygonPoints.getItem(3).getX()+","+polygonPoints.getItem(3).getY()+" ");
										portPoints.append(portLine.getItem(1).getX()+","+portLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(0).getX()+","+portLine.getItem(0).getY()+" ");
										portPoints.append(polygonPoints.getItem(0).getX()+","+portLine.getItem(0).getY());										
									} else if (inputsIterator.hasNext()) {//mid
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY()+" ");
										portPoints.append(lastPortLine.getItem(1).getX()+","+lastPortLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(1).getX()+","+portLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(0).getX()+","+portLine.getItem(0).getY()+" ");
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY());
									} else {//last
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY()+" ");
										portPoints.append(lastPortLine.getItem(1).getX()+","+lastPortLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(1).getX()+","+lastPortLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(1).getX()+","+portLine.getItem(1).getY()+" ");
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY());
									}
									SVGOMPolygonElement portPolygon = createPolygon(portPoints.toString());
									gElement.insertBefore(portPolygon, null);
									inputNode.setPolygon(portPolygon);
								}
							}
							svgGraphNode.setText(textIterator.next());
							if (outputs.size() > 0) {
								Iterator<GraphNode> outputsIterator = outputs.iterator();
								SVGPointList portLine = linesIterator.next().getPoints();
								while (outputsIterator.hasNext()) {
									SVGGraphNode outputNode = (SVGGraphNode) outputsIterator.next();
									outputNode.setGraphController(this);
									SVGPointList lastPortLine = portLine;
									if (linesIterator.hasNext()) {
										portLine = linesIterator.next().getPoints();
									} else {
										portLine = null;
									}
									textIterator.next();
									StringBuilder portPoints = new StringBuilder();
									if (outputs.size() == 1) {//first and only
										portPoints.append(polygonPoints.getItem(0).getX()+","+polygonPoints.getItem(0).getY()+" ");
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY()+" ");
										portPoints.append(lastPortLine.getItem(1).getX()+","+lastPortLine.getItem(1).getY()+" ");
										portPoints.append(polygonPoints.getItem(1).getX()+","+polygonPoints.getItem(1).getY()+" ");
										portPoints.append(polygonPoints.getItem(0).getX()+","+polygonPoints.getItem(0).getY());										
									} else if (outputs.indexOf(outputNode) == 0) {//first
										portPoints.append(polygonPoints.getItem(0).getX()+","+polygonPoints.getItem(0).getY()+" ");
										portPoints.append(polygonPoints.getItem(0).getX()+","+portLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(1).getX()+","+portLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(0).getX()+","+portLine.getItem(0).getY()+" ");
										portPoints.append(polygonPoints.getItem(0).getX()+","+polygonPoints.getItem(0).getY());										
									} else if (portLine == null) {//last
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY()+" ");
										portPoints.append(lastPortLine.getItem(1).getX()+","+lastPortLine.getItem(1).getY()+" ");
										portPoints.append(polygonPoints.getItem(1).getX()+","+lastPortLine.getItem(1).getY()+" ");
										portPoints.append(polygonPoints.getItem(1).getX()+","+polygonPoints.getItem(1).getY()+" ");
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY());										
									} else {//mid
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY()+" ");
										portPoints.append(lastPortLine.getItem(1).getX()+","+lastPortLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(1).getX()+","+portLine.getItem(1).getY()+" ");
										portPoints.append(portLine.getItem(0).getX()+","+portLine.getItem(0).getY()+" ");
										portPoints.append(lastPortLine.getItem(0).getX()+","+lastPortLine.getItem(0).getY());										
									}
									SVGOMPolygonElement portPolygon = createPolygon(portPoints.toString());
									gElement.insertBefore(portPolygon, null);
									outputNode.setPolygon(portPolygon);
								}
							}
						} else {
							
						}
					} else {
						logger.debug("Sanity check failed when adding ports for " + title);
					}
				}
				processorMap.put(title, svgGraphNode);
			}
		}
	}
	
	private SVGOMPolygonElement createPolygon(String points) {
		SVGOMPolygonElement polygon = (SVGOMPolygonElement) svgDocument.createElementNS(SVGUtil.svgNS, SVGConstants.SVG_POLYGON_TAG);
		polygon.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:none;");
		polygon.setAttribute("pointer-events", "fill");
		polygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, points);

        return polygon;
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
		if (title != null && path != null && (polygon != null || ellipse != null)) {
			GraphElement graphElement = graphElementMap.get(title);
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

	private String getTitle(SVGOMTitleElement titleElement) {
		String title = null;
		Object titleElementChild = titleElement
				.getFirstChild();
		if (titleElementChild instanceof GenericText) {
			GenericText textElement = (GenericText) titleElementChild;
			title = textElement.getData();
		}
		return title;
	}

}
