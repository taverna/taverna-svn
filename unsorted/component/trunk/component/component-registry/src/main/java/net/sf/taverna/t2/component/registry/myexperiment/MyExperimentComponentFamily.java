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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentComponentFamily implements ComponentFamily {

	private static Logger logger = Logger.getLogger(MyExperimentComponentFamily.class);

	private final MyExperimentComponentRegistry componentRegistry;
	private final String uri;

	private String name;
	private ComponentProfile componentProfile;
	private List<Component> components;

	public MyExperimentComponentFamily(MyExperimentComponentRegistry componentRegistry, String uri) {
		this.componentRegistry = componentRegistry;
		this.uri = uri;
	}

	@Override
	public ComponentRegistry getComponentRegistry() {
		return componentRegistry;
	}

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
	public ComponentProfile getComponentProfile() throws ComponentRegistryException {
		if (componentProfile == null) {
			for (Element internalPackItem : componentRegistry.getResourceElements(uri,
					"internal-pack-items")) {
				String itemUri = internalPackItem.getAttributeValue("uri");
				for (Element tag : componentRegistry.getResourceElements(itemUri, "tags")) {
					String tagText = tag.getTextTrim();
					if (tagText == "component profile") {
						try {
							componentProfile = new ComponentProfile(new URL(itemUri));
						} catch (MalformedURLException e) {
							logger.warn("URL for component profile is invalid : " + itemUri, e);
						}
					}
				}
			}
		}
		return componentProfile;
	}

	@Override
	public List<Component> getComponents() throws ComponentRegistryException {
		if (components == null) {
			components = new ArrayList<Component>();
			for (Element internalPackItem : componentRegistry.getResourceElements(uri,
					"internal-pack-items")) {
				String itemUri = internalPackItem.getAttributeValue("uri");
				for (Element tag : componentRegistry.getResourceElements(itemUri, "tags")) {
					String tagText = tag.getTextTrim();
					if (tagText == "component") {
						components.add(new MyExperimentComponent(componentRegistry, itemUri));
						break;
					}
				}
			}
		}
		return components;
	}

	@Override
	public ComponentVersion createComponentBasedOn(String componentName, Dataflow dataflow) {
		// TODO Auto-generated method stub
		return null;
	}

}
