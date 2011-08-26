/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.Set;

/**
 * 
 * A PropertiedGraphEdge connects together a Set of PropertiedGraphNodes that
 * share the same PropertyValue for a specific PropertyKey.
 * 
 * At the moment a PropertiedgraphEdge connects a Set of nodes rather than
 * having an individual edge for each connection between a pair of nodes. This
 * decision may be reviewed.
 * 
 * @author alanrw
 * 
 * @param <O>
 *            The class of Object that are contained by PropertiedGraphNodes
 *            connected by the PropertiedGraphEdge
 */
public interface PropertiedGraphEdge<O> {
	/**
	 * Return the PropertyKey associated with the PropertiedGraphEdge.
	 * 
	 * @return
	 */
	PropertyKey getKey();

	/**
	 * Return the PropertyValue associated with the PropertiedGraphEdge.
	 * 
	 * @return
	 */
	PropertyValue getValue();

	/**
	 * Return the Set of PropertiedGraphNode that share the PropertyKey +
	 * PropertyValue pair.
	 * 
	 * @return
	 */
	Set<PropertiedGraphNode<O>> getNodes();

	/**
	 * Add a PropertiedGraphNode to the set of nodes that are connected by the
	 * edge.
	 * 
	 * @param node
	 */
	void addNode(final PropertiedGraphNode<O> node);

	/**
	 * Remove a PropertiedGraphNode from the set of nodes that are connected by
	 * the edge.
	 * 
	 * @param node
	 */
	void removeNode(final PropertiedGraphNode<O> node);

	/**
	 * Specify the PropertyKey associated with the PropertiedGraphEdge.
	 * 
	 * @param key
	 */
	void setKey(final PropertyKey key);

	/**
	 * Specify the PropertyValue associated with the PropertiedGraphEdge.
	 * 
	 * @param value
	 */
	void setValue(final PropertyValue value);
}
