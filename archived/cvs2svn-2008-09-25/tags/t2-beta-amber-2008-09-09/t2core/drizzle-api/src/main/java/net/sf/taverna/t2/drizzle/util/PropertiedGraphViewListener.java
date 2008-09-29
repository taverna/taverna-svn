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
