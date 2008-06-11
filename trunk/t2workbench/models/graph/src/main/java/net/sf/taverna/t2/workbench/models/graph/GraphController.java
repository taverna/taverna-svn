package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.Graph.LineStyle;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge.ArrowStyle;
import net.sf.taverna.t2.workbench.models.graph.GraphNode.Shape;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author David Withers
 */
public class GraphController implements Observer<DataflowSelectionMessage> {

	public enum PortStyle {
		ALL {
			Shape inputShape() {return Shape.INVHOUSE;}
			Shape outputShape() {return Shape.HOUSE;}
			Shape processorShape() {return Shape.RECORD;}
		},
		BOUND {
			Shape inputShape() {return Shape.INVHOUSE;}
			Shape outputShape() {return Shape.HOUSE;}
			Shape processorShape() {return Shape.RECORD;}
		},
		NONE {
			Shape inputShape() {return Shape.BOX;}
			Shape outputShape() {return Shape.BOX;}
			Shape processorShape() {return Shape.BOX;}
		},
		BLOB {
			Shape inputShape() {return Shape.CIRCLE;}
			Shape outputShape() {return Shape.CIRCLE;}
			Shape processorShape() {return Shape.CIRCLE;}
		};
		
		abstract Shape inputShape();

		abstract Shape outputShape();

		abstract Shape processorShape();

		Shape mergeShape() {return Shape.CIRCLE;}

	}
	
	private static Logger logger = Logger.getLogger(GraphController.class);

	private Map<Object, GraphElement> dataflowToGraph = new HashMap<Object, GraphElement>();

	private Map<GraphElement, Object> graphToDataflow = new HashMap<GraphElement, Object>();

	private Map<Port, GraphNode> ports = new HashMap<Port, GraphNode>();

	private Map<Port, Port> nestedDataflowPorts = new HashMap<Port, Port>();
	
	private Map<Graph, GraphNode> inputControls = new HashMap<Graph, GraphNode>();
	
	private Map<Graph, GraphNode> outputControls = new HashMap<Graph, GraphNode>();
	
	private Dataflow dataflow;
	
	private GraphModelFactory graphModelFactory;
	
	private DataflowSelectionModel dataflowSelectionModel;
	
	private GraphEventManager graphEventManager;
	
	//graph settings	
	private PortStyle portStyle = PortStyle.NONE;
	
	private Alignment alignment = Alignment.VERTICAL;
	
	private boolean expandNestedDatflows = true;

	private boolean showMerges = true;

	public GraphController(Dataflow dataflow, GraphModelFactory graphModelFactory) {
		this.dataflow = dataflow;
		this.graphModelFactory = graphModelFactory;
		dataflowSelectionModel = DataflowSelectionManager.getInstance().getDataflowSelectionModel(dataflow);
		dataflowSelectionModel.addObserver(this);
		graphEventManager = new GraphEventManager(dataflowSelectionModel);
	}
	
	public Graph generateGraph() {
		return generateGraph(dataflow, "", "dataflow_graph", 0);
	}
	
	public void setSelected(GraphElement graphElement, boolean selected) {
		Object dataflowElement = graphToDataflow.get(graphElement);
		if (dataflowElement != null) {
			dataflowSelectionModel.addSelection(dataflowElement);
		} else {
			logger.debug("Selection failed : no dataflow element found for graph element " + graphElement.getId());
		}
	}
	
