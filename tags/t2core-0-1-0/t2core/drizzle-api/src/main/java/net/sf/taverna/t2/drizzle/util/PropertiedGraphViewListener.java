/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import net.sf.taverna.t2.util.beanable.Beanable;


/**
 * @author alanrw
 * 
 * @param <O>
 *            The class of Object in the PropertiedGraphSet of which the
 *            PropertiedGraphView which is being listened to is a view.
 */
public interface PropertiedGraphViewListener<O extends Beanable<?>> {

	/**
	 * Hear that an edge connected to a node has been added to the specified
	 * PropertiedGraphView.
	 * 
	 * @param view The PropertiedGraphView.
	 * @param edge The edge that is now connected to the node
	 * @param node The node to which a new connection has been made
	 */
	void edgeAdded(final PropertiedGraphView<O> view,
			final PropertiedGraphEdge<O> edge, final PropertiedGraphNode<O> node);

	/**
	 * Hear that the connection of an edge to a node has been removed.
	 * 
	 * @param view The PropertiedGraphView
	 * @param edge The edge that is no longer connected to the node
	 * @param node The node from which a connection has been removed
	 */
	void edgeRemoved(final PropertiedGraphView<O> view,
			final PropertiedGraphEdge<O> edge, final PropertiedGraphNode<O> node);

	/**
	 * Hear that a node has been added to the specified PropertiedGraphView
	 * 
	 * @param view The PropertiedGraphView
	 * @param node The node that has been added
	 */
	void nodeAdded(final PropertiedGraphView<O> view,
			final PropertiedGraphNode<O> node);

	/**
	 * Hear that a node has been removed from the specified PropertiedGraphView
	 * 
	 * @param view The PropertiedGraphView
	 * @param node The node that has been removed
	 */
	void nodeRemoved(final PropertiedGraphView<O> view,
			final PropertiedGraphNode<O> node);
}
