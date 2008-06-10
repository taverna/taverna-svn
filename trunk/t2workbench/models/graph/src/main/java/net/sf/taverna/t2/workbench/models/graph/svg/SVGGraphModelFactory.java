package net.sf.taverna.t2.workbench.models.graph.svg;

import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphModelFactory;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;

public class SVGGraphModelFactory implements GraphModelFactory {

	public GraphEdge createGraphEdge() {
		return new SVGGraphEdge();
	}

	public Graph createGraphModel() {
		return new SVGGraphModel();
	}

	public GraphNode createGraphNode() {
		return new SVGGraphNode();
	}

}