	public Graph generateGraph(Dataflow dataflow, String prefix, String name, int depth) {
		Graph graph = graphModelFactory.createGraph();
		graph.setId(prefix + name);
		graph.setAlignment(getAlignment());
		if (getPortStyle().equals(PortStyle.BLOB)) {
			graph.setLabel("");
		} else {
			graph.setLabel(name);
		}
		graph.setFillColor(GraphColorManager.getSubGraphFillColor(depth));
		graph.setLineStyle(LineStyle.SOLID);

		//processors
		for (Processor processor : dataflow.getProcessors()) {
			graph.addNode(generateProcessorNode(processor, prefix, depth));
		}

		//merges
		for (Merge merge : dataflow.getMerges()) {
			if (showMerges) {
				graph.addNode(generateMergeNode(merge, prefix));				
			} else {
				Port sinkPort = null;
				for (Datalink datalink : merge.getOutputPort().getOutgoingLinks()) {
					sinkPort = datalink.getSink();
					break;
				}
				if (sinkPort != null) {
					if (nestedDataflowPorts.containsKey(sinkPort)) {
						sinkPort = nestedDataflowPorts.get(sinkPort);
					}
					GraphNode sinkNode = ports.get(sinkPort);
					for (MergeInputPort inputPort : merge.getInputPorts()) {
						ports.put(inputPort, sinkNode);
					}					
				}
			}
		}
		
		//dataflow outputs
		List<? extends DataflowOutputPort> outputPorts = dataflow.getOutputPorts();
		if (outputPorts.size() > 0) {
			graph.addSubgraph(generateOutputsGraph(outputPorts, prefix, graph));
		}

		//dataflow inputs
		List<? extends DataflowInputPort> inputPorts = dataflow.getInputPorts();
		if (inputPorts.size() > 0) {
			graph.addSubgraph(generateInputsGraph(inputPorts, prefix, graph));
		}

		//datalinks
		for (Datalink datalink : dataflow.getLinks()) {
			GraphEdge edge = generateDatalinkEdge(datalink);
			if (edge != null) {
				graph.addEdge(edge);
			}
		}

		//conditions
		for (Processor processor : dataflow.getProcessors()) {
			GraphElement element = dataflowToGraph.get(processor);
			if (element instanceof GraphNode) {
				GraphNode sink = (GraphNode) element;
				for (Condition condition : processor.getPreconditionList()) {
					GraphEdge edge = generateControlEdge(condition, sink);
					if (edge != null) {
						graph.addEdge(edge);
					}
				}
			}
		}
		
		return graph;
	}

	private GraphEdge generateControlEdge(Condition condition, GraphNode sink) {
		GraphEdge edge = null;
		GraphElement element = dataflowToGraph.get(condition.getControl());
		if (element instanceof GraphNode) {
			GraphNode source = (GraphNode) element;
			if (source != null && sink != null) {
				edge = graphModelFactory.createGraphEdge();
				if (source.isExpanded()) {
					edge.setSource(outputControls.get(source.getGraph()));
				} else {
					edge.setSource(source);
				}
				if (sink.isExpanded()) {
					edge.setSink(inputControls.get(sink.getGraph()));
				} else {
					edge.setSink(sink);
				}
				String sourceId = edge.getSource().getId();
				String sinkId = edge.getSink().getId();
//				if (source.getParent() instanceof GraphNode) {
//					sourceId = source.getParent().getId();
//				}
//				if (sink.getParent() instanceof GraphNode) {
//					sinkId = sink.getParent().getId();
//				}
				edge.setId(sourceId + "->" + sinkId);
				edge.setLineStyle(LineStyle.SOLID);
				edge.setColor(Color.decode("#c0c0c0"));
				edge.setArrowHeadStyle(ArrowStyle.ODOT);
				edge.setDataflowObject(condition);
				edge.setEventManager(graphEventManager);
				dataflowToGraph.put(condition, edge);
				graphToDataflow.put(edge, condition);
			}
		}
		return edge;
	}

	private GraphEdge generateDatalinkEdge(Datalink datalink) {
		GraphEdge edge = null;
		Port sourcePort = datalink.getSource();
		Port sinkPort = datalink.getSink();
		if (nestedDataflowPorts.containsKey(sourcePort)) {
			sourcePort = nestedDataflowPorts.get(sourcePort);
		}
		if (nestedDataflowPorts.containsKey(sinkPort)) {
			sinkPort = nestedDataflowPorts.get(sinkPort);
		}
		GraphNode sourceNode = ports.get(sourcePort);
		GraphNode sinkNode = ports.get(sinkPort);
		if (sourceNode != null && sinkNode != null) {
			edge = graphModelFactory.createGraphEdge();
			edge.setSource(sourceNode);
			edge.setSink(sinkNode);

			String sourceId = sourceNode.getId();
			String sinkId = sinkNode.getId();
			if (sourceNode.getParent() instanceof GraphNode) {
				sourceId = sourceNode.getParent().getId();
			}
			if (sinkNode.getParent() instanceof GraphNode) {
				sinkId = sinkNode.getParent().getId();
			}
			edge.setId(sourceId + "->" + sinkId);
			edge.setLineStyle(LineStyle.SOLID);
			edge.setDataflowObject(datalink);
			edge.setEventManager(graphEventManager);
			dataflowToGraph.put(datalink, edge);
			graphToDataflow.put(edge, datalink);
		}
		return edge;
	}

