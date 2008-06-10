package net.sf.taverna.t2.workbench.models.graph;

/**
 * Factory for creating graph model elements.
 * 
 * @author David Withers
 */
public interface GraphModelFactory {

	public Graph createGraphModel();
	
	public GraphNode createGraphNode();
	
	public GraphEdge createGraphEdge();
	
}
