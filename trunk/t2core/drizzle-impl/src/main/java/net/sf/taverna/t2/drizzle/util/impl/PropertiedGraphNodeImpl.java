/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphNode;

/**
 * 
 * PropertiedGraphNodeImpl is an implementation of the PropertiedGraphNode
 * interface.
 * 
 * @author alanrw
 * 
 */
public class PropertiedGraphNodeImpl<O> implements PropertiedGraphNode<O> {

	/**
	 * edges contains the Set of PropertiedGraphEdges that connect the node to
	 * other nodes.
	 */
	private HashSet<PropertiedGraphEdge<O>> edges;

	/**
	 * object holds the Object to which the PropertiedGraphNode corresponds.
	 */
	private O object;

	/**
	 * Create a new PropertiedGraphNode
	 */
	public PropertiedGraphNodeImpl() {
		super();
		edges = new HashSet<PropertiedGraphEdge<O>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphNode#addEdge(net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge)
	 */
	public void addEdge(final PropertiedGraphEdge<O> edge) {
		if (edge == null) {
			throw new NullPointerException("edge cannot be null");
		}
		edges.add(edge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphNode#getEdges()
	 */
	public Set<PropertiedGraphEdge<O>> getEdges() {
		// Copy to be safe
		return new HashSet<PropertiedGraphEdge<O>>(edges);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphNode#getObject()
	 */
	public O getObject() {
		return this.object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphNode#removeEdge(net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge)
	 */
	public void removeEdge(final PropertiedGraphEdge<O> edge) {
		if (edge == null) {
			throw new NullPointerException("edge cannot be null");
		}
		edges.remove(edge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphNode#setObject(java.lang.Object)
	 */
	public void setObject(final O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null");
		}
		if (this.object != null) {
			throw new IllegalStateException(
					"object cannot be initialized more than once");
		}
		this.object = object;
	}

}
