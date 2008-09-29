/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.Set;

/**
 * A PropertiedGraphNode encapsulates an Object that is connected by a, possibly
 * empty, Set of PropertiedGraphEdges to other Objects with which it shares a
 * PropertyKey + PropertyValue pair.
 * 
 * @author alanrw
 * 
 * @param <O> The class of Object contained by the PropertiedGraphNode
 */
public interface PropertiedGraphNode<O> {
	/**
	 * Return the Object that is encapsulated by the PropertiedGraphNode.
	 * 
	 * @return
	 */
	O getObject();

	/**
	 * Specify the Object that is encapsulated by the PropertiedGraphNode.
	 * 
	 * @param object
	 */
	void setObject(final O object);

	/**
	 * Return the Set of PropertiedGraphEdges that connect the
	 * PropertiedGraphNode.
	 * 
	 * @return
	 */
	Set<PropertiedGraphEdge<O>> getEdges();

	/**
	 * Add a PropertiedGraphEdge to the Set of edges that connect to the
	 * PropertiedGraphNode.
	 * 
	 * @param edge
	 */
	void addEdge(final PropertiedGraphEdge<O> edge);

	/**
	 * Remove a PropertiedgraphEdge from the Set of edges that connect to the
	 * PropertiedgraphNode.
	 * 
	 * @param edge
	 */
	void removeEdge(final PropertiedGraphEdge<O> edge);
}
