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

import static java.util.Collections.synchronizedSortedMap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A Component is a building block for creating Taverna workflows. Components
 * and must comply with the ComponentProfile of their ComponentFamily.
 * 
 * @author David Withers
 */
public abstract class Component implements
		net.sf.taverna.t2.component.api.Component {
	private String name;
	private String description;
	private URL url;
	/**
	 * Mapping from version numbers to version implementations.
	 */
	protected SortedMap<Integer, Version> versionMap = new TreeMap<Integer, Version>();

	protected Component(URL url) {
		this.url = url;
	}
	
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

	@Override
	public final synchronized String getName() {
		if (name == null)
			name = internalGetName();
		return name;
	}

	/**
	 * The real implementation of the name fetching. Caching already handled.
	 * 
	 * @return The name of the component.
	 */
	protected abstract String internalGetName();

	@Override
	public final synchronized String getDescription() {
		if (description == null)
			description = internalGetDescription();
		return description;
	}

	/**
	 * The real implementation of the description fetching. Caching already
	 * handled.
	 * 
	 * @return The description of the component.
	 */
	protected abstract String internalGetDescription();

	@Override
	public final SortedMap<Integer, Version> getComponentVersionMap() {
		synchronized (versionMap) {
			checkComponentVersionMap();
			return synchronizedSortedMap(versionMap);
		}
	}

	private void checkComponentVersionMap() {
		if (versionMap.isEmpty())
			populateComponentVersionMap();
	}

	/**
	 * Create the contents of the {@link #versionMap} field.
	 */
	protected abstract void populateComponentVersionMap();

	@Override
	public final Version getComponentVersion(Integer version)
			throws RegistryException {
		synchronized (versionMap) {
			checkComponentVersionMap();
			return versionMap.get(version);
		}
	}

	@Override
	public final Version addVersionBasedOn(Dataflow dataflow,
			String revisionComment) throws RegistryException {
		Version result = internalAddVersionBasedOn(dataflow, revisionComment);
		synchronized (versionMap) {
			checkComponentVersionMap();
			versionMap.put(result.getVersionNumber(), result);
		}
		return result;
	}

	/**
	 * Manufacture a new version of a component. Does not add to the overall
	 * version map.
	 * 
	 * @param dataflow
	 *            The definition of the component.
	 * @param revisionComment
	 *            The description of the version.
	 * @return The new version of the component.
	 * @throws RegistryException
	 */
	protected abstract Version internalAddVersionBasedOn(Dataflow dataflow,
			String revisionComment) throws RegistryException;

	@Override
	public final URL getComponentURL() {
		return url;
	}

	@Override
	public void delete() throws RegistryException {
		getFamily().removeComponent(this);
	}
}
