package net.sf.taverna.t2.workbench.views.graph;

import java.awt.Color;
import java.awt.GridLayout;
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

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.models.graph.DotWriter;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.GraphSelectionMessage;

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
import org.embl.ebi.escience.scuflui.shared.StreamDevourer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 * An SVG graph view of a Dataflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class SVGGraph extends JComponent implements Observer<GraphSelectionMessage> {

	private static Logger logger = Logger.getLogger(SVGGraph.class);

	static final String COMPLETED_COLOUR = "grey";

	private static final String OUTPUT_COLOUR = "blue";

	static final String ERROR_COLOUR = "red";

	static final String SELECTED_COLOUR = "blue";

	private static final int OUTPUT_FLASH_PERIOD = 200;

	static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

	private static Timer timer = new Timer(true);
	
	GraphController graphController;

	SVGDocument svgDocument;

	private JSVGCanvas svgCanvas;

	UpdateManager updateManager;
	
	private Map<String, GraphElement> graphElementMap = new HashMap<String, GraphElement>();
	
	private Map<GraphElement, SVGElement> elementToProcessorMap = new HashMap<GraphElement, SVGElement>();

	private Map<String, SVGNode> processorMap = new HashMap<String, SVGNode>();

	private Map<String, List<SVGEdge>> datalinkMap = new HashMap<String, List<SVGEdge>>();

	private static SAXSVGDocumentFactory docFactory = null;

	static {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		logger.info("Using XML parser " + parser);
		docFactory = new SAXSVGDocumentFactory(parser);
	}

	/**
	 * Constructs a new instance of SVGDiagram.
	 * 
	 */
	public SVGGraph(GraphController graphController) {
		this.graphController = graphController;
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

	public void setGraph(Graph graph) {
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
			System.out.println("Mapping edge:" + edge.getId());
		}		
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
									title = title.substring(8);//, title.lastIndexOf(textData));
								}
							}
						}
						GraphElement graphElement = graphElementMap.get(title);
//						System.out.println("looking for node: "+title);
//						System.out.println("  found : "+graphElement);
						SVGNode svgProcessor = new SVGNode(this, graphElement, gElement,
								polygon, text, nested);
						processorMap.put(title, svgProcessor);
						elementToProcessorMap.put(graphElement, svgProcessor);
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
						GraphElement graphElement = graphElementMap.get(title);
						System.out.println("looking for edge : "+title);
						System.out.println("  found : "+graphElement);

						SVGEdge datalink = new SVGEdge(this, graphElement, path, polygon);
						elementToProcessorMap.put(graphElement, datalink);
						mapDatalink(title, datalink);
					}
				}
			} else {
				mapNodes(node.getChildNodes());
			}
		}
	}

	private void mapDatalink(String title, SVGEdge datalink) {
		String sinkProcessor = title.substring(title.indexOf("->") + 2);
		int index = sinkProcessor.indexOf("WORKFLOWINTERNALSOURCE_");
		if (index > 0) {
			sinkProcessor = sinkProcessor.substring(0, index);
		}
		if (!datalinkMap.containsKey(sinkProcessor)) {
			datalinkMap.put(sinkProcessor, new ArrayList<SVGEdge>());
		}
		datalinkMap.get(sinkProcessor).add(datalink);
	}

	/**
	 * Resets the diagram to its original appearance.
	 * 
	 */
	public void reset() {
		for (SVGNode node : processorMap.values()) {
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
			for (SVGEdge datalink : datalinkMap.get(datalinkId)) {
				datalink.setColour(OUTPUT_COLOUR);
			}
			timer.schedule(new TimerTask() {
				public void run() {
					for (SVGEdge datalink : datalinkMap.get(datalinkId)) {
						datalink.resetStyle();
					}
				}
			}, OUTPUT_FLASH_PERIOD);
		}
	}

	public void setSVGDocument(SVGDocument svgDocument) {
		updateManager = null;
		processorMap.clear();
		datalinkMap.clear();
		
		mapNodes(svgDocument.getChildNodes());
		svgCanvas.setSVGDocument(svgDocument);
	}
	
	public JSVGCanvas getSvgCanvas() {
		return svgCanvas;
	}

	public static SVGDocument getSVG(String dotText) throws IOException {
		// FIXME: Should use MyGridConfiguration.getProperty(), 
		// but that would not include the system property
		// specified at command line on Windows (runme.bat) 
		// and OS X (Taverna.app)
		String dotLocation = System.getProperty("taverna.dotlocation");
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

	public void notify(Observable<GraphSelectionMessage> sender,
			GraphSelectionMessage message) throws Exception {
		GraphElement element = message.getElement();
		SVGElement svgElement = elementToProcessorMap.get(element);
		if (svgElement != null) {
			svgElement.setSelected(message.getType().equals(GraphSelectionMessage.Type.ADDED));
		} else {
			System.out.println(element + " not found");
		}
	}

}
