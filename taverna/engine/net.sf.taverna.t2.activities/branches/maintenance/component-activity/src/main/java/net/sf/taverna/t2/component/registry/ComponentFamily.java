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

import static net.sf.taverna.t2.component.profile.BaseProfileLocator.getBaseProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A ComponentFamily is a collection of Components that share the same
 * ComponentProfile.
 * 
 * @author David Withers
 */
public abstract class ComponentFamily implements
		net.sf.taverna.t2.component.api.Family {
	private Registry parentRegistry;
	private String name;
	private String description;
	private Profile componentProfile;

	protected Map<String, Component> componentCache = new HashMap<String, Component>();

	public ComponentFamily(Registry componentRegistry) {
		this.parentRegistry = componentRegistry;
	}

	@Override
	public Registry getComponentRegistry() {
		return parentRegistry;
	}

	@Override
	public final synchronized String getName() {
		if (name == null) {
			name = internalGetName();
		}
		return name;
	}

	protected abstract String internalGetName();

	@Override
	public final synchronized String getDescription() {
		if (description == null) {
			description = internalGetDescription();
		}
		return description;
	}

	protected abstract String internalGetDescription();

	@Override
	public final synchronized Profile getComponentProfile()
			throws RegistryException {
		if (componentProfile == null)
			componentProfile = internalGetComponentProfile();
		if (componentProfile == null) {
			Profile baseProfile = getBaseProfile();
			if ((baseProfile != null)
					&& componentProfile.getName().equals(baseProfile.getName()))
				return baseProfile;
		}
		return componentProfile;
	}

	protected abstract Profile internalGetComponentProfile()
			throws RegistryException;

	@Override
	public final List<Component> getComponents() throws RegistryException {
		checkComponentCache();
		return new ArrayList<Component>(componentCache.values());
	}

	private void checkComponentCache() throws RegistryException {
		synchronized (componentCache) {
			if (componentCache.isEmpty())
				populateComponentCache();
		}
	}

	protected abstract void populateComponentCache() throws RegistryException;

	@Override
	public final Component getComponent(String componentName)
			throws RegistryException {
		checkComponentCache();
		return componentCache.get(componentName);
	}

	@Override
	public final Version createComponentBasedOn(String componentName,
			String description, Dataflow dataflow) throws RegistryException {
		if (componentName == null)
			throw new RegistryException("Component name must not be null");
		if (dataflow == null)
			throw new RegistryException("Dataflow must not be null");
		checkComponentCache();
		if (componentCache.containsKey(componentName))
			throw new RegistryException("Component name already used");
		Version version = internalCreateComponentBasedOn(componentName,
				description, dataflow);
		synchronized (componentCache) {
			Component c = version.getComponent();
			componentCache.put(componentName, c);
		}
		return version;
	}

	protected abstract Version internalCreateComponentBasedOn(
			String componentName, String description, Dataflow dataflow)
			throws RegistryException;

	@Override
	public final void removeComponent(Component component)
			throws RegistryException {
		if (component != null) {
			checkComponentCache();
			synchronized (componentCache) {
				componentCache.remove(component.getName());
			}
			internalRemoveComponent(component);
		}
	}

	protected abstract void internalRemoveComponent(Component component)
			throws RegistryException;

	@Override
	public void delete() throws RegistryException {
		getComponentRegistry().removeComponentFamily(this);
	}
}
