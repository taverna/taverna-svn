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
