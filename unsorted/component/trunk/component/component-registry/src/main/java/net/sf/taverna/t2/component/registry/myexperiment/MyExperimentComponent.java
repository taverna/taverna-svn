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
package net.sf.taverna.t2.component.registry.myexperiment;

import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentComponent implements Component {

	private final MyExperimentComponentRegistry componentRegistry;
	private final String uri;

	private String name;
	private SortedMap<Integer, ComponentVersion> componentVersions;

	public MyExperimentComponent(MyExperimentComponentRegistry componentRegistry, String uri) {
		this.componentRegistry = componentRegistry;
		this.uri = uri;
	}

	@Override
	public String getName() {
		if (name == null) {
			Element titleElement = componentRegistry.getResourceElement(uri, "title");
			if (titleElement == null) {
				name = "";
			}
			name = titleElement.getTextTrim();
		}
		return name;
	}

	@Override
	public SortedMap<Integer, ComponentVersion> getComponentVersionMap() {
		if (componentVersions == null) {
			componentVersions = new TreeMap<Integer, ComponentVersion>();
			for (Element version : componentRegistry.getResourceElements(uri, "versions")) {
				String versionUri = version.getAttributeValue("uri");
				ComponentVersion componentVersion = new MyExperimentComponentVersion(componentRegistry, this, versionUri);
				componentVersions.put(componentVersion.getVersionNumber(), componentVersion);
			}
		}
		return componentVersions;
	}

	@Override
	public ComponentVersion getComponentVersion(Integer versionNumber) {
		return getComponentVersionMap().get(versionNumber);
	}

	@Override
	public ComponentVersion addVersionBasedOn(Dataflow dataflow) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getComponentURL() {
		// TODO Auto-generated method stub
		return null;
	}

}