	private Graph generateInputsGraph(List<? extends DataflowInputPort> inputPorts, String prefix, Graph graph) {
		Graph inputs = graphModelFactory.createGraph();
		inputs.setId(prefix + "sources");
		inputs.setLineStyle(LineStyle.DOTTED);
		if (getPortStyle().equals(PortStyle.BLOB)) {
			inputs.setLabel("");
		} else {
			inputs.setLabel("Workflow Inputs");
		}

		GraphNode triangle = graphModelFactory.createGraphNode();
		triangle.setId(prefix + "WORKFLOWINTERNALSOURCECONTROL");
		triangle.setLabel("");
		triangle.setShape(Shape.TRIANGLE);
		triangle.setWidth(0.2f);
		triangle.setHeight(0.2f);
		triangle.setFillColor(Color.decode("#ff4040"));
		inputs.addNode(triangle);
		inputControls.put(graph, triangle);

		for (DataflowInputPort inputPort : inputPorts) {
			GraphNode inputNode = graphModelFactory.createGraphNode();
			inputNode.setId(prefix + "WORKFLOWINTERNALSOURCE_"+ inputPort.getName());
			if (getPortStyle().equals(PortStyle.BLOB)) {
				inputNode.setLabel("");
				inputNode.setWidth(0.3f);
				inputNode.setHeight(0.3f);
			} else {
				inputNode.setLabel(inputPort.getName());
			}
			inputNode.setShape(getPortStyle().inputShape());
			inputNode.setFillColor(Color.decode("#87ceeb"));
			inputNode.setDataflowObject(inputPort);
			inputNode.setEventManager(graphEventManager);
			ports.put(inputPort.getInternalOutputPort(), inputNode);
			dataflowToGraph.put(inputPort, inputNode);
			graphToDataflow.put(inputNode, inputPort);
			inputs.addNode(inputNode);
		}
		return inputs;
	}

	private Graph generateOutputsGraph(List<? extends DataflowOutputPort> outputPorts, String prefix, Graph graph) {
		Graph outputs = graphModelFactory.createGraph();
		outputs.setId(prefix + "sinks");
		outputs.setLineStyle(LineStyle.DOTTED);
		if (getPortStyle().equals(PortStyle.BLOB)) {
			outputs.setLabel("");
		} else {
			outputs.setLabel("Workflow Outputs");
		}

		GraphNode triangle = graphModelFactory.createGraphNode();
		triangle.setId(prefix + "WORKFLOWINTERNALSINKCONTROL");
		triangle.setLabel("");
		triangle.setShape(Shape.INVTRIANGLE);
		triangle.setWidth(0.2f);
		triangle.setHeight(0.2f);
		triangle.setFillColor(Color.decode("#66cd00"));
		outputs.addNode(triangle);
		outputControls.put(graph, triangle);

		for (DataflowOutputPort outputPort : outputPorts) {
			GraphNode outputNode = graphModelFactory.createGraphNode();
			outputNode.setId(prefix + "WORKFLOWINTERNALSINK_"+ outputPort.getName());
			if (getPortStyle().equals(PortStyle.BLOB)) {
				outputNode.setLabel("");
				outputNode.setWidth(0.3f);
				outputNode.setHeight(0.3f);
			} else {
				outputNode.setLabel(outputPort.getName());
			}
			outputNode.setShape(getPortStyle().outputShape());
			outputNode.setFillColor(Color.decode("#bcd2ee"));
			outputNode.setDataflowObject(outputPort);
			outputNode.setEventManager(graphEventManager);
			ports.put(outputPort.getInternalInputPort(), outputNode);
			dataflowToGraph.put(outputPort, outputNode);
			graphToDataflow.put(outputNode, outputPort);
			outputs.addNode(outputNode);
		}
		return outputs;
	}

	private GraphNode generateMergeNode(Merge merge, String prefix) {
		GraphNode node = graphModelFactory.createGraphNode();
		node.setId(prefix + merge.getLocalName());
		node.setLabel("");
		node.setShape(getPortStyle().mergeShape());
		node.setWidth(0.2f);
		node.setHeight(0.2f);
		node.setFillColor(Color.decode("#4f94cd"));
		node.setDataflowObject(merge);
		node.setEventManager(graphEventManager);

		dataflowToGraph.put(merge, node);
		graphToDataflow.put(node, merge);

		for (MergeInputPort inputPort : merge.getInputPorts()) {
			GraphNode portNode = graphModelFactory.createGraphNode();
			portNode.setId("i" + inputPort.getName());
			portNode.setLabel(inputPort.getName());
			ports.put(inputPort, portNode);
			node.addSinkNode(portNode);
		}

		OutputPort outputPort = merge.getOutputPort();
		GraphNode portNode = graphModelFactory.createGraphNode();
		portNode.setId("o" + outputPort.getName());
		portNode.setLabel(outputPort.getName());
		ports.put(outputPort, portNode);
		node.addSourceNode(portNode);
		return node;
	}

