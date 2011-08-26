package net.sf.taverna.t2.workbench.models.graph.svg;

import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.GraphModelFactory;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;

public class SVGGraphModelFactory implements GraphModelFactory {

	public GraphEdge createGraphEdge(GraphEventManager eventManager) {
		return new SVGGraphEdge(eventManager);
	}

	public Graph createGraph(GraphEventManager eventManager) {
		return new SVGGraph(eventManager);
	}

	public GraphNode createGraphNode(GraphEventManager eventManager) {
		return new SVGGraphNode(eventManager);
	}

}
