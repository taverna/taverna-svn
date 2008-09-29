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

import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * A PropertiedGraphView is a view of a PropertiedObjectSet.
 * 
 * The Objects encapsulated by PropertiedObjects within the PropertiedObjectSet
 * are viewed as PropertiedGraphNodes.
 * 
 * PropertiedGraphEdges connect together PropertiedGraphNodes where the
 * corresponding Objects share a PropertyKey + PropertyValue pair within the
 * context of the PropertiedObjectSet.
 * 
 * @author alanrw
 * 
 * @param <O>
 *            The class of Object in the PropertiedObjectSet of which this is a
 *            view.
 */
public interface PropertiedGraphView<O extends Beanable<?>> {
	/**
	 * Specify the PropertiedObjectSet of which the PropertiedGraphView is a
	 * view.
	 * 
	 * @param propertiedObjectSet
	 */
	void setPropertiedObjectSet(final PropertiedObjectSet<O> propertiedObjectSet);

	/**
	 * Return the PropertiedObjectSet of which the PropertiedGraphView is a
	 * view.
	 * 
	 * @return
	 */
	PropertiedObjectSet<O> getPropertiedObjectSet();

	/**
	 * Return the Set of PropertiedGraphNodes that correspond to the Objects
	 * within the PropertiedObjectSet.
	 * 
	 * @return
	 */
	Set<PropertiedGraphNode<O>> getNodes();

	/**
	 * Return the Set of PropertiedGraphEdges that correspond to Objects within
	 * the PropertiedObjectSet sharing a PropertyKey + PropertyValue pair.
	 * 
	 * @return
	 */
	Set<PropertiedGraphEdge<O>> getEdges();

	/**
	 * Add a PropertiedGraphViewListener that listens to the addition or removal
	 * of a PropertiedGraphNode or PropertiedGraphEdge.
	 * 
	 * @param listener
	 */
	void addListener(final PropertiedGraphViewListener<O> listener);

	/**
	 * Removed a PropertiedGraphViewListener from the PropertiedGraphView.
	 * 
	 * @param listener
	 */
	void removeListener(final PropertiedGraphViewListener<O> listener);

	/**
	 * Replay all the calls necessary to acquant a listener with the current
	 * state of the PropertiedGraphView.
	 * 
	 * Note that the calls are not necessarily the same as created the state.
	 */
	void replayToListener(final PropertiedGraphViewListener<O> listener);

	/**
	 * Return the PropertiedGraphNode that corresponds to the specified Object
	 * in the PropertiedObjectSet.
	 * 
	 * @param object
	 * @return
	 */
	PropertiedGraphNode<O> getNode(O object);

	/**
	 * Return the set of PropertyKey for which there are edges in the
	 * PropertiedGraphView.
	 * 
	 * @return
	 */
	Set<PropertyKey> getKeys();

	/**
	 * Return the set of PropertyValue for which there is an edge for the
	 * PropertyKey + PropertyValue.
	 * 
	 * @param key
	 * @return
	 */
	Set<PropertyValue> getValues(final PropertyKey key);

	/**
	 * Return the PropertiedGraphEdge the connects PropertiedGraphNodes where
	 * the corresponding Objects within the PropertiedObjectSet share the
	 * specified PropertyKey + PropertyValue pair.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	PropertiedGraphEdge<O> getEdge(PropertyKey key, PropertyValue value);
}
