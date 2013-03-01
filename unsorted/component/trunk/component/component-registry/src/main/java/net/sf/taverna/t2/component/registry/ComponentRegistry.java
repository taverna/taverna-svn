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
import java.util.List;

import net.sf.taverna.t2.component.profile.ComponentProfile;

/**
 * A ComponentRegistry contains ComponentFamilies and ComponentProfiles.
 *
 * @author David Withers
 */
public interface ComponentRegistry {

	/**
	 * Returns all the ComponentFamilies in this ComponetRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain any ComponentFamilies an empty
	 * list is returned.
	 *
	 * @return all the ComponentFamilies in this ComponetRegistry.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public List<ComponentFamily> getComponentFamilies() throws ComponentRegistryException;

	/**
	 * Returns the ComponentFamily with the specified name.
	 * <p>
	 * If this ComponentRegistry does not contain a ComponentFamily with the
	 * specified name <code>null</code> is returned.
	 *
	 * @param familyName
	 *            the name of the ComponentFamily to return. Must not be null.
	 * @return the ComponentFamily with the specified name in this
	 *         ComponentRepository or null if none exists.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public ComponentFamily getComponentFamily(String familyName) throws ComponentRegistryException;

	/**
	 * Creates a new ComponentFamily and adds it to this ComponentRegistry.
	 *
	 * @param familyName
	 *            the name of the ComponentFamily to create. Must not be null.
	 * @param componentProfile
	 *            the ComponentProfile for the new ComponentFamily. Must not be
	 *            null.
	 * @param sharingPolicy 
	 * 			  the SharingPolicy to use for the new ComponentFamily.
	 * @return the new ComponentFamily
	 * @throws ComponentRegistryException
	 *             <ul>
	 *             <li>if familyName is null,
	 *             <li>if componentProfile is null,
	 *             <li>if a ComponentFamily with this name already exists,
	 *             <li>if there is a problem accessing the ComponentRegistry.
	 *             </ul>
	 */
	public ComponentFamily createComponentFamily(String familyName,
			ComponentProfile componentProfile, String description, SharingPolicy sharingPolicy) throws ComponentRegistryException;

	/**
	 * Removes a the ComponentFamily with the specified name from this
	 * ComponentRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain a ComponentFamily with the
	 * specified name this method has no effect.
	 *
	 * @param componentFamily
	 *            the ComponentFamily to remove.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public void removeComponentFamily(ComponentFamily componentFamily)
			throws ComponentRegistryException;

	/**
	 * Returns the location of this ComponentRepository.
	 *
	 * @return the location of this ComponentRepository
	 */
	public URL getRegistryBase();

	/**
	 * Returns all the ComponentProfiles in this ComponetRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain any ComponentProfiles an empty
	 * list is returned.
	 *
	 * @return all the ComponentProfiles in this ComponetRegistry.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public List<ComponentProfile> getComponentProfiles() throws ComponentRegistryException;

	/**
	 * Adds a ComponentProfile to this ComponentRegistry.
	 *
	 * @param componentProfile
	 *            the ComponentProfile to add. Must not be null.
	 * @return the ComponentProfile added to this ComponentRegistry.
	 * @throws ComponentRegistryException
	 *             <ul>
	 *             <li>if componentProfile is null,
	 *             <li>if there is a problem accessing the ComponentRegistry.
	 *             </ul>
	 */
	public ComponentProfile addComponentProfile(ComponentProfile componentProfile)
			throws ComponentRegistryException;

	/**
	 * @return The list of permissions available to the registry
	 * 
	 * @throws ComponentRegistryException
	 */
	public List<SharingPolicy> getPermissions() throws ComponentRegistryException;
}
