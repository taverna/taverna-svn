package net.sf.taverna.t2.workbench.models.graph;

/**
 * Factory for creating graph elements.
 * 
 * @author David Withers
 */
public interface GraphModelFactory {

	public Graph createGraph(GraphEventManager eventManager);
	
	public GraphNode createGraphNode(GraphEventManager eventManager);
	
	public GraphEdge createGraphEdge(GraphEventManager eventManager);
	
}
