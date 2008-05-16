package net.sf.taverna.t2.graph;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.graph.Edge.ArrowStyle;
import net.sf.taverna.t2.graph.Graph.Alignment;
import net.sf.taverna.t2.graph.Graph.LineStyle;
import net.sf.taverna.t2.graph.Node.Shape;
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
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class GraphController {

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
	
	private Map<TokenProcessingEntity, Node> processors = new HashMap<TokenProcessingEntity, Node>();

	private Map<Port, Node> ports = new HashMap<Port, Node>();

	private Map<Port, Port> nestedDataflowPorts = new HashMap<Port, Port>();

	//graph settings	
	private PortStyle portStyle = PortStyle.NONE;
	
	private Alignment alignment = Alignment.VERTICAL;
	
	private boolean expandNestedDatflows = true;

	private boolean showMerges = true;

	public Graph generateGraph(Dataflow dataflow) {
		return generateGraph(dataflow, "", "", 0);
	}
	
	public Graph generateGraph(Dataflow dataflow, String prefix, String name, int depth) {
		Graph graph = new Graph();
		graph.setId("cluster_" + prefix + name);
		graph.setAlignment(getAlignment());
		if (getPortStyle().equals(PortStyle.BLOB)) {
			graph.setLabel("");
		} else {
			graph.setLabel(name);
		}
		graph.setFillColor(ColorController.getSubGraphFillColor(depth));
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
					Node sinkNode = ports.get(sinkPort);
					for (MergeInputPort inputPort : merge.getInputPorts()) {
						ports.put(inputPort, sinkNode);
					}					
				}
			}
		}
		
		//dataflow outputs
		List<? extends DataflowOutputPort> outputPorts = dataflow.getOutputPorts();
		if (outputPorts.size() > 0) {
			graph.addSubgraph(generateOutputsGraph(outputPorts, prefix));
		}

		//dataflow inputs
		List<? extends DataflowInputPort> inputPorts = dataflow.getInputPorts();
		if (inputPorts.size() > 0) {
			graph.addSubgraph(generateInputsGraph(inputPorts, prefix));
		}

		//datalinks
		for (Datalink datalink : dataflow.getLinks()) {
			Edge edge = generateDatalinkEdge(datalink);
			if (edge != null) {
				graph.addEdge(edge);
			}
		}

		//conditions
		for (Processor processor : dataflow.getProcessors()) {
			Node sink = processors.get(processor);
			for (Condition condition : processor.getPreconditionList()) {
				Edge edge = generateControlEdge(condition, sink);
				if (edge != null) {
					graph.addEdge(edge);
				}
			}
		}
		
		return graph;
	}

	private Edge generateControlEdge(Condition condition, Node sink) {
		Edge edge = null;
		Node source = processors.get(condition.getControl());
		if (source != null && sink != null) {
			edge = new Edge(source, sink);
			edge.setLineStyle(LineStyle.SOLID);
			edge.setColor(Color.decode("#c0c0c0"));
			edge.setArrowHeadStyle(ArrowStyle.ODOT);
		}
		return edge;
	}

	private Edge generateDatalinkEdge(Datalink datalink) {
		Edge edge = null;
		Port sourcePort = datalink.getSource();
		Port sinkPort = datalink.getSink();
		if (nestedDataflowPorts.containsKey(sourcePort)) {
			sourcePort = nestedDataflowPorts.get(sourcePort);
		}
		if (nestedDataflowPorts.containsKey(sinkPort)) {
			sinkPort = nestedDataflowPorts.get(sinkPort);
		}
		Node sourceNode = ports.get(sourcePort);
		Node sinkNode = ports.get(sinkPort);
		if (sourceNode != null && sinkNode != null) {
			edge = new Edge(sourceNode, sinkNode);
			edge.setLineStyle(LineStyle.SOLID);
		}
		return edge;
	}

	private Graph generateInputsGraph(
			List<? extends DataflowInputPort> inputPorts, String prefix) {
		Graph inputs = new Graph();
		inputs.setId("cluster_" + prefix + "sources");
		inputs.setLineStyle(LineStyle.DOTTED);
		if (getPortStyle().equals(PortStyle.BLOB)) {
			inputs.setLabel("");
		} else {
			inputs.setLabel("Workflow Inputs");
		}

		Node triangle = new Node();
		triangle.setId(prefix + "WORKFLOWINTERNALSOURCECONTROL");
		triangle.setLabel("");
		triangle.setShape(Shape.TRIANGLE);
		triangle.setWidth(0.2f);
		triangle.setHeight(0.2f);
		triangle.setFillColor(Color.decode("#ff4040"));
		inputs.addNode(triangle);

		for (DataflowInputPort inputPort : inputPorts) {
			Node inputNode = new Node();
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
			ports.put(inputPort.getInternalOutputPort(), inputNode);
			inputs.addNode(inputNode);
		}
		return inputs;
	}

	private Graph generateOutputsGraph(
			List<? extends DataflowOutputPort> outputPorts, String prefix) {
		Graph outputs = new Graph();
		outputs.setId("cluster_" + prefix + "sinks");
		outputs.setLineStyle(LineStyle.DOTTED);
		if (getPortStyle().equals(PortStyle.BLOB)) {
			outputs.setLabel("");
		} else {
			outputs.setLabel("Workflow Outputs");
		}

		Node triangle = new Node();
		triangle.setId(prefix + "WORKFLOWINTERNALSINKCONTROL");
		triangle.setLabel("");
		triangle.setShape(Shape.INVTRIANGLE);
		triangle.setWidth(0.2f);
		triangle.setHeight(0.2f);
		triangle.setFillColor(Color.decode("#66cd00"));
		outputs.addNode(triangle);

		for (DataflowOutputPort outputPort : outputPorts) {
			Node outputNode = new Node();
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
			ports.put(outputPort.getInternalInputPort(), outputNode);
			outputs.addNode(outputNode);
		}
		return outputs;
	}

	private Node generateMergeNode(Merge merge, String prefix) {
		Node node = new Node();
		node.setId(prefix + merge.getLocalName());
		node.setLabel("");
		node.setShape(getPortStyle().mergeShape());
		node.setWidth(0.2f);
		node.setHeight(0.2f);
		node.setFillColor(Color.decode("#4f94cd"));

		processors.put(merge, node);

		for (MergeInputPort inputPort : merge.getInputPorts()) {
			Node portNode = new Node();
			portNode.setId("i" + inputPort.getName());
			portNode.setLabel(inputPort.getName());
			ports.put(inputPort, portNode);
			node.addSinkNode(portNode);
		}

		OutputPort outputPort = merge.getOutputPort();
		Node portNode = new Node();
		portNode.setId("o" + outputPort.getName());
		portNode.setLabel(outputPort.getName());
		ports.put(outputPort, portNode);
		node.addSourceNode(portNode);
		return node;
	}

	private Node generateProcessorNode(Processor processor, String prefix, int depth) {
		//Blatantly ignoring any other activities for now
		Activity<?> firstActivity = processor.getActivityList().get(0);

		Node node = new Node();
		node.setId(prefix + processor.getLocalName());
		if (getPortStyle().equals(PortStyle.BLOB)) {
			node.setLabel("");
			node.setWidth(0.3f);
			node.setHeight(0.3f);
		} else {
			node.setLabel(processor.getLocalName());
		}
		node.setShape(getPortStyle().processorShape());
		node.setFillColor(ColorController.getFillColor(firstActivity));

		processors.put(processor, node);

		if (expandNestedDatflows) {

			if (firstActivity.getConfiguration() instanceof Dataflow) {
				Dataflow subDataflow = (Dataflow) firstActivity.getConfiguration();
				Graph subGraph = generateGraph(subDataflow, node.getId(), processor.getLocalName(), depth + 1);
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
			Node portNode = new Node();
			portNode.setId("i" + inputPort.getName());
			portNode.setLabel(inputPort.getName());
			ports.put(inputPort, portNode);
			node.addSinkNode(portNode);
		}

		for (ProcessorOutputPort outputPort : processor.getOutputPorts()) {
			Node portNode = new Node();
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

}

