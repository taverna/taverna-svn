/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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
 * @param <O> The class of Object encapsulated by nodes connected by the edge.
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
		this.nodes = new HashSet<PropertiedGraphNode<O>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addNode(final PropertiedGraphNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		this.nodes.add(node);
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyKey getKey() {
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertiedGraphNode<O>> getNodes() {
		// Copy to be safe
		return new HashSet<PropertiedGraphNode<O>>(this.nodes);
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyValue getValue() {
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeNode(final PropertiedGraphNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		this.nodes.remove(node);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKey(PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (this.key != null) {
			throw new IllegalStateException(
					"key cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.key = key;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(final PropertyValue value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null"); //$NON-NLS-1$
		}
		if (this.value != null) {
			throw new IllegalStateException(
					"value cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.value = value;
	}

}
