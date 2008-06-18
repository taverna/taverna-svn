package net.sf.taverna.t2.workbench.models.graph.svg;

import java.awt.Component;
import java.awt.Point;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.models.graph.DotWriter;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.GenericText;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
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

public class SVGGraphController extends GraphController {

	private static Logger logger = Logger.getLogger(SVGGraphController.class);

	private Map<String, SVGShape> processorMap = new HashMap<String, SVGShape>();

	private Map<String, List<SVGGraphEdge>> datalinkMap = new HashMap<String, List<SVGGraphEdge>>();

	private Map<String, GraphElement> graphElementMap = new HashMap<String, GraphElement>();
		
	private JSVGCanvas svgCanvas = new JSVGCanvas();
	
	private Element edgeLine;
	
	private Element edgePointer;
	
	UpdateManager updateManager;
	
	public SVGGraphController(Dataflow dataflow, JComponent xcomponent) {
		super(dataflow, new SVGGraphModelFactory(), xcomponent);

		svgCanvas = new JSVGCanvas();
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
//		svgCanvas.setOpaque(false);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			@Override
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				updateManager = svgCanvas.getUpdateManager();
			}
		});
		
	}

	@Override
	public Component getComponent() {
		return svgCanvas;
	}

	@Override
	public void redraw() {
		logger.debug("Redrawing graph");
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

	@Override
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
	
	@Override
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

	@Override
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
//		SVGOMPolygonElement polygon = null;
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
		SVGOMTextElement text = null;
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof SVGOMTitleElement) {
				title = getTitle((SVGOMTitleElement) child);
			} else if (child instanceof SVGOMPolygonElement) {
				polygon = (SVGOMPolygonElement) child;
			} else if (child instanceof SVGOMEllipseElement) {
				ellipse = (SVGOMEllipseElement) child;
			} else if (child instanceof SVGOMTextElement) {
				text = (SVGOMTextElement) child;
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
				svgGraphNode.setText(text);
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
