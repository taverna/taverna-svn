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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A Component is a building block for creating Taverna workflows. Components
 * and must comply with the ComponentProfile of their ComponentFamily.
 *
 * @author David Withers
 */
public abstract class Component {
	
	private String name;
	
	private String description;
	
	private URL url;
	
	protected SortedMap<Integer, ComponentVersion> versionMap = new TreeMap<Integer, ComponentVersion>();
	
	protected Component(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			// nothing
		}
	}
	
	protected Component(File fileDir) {
		try {
			this.url = fileDir.toURI().toURL();
		} catch (MalformedURLException e) {
			// nothing
		}
	}

	/**
	 * Returns the name of the Component.
	 *
	 * @return the name of the Component.
	 */
	public final synchronized String getName() {
		if (name == null) {
			name = internalGetName();
		}
		return name;
	}
	
	protected abstract String internalGetName();

	/**
	 * Returns the description of the Component.
	 *
	 * @return the description of the Component.
	 */
	public final synchronized String getDescription() {
		if (description == null) {
			description = internalGetDescription();
		}
		return description;
	}
	
	protected abstract String internalGetDescription();

	/**
	 * Returns a SortedMap of version number to ComponentVersion.
	 * <p>
	 * The returned map is sorted increasing numeric order.
	 *
	 * @return a SortedMap of version number to ComponentVersion.
	 */
	public final SortedMap<Integer, ComponentVersion> getComponentVersionMap() {
		checkComponentVersionMap();
		return versionMap;
	}
	
	private void checkComponentVersionMap() {
		synchronized (versionMap) {
			if (versionMap.isEmpty()) {
				populateComponentVersionMap();
			}
		}
	}
	
	protected abstract void populateComponentVersionMap();

	/**
	 * Returns the ComponentVersion that has the specified version number.
	 *
	 * @param version
	 *            the version number of the ComponentVersion to return.
	 * @return the ComponentVersion that has the specified version number.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public final ComponentVersion getComponentVersion(Integer version) throws ComponentRegistryException {
		checkComponentVersionMap();
		return versionMap.get(version);
	}

	/**
	 * Creates a new version of this Component.
	 *
	 * @param dataflow
	 *            the Dataflow that the new ComponentVersion will use.
	 * @return a new version of this Component.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public final ComponentVersion addVersionBasedOn(Dataflow dataflow, String revisionComment) throws ComponentRegistryException {
		ComponentVersion result = internalAddVersionBasedOn(dataflow, revisionComment);
		checkComponentVersionMap();
		synchronized(versionMap) {
			versionMap.put(result.getVersionNumber(), result);
		}
		return result;
	}

	protected abstract ComponentVersion internalAddVersionBasedOn(Dataflow dataflow, String revisionComment) throws ComponentRegistryException;
	
	/**
	 * Returns the URL for the Component.
	 *
	 * @return the URL for the Component.
	 */
	public final synchronized URL getComponentURL() {
		return url;
	}

}
