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

import java.util.List;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A ComponentFamily is a collection of Components that share the same
 * ComponentProfile.
 *
 * @author David Withers
 */
public interface ComponentFamily {

	/**
	 * Returns the ComponentRegistry that contains this ComponentFamily.
	 *
	 * @return the ComponentRegistry that contains this ComponentFamily.
	 */
	public ComponentRegistry getComponentRegistry();

	/**
	 * Returns the name of the ComponentFamily.
	 *
	 * @return the name of the ComponentFamily.
	 */
	public String getName();

	/**
	 * Returns the description of the ComponentFamily.
	 *
	 * @return the description of the ComponentFamily.
	 */
	public String getDescription();

	/**
	 * Returns the ComponentProfile for this ComponentFamily.
	 *
	 * @return the ComponentProfile for this ComponentFamily.
	 * @throws ComponentRegistryException
	 */
	public ComponentProfile getComponentProfile() throws ComponentRegistryException;

	/**
	 * Returns all the Components in this ComponentFamily.
	 * <p>
	 * If this ComponentFamily does not contain any Components an empty list is
	 * returned.
	 *
	 * @return all the Components in this ComponentFamilies.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public List<Component> getComponents() throws ComponentRegistryException;

	/**
	 * Returns the Component with the specified name.
	 * <p>
	 * If this ComponentFamily does not contain a Component with the specified
	 * name <code>null</code> is returned.
	 *
	 * @param componentName
	 *            the name of the Component to return. Must not be null.
	 * @return the Component with the specified name.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public Component getComponent(String componentName) throws ComponentRegistryException;

	/**
	 * Creates a new Component and adds it to this ComponentFamily.
	 *
	 * @param componentName
	 *            the name of the Component to create. Must not be null.
	 * @param dataflow
	 *            the Dataflow for the Component. Must not be null.
	 * @return the new Component.
	 * @throws ComponentRegistryException
	 *             <ul>
	 *             <li>if componentName is null,
	 *             <li>if dataflow is null,
	 *             <li>if a Component with this name already exists,
	 *             <li>if there is a problem accessing the ComponentRegistry.
	 *             </ul>
	 */
	public ComponentVersion createComponentBasedOn(String componentName, Dataflow dataflow)
			throws ComponentRegistryException;
	
	/**
	 * Removes the specified Component from this
	 * ComponentFamily.
	 * <p>
	 * If this ComponentFamily does not contain the Component this method has no effect.
	 *
	 * @param component
	 *            the Component to remove.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public void removeComponent(Component component)
			throws ComponentRegistryException;


}
