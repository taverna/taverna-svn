package net.sf.taverna.t2.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A graph representation of a dataflow.
 * 
 * @author David Withers
 */
public class Graph extends Element {

	public enum Alignment {HORIZONTAL, VERTICAL}
	
	public enum LineStyle {NONE, SOLID, DOTTED}
	
	private List<Node> nodes = new ArrayList<Node>();
	
	private Set<Edge> edges = new HashSet<Edge>();
	
	private Set<Graph> subgraphs = new HashSet<Graph>();

	private Alignment alignment = Alignment.VERTICAL;
	
	/**
	 * Constructs a new instance of Graph.
	 *
	 */
	public Graph() {
	}

	/**
	 * Adds an edge to the graph.
	 * 
	 * @param edge the edge to add
	 */
	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	/**
	 * Adds a node to the graph.
	 * 
	 * @param node the node to add
	 */
	public void addNode(Node node) {
		node.setParent(this);
		nodes.add(node);
	}

	/**
	 * Adds a subgraph to the graph.
	 * 
	 * @param subgraph the subgraph to add
	 */
	public void addSubgraph(Graph subgraph) {
		subgraph.setParent(this);
		subgraphs.add(subgraph);
	}

	/**
	 * Returns the alignment of the graph.
	 *
	 * @return the alignment of the graph
	 */
	public Alignment getAlignment() {
		return alignment;
	}

	/**
	 * Returns the edges contained in the graph.
	 *
	 * @return the edges contained in the graph
	 */
	public Set<Edge> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	/**
	 * Returns the nodes contained in the graph.
	 *
	 * @return the nodes contained in the graph
	 */
	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	
	/**
	 * Returns the subgraphs contained in the graph.
	 *
	 * @return the subgraphs contained in the graph
	 */
	public Set<Graph> getSubgraphs() {
		return Collections.unmodifiableSet(subgraphs);
	}
	
	/**
	 * Removes an edge from the graph.
	 * 
	 * @param edge the edge to remove
	 * @return true if the edge is removed from the graph
	 */
	public boolean removeEdge(Edge edge) {
		return edges.remove(edge);
	}

	/**
	 * Removes a node from the graph.
	 * 
	 * @param node the node to remove
	 * @return true if the node is removed from the graph
	 */
	public boolean removeNode(Node node) {
		return nodes.remove(node);
	}
	
	/**
	 * Removes a subgraph from the graph.
	 * 
	 * @param subgraph the subgraph to remove
	 * @return true if the subgraph is removed from the graph
	 */
	public boolean removeSubgraph(Graph subgraph) {
		return subgraphs.remove(subgraph);
	}
	
	/**
	 * Sets the alignment of the graph.
	 *
	 * @param alignment the new alignment
	 */
	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

}
