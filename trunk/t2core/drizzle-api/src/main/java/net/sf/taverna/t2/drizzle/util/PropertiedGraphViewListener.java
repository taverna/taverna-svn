/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

/**
 * @author alanrw
 * 
 */
public interface PropertiedGraphViewListener<O> {
	void edgeAdded(final PropertiedGraphView<O> view,
			final PropertiedGraphEdge<O> edge, final PropertiedGraphNode<O> node);
	void edgeRemoved(final PropertiedGraphView<O> view,
			final PropertiedGraphEdge<O> edge, final PropertiedGraphNode<O> node);
	void nodeAdded(final PropertiedGraphView<O> view, final PropertiedGraphNode<O> node);
	void nodeRemoved(final PropertiedGraphView<O> view, final PropertiedGraphNode<O> node);}
