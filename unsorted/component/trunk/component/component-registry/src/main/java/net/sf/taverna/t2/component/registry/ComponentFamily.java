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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.component.profile.BaseProfile;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A ComponentFamily is a collection of Components that share the same
 * ComponentProfile.
 *
 * @author David Withers
 */
public abstract class ComponentFamily {
	
	private ComponentRegistry parentRegistry;
	
	private String name;
	
	private String description;
	
	private ComponentProfile componentProfile;
	
	protected Map<String, Component> componentCache = new HashMap<String, Component>();

	public ComponentFamily(ComponentRegistry componentRegistry) {
		this.parentRegistry = componentRegistry;
	}

	/**
	 * Returns the ComponentRegistry that contains this ComponentFamily.
	 *
	 * @return the ComponentRegistry that contains this ComponentFamily.
	 */
	public ComponentRegistry getComponentRegistry() {
		return parentRegistry;
	}

	/**
	 * Returns the name of the ComponentFamily.
	 *
	 * @return the name of the ComponentFamily.
	 */
	public final synchronized String getName() {
		if (name == null) {
			name = internalGetName();
		}
		return name;
	}

	protected abstract String internalGetName();

	/**
	 * Returns the description of the ComponentFamily.
	 *
	 * @return the description of the ComponentFamily.
	 */
	public final synchronized String getDescription() {
		if (description == null) {
			description = internalGetDescription();
		}
		return description;
	}
	
	protected abstract String internalGetDescription();

	/**
	 * Returns the ComponentProfile for this ComponentFamily.
	 *
	 * @return the ComponentProfile for this ComponentFamily.
	 * @throws ComponentRegistryException
	 */
	public final synchronized ComponentProfile getComponentProfile() throws ComponentRegistryException {
		if (componentProfile == null) {
			componentProfile = internalGetComponentProfile();
		}
		if (componentProfile != null) {
			ComponentProfile baseProfile = BaseProfile.getInstance().getProfile();
			if ((baseProfile != null) && componentProfile.getName().equals(baseProfile.getName())) {
				return baseProfile;
			}
		}
		return componentProfile;
	}

	protected abstract ComponentProfile internalGetComponentProfile() throws ComponentRegistryException;

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
	public final List<Component> getComponents() throws ComponentRegistryException {
		checkComponentCache();
		return new ArrayList<Component>(componentCache.values());
	}
	
	private void checkComponentCache() throws ComponentRegistryException {
		synchronized(componentCache) {
			if (componentCache.isEmpty()) {
				populateComponentCache();
			}
		}
	}

	protected abstract void populateComponentCache() throws ComponentRegistryException;

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
	public final Component getComponent(String componentName) throws ComponentRegistryException {
		checkComponentCache();
		return componentCache.get(componentName);
	}

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
	public final ComponentVersion createComponentBasedOn(String componentName, String description, Dataflow dataflow)
			throws ComponentRegistryException {
		if (componentName == null) {
			throw new ComponentRegistryException("Component name must not be null");
		}
		if (dataflow == null) {
			throw new ComponentRegistryException("Dataflow must not be null");
		}
		checkComponentCache();
		if (componentCache.containsKey(componentName)) {
			throw new ComponentRegistryException("Component name already used");
		}
		ComponentVersion version = internalCreateComponentBasedOn(componentName, description, dataflow);
		synchronized(componentCache) {
			Component c = version.getComponent();
			componentCache.put(componentName, c);
		}
		return version;
	}

	protected abstract ComponentVersion internalCreateComponentBasedOn(String componentName, String description, Dataflow dataflow)
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
	public final void removeComponent(Component component)
			throws ComponentRegistryException {
		if (component != null) {
			checkComponentCache();
			synchronized(componentCache) {
				componentCache.remove(component.getName());
			}
			internalRemoveComponent(component);
		}
	}

	protected abstract void internalRemoveComponent (Component component)
	throws ComponentRegistryException;
}
