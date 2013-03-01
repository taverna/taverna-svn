/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.component.registry;

import java.net.URL;
import java.util.SortedMap;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A Component is a building block for creating Taverna workflows. Components
 * and must comply with the ComponentProfile of their ComponentFamily.
 *
 * @author David Withers
 */
public interface Component {

	/**
	 * Returns the name of the Component.
	 *
	 * @return the name of the Component.
	 */
	public String getName();

	/**
	 * Returns the description of the Component.
	 *
	 * @return the description of the Component.
	 */
	public String getDescription();

	/**
	 * Returns a SortedMap of version number to ComponentVersion.
	 * <p>
	 * The returned map is sorted increasing numeric order.
	 *
	 * @return a SortedMap of version number to ComponentVersion.
	 */
	public SortedMap<Integer, ComponentVersion> getComponentVersionMap();

	/**
	 * Returns the ComponentVersion that has the specified version number.
	 *
	 * @param version
	 *            the version number of the ComponentVersion to return.
	 * @return the ComponentVersion that has the specified version number.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public ComponentVersion getComponentVersion(Integer version) throws ComponentRegistryException;

	/**
	 * Creates a new version of this Component.
	 *
	 * @param dataflow
	 *            the Dataflow that the new ComponentVersion will use.
	 * @return a new version of this Component.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public ComponentVersion addVersionBasedOn(Dataflow dataflow, String revisionComment) throws ComponentRegistryException;

	/**
	 * Returns the URL for the Component.
	 *
	 * @return the URL for the Component.
	 */
	public URL getComponentURL();

}
