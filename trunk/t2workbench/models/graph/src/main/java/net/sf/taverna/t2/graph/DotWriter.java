package net.sf.taverna.t2.graph;

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import net.sf.taverna.t2.graph.Graph.Alignment;
import net.sf.taverna.t2.graph.Node.Shape;

public class DotWriter {

	private static final String EOL = System.getProperty("line.separator");

	private Writer writer;

	public DotWriter(Writer writer) {
		this.writer = writer;
	}

	public void writeGraph(Graph graph) throws IOException {
		writeLine("digraph " + graph.getId() + " {");

		// Overall graph style
		writeLine(" graph [");
		writeLine("  style=\"\"");
		writeLine("  fontsize=\"10\"");
		writeLine("  labeljust=\"left\"");
		writeLine("  clusterrank=\"local\"");
		writeLine("  ranksep=\"0.22\"");
		writeLine("  nodesep=\"0.05\"");
		// Set left to right view if alignment is horizontal
		if (graph.getAlignment().equals(Alignment.HORIZONTAL)) {
			writeLine("  rankdir=\"LR\"");
		}
		writeLine(" ]");

		// Overall node style
		writeLine(" node [");
		writeLine("  fontname=\"Helvetica\"");
		writeLine("  fontsize=\"10\"");
		writeLine("  fontcolor=\"black\"");
		writeLine("  shape=\"record\"");
		writeLine("  height=\"0\"");
		writeLine("  width=\"0\"");
		writeLine("  color=\"black\"");
		writeLine("  fillcolor=\"lightgoldenrodyellow\"");
		writeLine("  style=\"filled\"");
		writeLine(" ];");

		// Overall edge style
		writeLine(" edge [");
		writeLine("  fontname=\"Helvetica\"");
		writeLine("  fontsize=\"8\"");
		writeLine("  fontcolor=\"black\"");
		writeLine("  color=\"black\"");
		writeLine(" ];");

		for(Node node : graph.getNodes()) {
			if (node.isExpanded()) {
				writeSubGraph(node.getGraph(), " ");
			} else {
				writeNode(node, graph.getAlignment(), " ");
			}
		}

		for(Graph subGraph : graph.getSubgraphs()) {
			writeSubGraph(subGraph, " ");
		}

		for(Edge edge : graph.getEdges()) {
			writeEdges(edge, " ");
		}

		writeLine("}");
	}

	private void writeSubGraph(Graph graph, String indent) throws IOException {
		writeLine(indent + "subgraph " + graph.getId() + " {");
//		writeLine(indent + " rank=\"same\"");

		StringBuilder style = new StringBuilder();
		if (graph.getFillColor() != null) {
			writeLine(indent + " fillcolor=\"" + getHexValue(graph.getFillColor()) + "\"");
			style.append("filled");
		}
		if (graph.getLineStyle() != null) {
			style.append(style.length() == 0 ? "" : ",");
			style.append(graph.getLineStyle().toString().toLowerCase());
		}
		writeLine(indent + " style=\"" + style.toString() + "\"");

		if (graph.getLabel() != null) {
			writeLine(indent + " label=\"" + graph.getLabel() + "\"");				
		}

		for(Node node : graph.getNodes()) {
			if (node.isExpanded()) {
				writeSubGraph(node.getGraph(), indent + " ");
			} else {
				writeNode(node, graph.getAlignment(), indent + " ");
			}
		}

		for(Graph subGraph : graph.getSubgraphs()) {
			writeSubGraph(subGraph, indent + " ");
		}

		for(Edge edge : graph.getEdges()) {
			writeEdges(edge, indent + " ");
		}

		writeLine(indent + "}");
	}

	private void writeEdges(Edge edge, String indent) throws IOException {
		Node source = edge.getSource();
		Node sink = edge.getSink();
		String sourceId = "\"" + source.getId() + "\"";
		String sinkId = "\"" + sink.getId() + "\"";
		if (source.getParent() instanceof Node) {
			Node parent = (Node) source.getParent();
			if (parent.getShape().equals(Shape.RECORD)) {
				sourceId = "\"" + parent.getId() + "\":" + sourceId;
			} else {
				sourceId = "\"" + parent.getId() + "\"";
			}
		}
		if (sink.getParent() instanceof Node) {
			Node parent = (Node) sink.getParent();
			if (parent.getShape().equals(Shape.RECORD)) {
				sinkId = "\"" + parent.getId() + "\":" + sinkId;
			} else {
				sinkId = "\"" + parent.getId() + "\"";
			}
		}
		writeLine(indent + sourceId + "->" + sinkId + " [");
		writeLine(indent + " arrowhead=\"" + edge.getArrowHeadStyle().toString().toLowerCase() + "\"");
		writeLine(indent + " arrowtail=\"" + edge.getArrowTailStyle().toString().toLowerCase() + "\"");
		if (edge.getColor() != null) {
			writeLine(indent + " color=\"" + getHexValue(edge.getColor()) + "\"");
		}
		writeLine(indent + "]");
	}

	private void writeNode(Node node, Alignment alignment, String indent) throws IOException {
		writeLine(indent + "\"" + node.getId() + "\" [");

		StringBuilder style = new StringBuilder();
		if (node.getFillColor() != null) {
			writeLine(indent + " fillcolor=\"" + getHexValue(node.getFillColor()) + "\"");
			style.append("filled");
		}
		if (node.getLineStyle() != null) {
			style.append(style.length() == 0 ? "" : ",");
			style.append(node.getLineStyle().toString().toLowerCase());
		}
		writeLine(indent + " style=\"" + style + "\"");

		writeLine(indent + " shape=\"" + node.getShape().toString().toLowerCase() + "\"");
		writeLine(indent + " width=\"" + node.getWidth() + "\"");
		writeLine(indent + " height=\"" + node.getHeight() + "\"");

		if (node.getShape().equals(Shape.RECORD)) {
			StringBuilder labelString = new StringBuilder();
			if (alignment.equals(Alignment.VERTICAL)) {
				labelString.append("{{");
				addNodeLabels(node.getSinkNodes(), labelString);
				labelString.append("}|" + node.getLabel() + "|{");
				addNodeLabels(node.getSourceNodes(), labelString);
				labelString.append("}}");
			} else {
				labelString.append("" + node.getLabel() + "|{{");
				addNodeLabels(node.getSinkNodes(), labelString);
				labelString.append("}|{");
				addNodeLabels(node.getSourceNodes(), labelString);
				labelString.append("}}");
			}
			writeLine(indent + " label=\"" + labelString + "\"");
		} else {
			writeLine(indent + " label=\"" + node.getLabel() + "\"");				
		}

		writeLine(indent + "];");
	}

	private void addNodeLabels(List<Node> nodes, StringBuilder labelString) {
		boolean firstNode = true;
		for (Node node : nodes) {
			if (firstNode) {
				firstNode = false;
			} else {
				labelString.append("|");
			}
			labelString.append("<");
			labelString.append(node.getId());
			labelString.append(">");
			labelString.append(node.getLabel());
		}
	}

	private String getHexValue(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	private void writeLine(String line) throws IOException {
		writer.write(line);
		writer.write(EOL);
	}

}