	private GraphNode generateProcessorNode(Processor processor, String prefix, int depth) {
		//Blatantly ignoring any other activities for now
		Activity<?> firstActivity = processor.getActivityList().get(0);

		GraphNode node = graphModelFactory.createGraphNode();
		node.setId(prefix + processor.getLocalName());
		if (getPortStyle().equals(PortStyle.BLOB)) {
			node.setLabel("");
			node.setWidth(0.3f);
			node.setHeight(0.3f);
		} else {
			node.setLabel(processor.getLocalName());
		}
		node.setShape(getPortStyle().processorShape());
		node.setFillColor(GraphColorManager.getFillColor(firstActivity));
		node.setDataflowObject(processor);
		node.setEventManager(graphEventManager);

		dataflowToGraph.put(processor, node);
		graphToDataflow.put(node, processor);

		if (expandNestedDatflows) {

			if (firstActivity.getConfiguration() instanceof Dataflow) {
				Dataflow subDataflow = (Dataflow) firstActivity.getConfiguration();
				Graph subGraph = generateGraph(subDataflow, node.getId(), processor.getLocalName(), depth + 1);
				subGraph.setDataflowObject(processor);
				subGraph.setEventManager(graphEventManager);
//				dataflowToGraph.put(processor, subGraph);
				graphToDataflow.put(subGraph, processor);
				node.setGraph(subGraph);
				node.setExpanded(true);

				Map<String, String> inputPortMapping = firstActivity.getInputPortMapping();
				for (Port processorPort : processor.getInputPorts()) {
					String activityPortName = inputPortMapping.get(processorPort.getName());
					for (DataflowInputPort dataflowPort : subDataflow.getInputPorts()) {
						if (activityPortName.equals(dataflowPort.getName())) {
							nestedDataflowPorts.put(processorPort, dataflowPort.getInternalOutputPort());
							break;
						}
					}
				}

				Map<String, String> outputPortMapping = firstActivity.getOutputPortMapping();
				for (Port processorPort : processor.getOutputPorts()) {
					String activityPortName = outputPortMapping.get(processorPort.getName());
					for (DataflowOutputPort dataflowPort : subDataflow.getOutputPorts()) {
						if (activityPortName.equals(dataflowPort.getName())) {
							nestedDataflowPorts.put(processorPort, dataflowPort.getInternalInputPort());
							break;
						}
					}
				}

			} 
		}

		for (ProcessorInputPort inputPort : processor.getInputPorts()) {
			GraphNode portNode = graphModelFactory.createGraphNode();
			portNode.setId("i" + inputPort.getName());
			portNode.setLabel(inputPort.getName());
			ports.put(inputPort, portNode);
			node.addSinkNode(portNode);
		}

		for (ProcessorOutputPort outputPort : processor.getOutputPorts()) {
			GraphNode portNode = graphModelFactory.createGraphNode();
			portNode.setId("o" + outputPort.getName());
			portNode.setLabel(outputPort.getName());
			ports.put(outputPort, portNode);
			node.addSourceNode(portNode);
		}
		return node;
	}

	/**
	 * Returns the alignment.
	 *
	 * @return the alignment
	 */
	public Alignment getAlignment() {
		return alignment;
	}

	/**
	 * Returns the portStyle.
	 *
	 * @return the portStyle
	 */
	public PortStyle getPortStyle() {
		return portStyle;
	}
	
	/**
	 * Sets the alignment.
	 *
	 * @param alignment the new alignment
	 */
	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}
	
	/**
	 * Sets the portStyle.
	 *
	 * @param style the new portStyle
	 */
	public void setPortStyle(PortStyle portStyle) {
		this.portStyle = portStyle;
	}

	public void resetSelection() {
		for (Object dataflowElement : dataflowSelectionModel.getSelection()) {
			GraphElement graphElement = dataflowToGraph.get(dataflowElement);
			if (graphElement != null) {
				graphElement.setSelected(true);
			}		
		}
	}

	public void notify(Observable<DataflowSelectionMessage> sender,
			DataflowSelectionMessage message) throws Exception {
		GraphElement graphElement = dataflowToGraph.get(message.getElement());
		if (graphElement != null) {
			graphElement.setSelected(message.getType().equals(DataflowSelectionMessage.Type.ADDED));
		}		
	}

	/**
	 * Returns the dataflowSelectionModel.
	 *
	 * @return the dataflowSelectionModel
	 */
	public DataflowSelectionModel getDataflowSelectionModel() {
		return dataflowSelectionModel;
	}

}

