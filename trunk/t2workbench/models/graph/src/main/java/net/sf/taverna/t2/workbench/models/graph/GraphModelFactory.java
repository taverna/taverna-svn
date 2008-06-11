package net.sf.taverna.t2.workbench.models.graph;

/**
 * Factory for creating graph elements.
 * 
 * @author David Withers
 */
public interface GraphModelFactory {

	public Graph createGraph();
	
	public GraphNode createGraphNode();
	
	public GraphEdge createGraphEdge();
	
}
