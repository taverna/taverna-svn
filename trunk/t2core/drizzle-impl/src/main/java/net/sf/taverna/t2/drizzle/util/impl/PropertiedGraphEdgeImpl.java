/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * PropertiedGraphEdgeImpl is an implementation of the PropertiedGraphEdge
 * interface.
 * 
 * @author alanrw
 * 
 */
public final class PropertiedGraphEdgeImpl<O> implements PropertiedGraphEdge<O> {

	/**
	 * nodes contains the Set of PropertiedGraphNodes that are connected by the
	 * PropertiedGraphEdge.
	 */
	private HashSet<PropertiedGraphNode<O>> nodes;

	/**
	 * key holds the PropertyKey that is shared by the nodes connected by the
	 * edge.
	 */
	private PropertyKey key;

	/**
	 * value holds the PropertyValue that is shared by the nodes connected by
	 * the edge.
	 */
	private PropertyValue value;

	/**
	 * Create an empty PropertiedGraphEdge.
	 */
	public PropertiedGraphEdgeImpl() {
		super();
		nodes = new HashSet<PropertiedGraphNode<O>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge#addNode(net.sf.taverna.t2.drizzle.util.PropertiedGraphNode)
	 */
	public void addNode(final PropertiedGraphNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null");
		}
		nodes.add(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge#getKey()
	 */
	public PropertyKey getKey() {
		return this.key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge#getNodes()
	 */
	public Set<PropertiedGraphNode<O>> getNodes() {
		// Copy to be safe
		return new HashSet<PropertiedGraphNode<O>>(nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge#getValue()
	 */
	public PropertyValue getValue() {
		return this.value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge#removeNode(net.sf.taverna.t2.drizzle.util.PropertiedGraphNode)
	 */
	public void removeNode(final PropertiedGraphNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null");
		}
		nodes.remove(node);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge#setKey(net.sf.taverna.t2.drizzle.util.PropertyKey)
	 */
	public void setKey(PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (this.key != null) {
			throw new IllegalStateException(
					"key cannot be initialized more than once");
		}
		this.key = key;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge#setValue(net.sf.taverna.t2.drizzle.util.PropertyValue)
	 */
	public void setValue(PropertyValue value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		if (this.value != null) {
			throw new IllegalStateException(
					"value cannot be initialized more than once");
		}
		this.value = value;
	}

}
