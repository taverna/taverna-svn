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

/**
 * 
 * PropertiedGraphNodeImpl is an implementation of the PropertiedGraphNode
 * interface.
 * 
 * @author alanrw
 * 
 * @param <O>
 *            The class of Object within the PropertiedObjectSet of which the
 *            containing PropertiedgraphView is a view.
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
		this.edges = new HashSet<PropertiedGraphEdge<O>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addEdge(final PropertiedGraphEdge<O> edge) {
		if (edge == null) {
			throw new NullPointerException("edge cannot be null"); //$NON-NLS-1$
		}
		this.edges.add(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertiedGraphEdge<O>> getEdges() {
		// Copy to be safe
		return new HashSet<PropertiedGraphEdge<O>>(this.edges);
	}

	/**
	 * {@inheritDoc}
	 */
	public O getObject() {
		return this.object;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeEdge(final PropertiedGraphEdge<O> edge) {
		if (edge == null) {
			throw new NullPointerException("edge cannot be null"); //$NON-NLS-1$
		}
		this.edges.remove(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setObject(final O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null"); //$NON-NLS-1$
		}
		if (this.object != null) {
			throw new IllegalStateException(
					"object cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.object = object;
	}

}
