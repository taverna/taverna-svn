package net.sf.taverna.t2.workbench.models.graph.svg;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;

import net.sf.taverna.t2.lang.io.StreamDevourer;
import net.sf.taverna.t2.workbench.models.graph.DotWriter;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.ui.impl.configuration.WorkbenchConfiguration;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.GenericText;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
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
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

public class SVGGraphComponent extends JComponent {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SVGGraphComponent.class);

	static final String COMPLETED_COLOUR = "grey";

	private static final String OUTPUT_COLOUR = "blue";

	static final String ERROR_COLOUR = "red";

	static final String SELECTED_COLOUR = "blue";

	private static final int OUTPUT_FLASH_PERIOD = 200;

	static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

	private static Timer timer = new Timer(true);
	
	private Map<String, SVGShape> processorMap = new HashMap<String, SVGShape>();

	private Map<String, List<SVGGraphEdge>> datalinkMap = new HashMap<String, List<SVGGraphEdge>>();

	private JSVGCanvas svgCanvas;

	UpdateManager updateManager;
	
	GraphController graphController;

	private Map<String, GraphElement> graphElementMap = new HashMap<String, GraphElement>();
	
	private static SAXSVGDocumentFactory docFactory = null;

	static {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		logger.info("Using XML parser " + parser);
		docFactory = new SAXSVGDocumentFactory(parser);
	}

	public SVGGraphComponent() {
		setLayout(new BorderLayout());
		svgCanvas = new JSVGCanvas();
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		svgCanvas.setOpaque(false);
		add(svgCanvas, BorderLayout.CENTER);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				updateManager = svgCanvas.getUpdateManager();
			}
		});
	}
	
	public void setGraphController(GraphController graphController) {
		this.graphController = graphController;
		redraw();
	}
	
	public void setSVGDocument(SVGDocument svgDocument) {
		updateManager = null;
		processorMap.clear();
		datalinkMap.clear();

		mapNodes(svgDocument.getChildNodes());
		svgCanvas.setSVGDocument(svgDocument);
	}

	/**
	 * Returns the svgCanvas.
	 *
	 * @return the svgCanvas
	 */
	public JSVGCanvas getSvgCanvas() {
		return svgCanvas;
	}
	
	public void redraw() {
		Graph graph = graphController.generateGraph();
		graphElementMap.clear();
		mapGraphElements(graph);
		try {
			StringWriter stringWriter = new StringWriter();
			DotWriter dotWriter = new DotWriter(stringWriter);
			dotWriter.writeGraph(graph);
			setSVGDocument(getSVG(stringWriter.toString()));
		} catch (IOException e) {
			logger.error("Couldn't generate svg", e);
		}
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
				svgGraph.setGraphComponent(this);
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
				svgGraphNode.setGraphComponent(this);
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
				svgGraphEdge.setGraphComponent(this);
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

	/**
	 * Resets the diagram to its original appearance.
	 * 
	 */
	public void reset() {
		for (SVGShape node : processorMap.values()) {
			node.setCompleted(0f);
			node.setIteration(0);
			node.setErrors(0);
		}
	}

	public static SVGDocument getSVG(String dotText) throws IOException {
		// FIXME: Should use MyGridConfiguration.getProperty(), 
		// but that would not include the system property
		// specified at command line on Windows (runme.bat) 
		// and OS X (Taverna.app)
		String dotLocation = (String)WorkbenchConfiguration.getInstance().getPropertyMap().get("taverna.dotlocation");
		if (dotLocation == null) {
			dotLocation = "dot";
		}
		logger.debug("Invoking dot...");
		Process dotProcess = Runtime.getRuntime().exec(
				new String[] { dotLocation, "-Tsvg" });
		StreamDevourer devourer = new StreamDevourer(dotProcess
				.getInputStream());
		devourer.start();
		// Must create an error devourer otherwise stderr fills up and the
		// process stalls!
		StreamDevourer errorDevourer = new StreamDevourer(dotProcess
				.getErrorStream());
		errorDevourer.start();
		PrintWriter out = new PrintWriter(dotProcess.getOutputStream(), true);
		out.print(dotText);
		out.flush();
		out.close();
		

		String svgText = devourer.blockOnOutput();
		// Avoid TAV-424, replace buggy SVG outputted by "modern" GraphViz versions.
		// http://www.graphviz.org/bugs/b1075.html
		// Contributed by Marko Ullgren
		svgText = svgText.replaceAll("font-weight:regular","font-weight:normal");
		// Fake URI, just used for internal references like #fish
		return docFactory.createSVGDocument("http://taverna.sf.net/diagram/generated.svg", 
			new StringReader(svgText));
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
			for (SVGGraphEdge datalink : datalinkMap.get(datalinkId)) {
				datalink.setColour(OUTPUT_COLOUR);
			}
			timer.schedule(new TimerTask() {
				public void run() {
					for (SVGGraphEdge datalink : datalinkMap.get(datalinkId)) {
						datalink.resetStyle();
					}
				}
			}, OUTPUT_FLASH_PERIOD);
		}
	}

}
