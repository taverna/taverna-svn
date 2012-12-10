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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
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
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentComponentFamily implements ComponentFamily {

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
			Element fileElement = componentRegistry.getInternalPackItem(uri, "file", "component profile");
			String resourceUri = fileElement.getAttributeValue("resource");
			String version = fileElement.getAttributeValue("version");
			String downloadUri = resourceUri + "/download?version=" + version;
			try {
				componentProfile = new ComponentProfile(new URL(downloadUri));
			} catch (MalformedURLException e) {
				throw new ComponentRegistryException("Unable to open profile from " + downloadUri, e);
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
				if (internalPackItem.getName().equals("pack")) {
					String resourceUri = internalPackItem.getAttributeValue("resource");
					Element resource = componentRegistry.getResource(resourceUri + ".xml");
					String packUri = resource.getAttributeValue("uri");
					for (Element tag : componentRegistry.getResourceElements(packUri, "tags")) {
						String tagText = tag.getTextTrim();
						if ("component".equals(tagText)) {
							components.add(new MyExperimentComponent(componentRegistry, packUri));
							break;
						}
					}
				}
			}
		}
		return components;
	}

	@Override
	public ComponentVersion createComponentBasedOn(String componentName, Dataflow dataflow)
			throws ComponentRegistryException {
		Component component = getComponent(componentName);
		if (component == null) {
			// Are title and description pulled out anyway?
			// String title = annotationTools.getAnnotationString(dataflow, DescriptiveTitle.class, "");
			// String description = annotationTools.getAnnotationString(dataflow, FreeTextDescription.class, "");
			String sharing = "private"; // or download
			String dataflowString;
			try {
				ByteArrayOutputStream dataflowStream = new ByteArrayOutputStream();
				FileManager.getInstance().saveDataflowSilently(dataflow, new T2FlowFileType(),
						dataflowStream, false);
				dataflowString = dataflowStream.toString("UTF-8");
			} catch (OverwriteException e) {
				throw new ComponentRegistryException(e);
			} catch (SaveException e) {
				throw new ComponentRegistryException(e);
			} catch (IllegalStateException e) {
				throw new ComponentRegistryException(e);
			} catch (UnsupportedEncodingException e) {
				throw new ComponentRegistryException(e);
			}
			Element componentWorkflow = componentRegistry.uploadWorkflow(dataflowString, sharing);
			Element componentPack = componentRegistry.createPack(componentName);
			componentRegistry.addPackItem(componentPack, componentWorkflow);
		} else {

		}
		return null;
	}

	@Override
	public Component getComponent(String componentName) throws ComponentRegistryException {
		for (Component component : getComponents()) {
			if (componentName.equals(component.getName())) {
				return component;
			}
		}
		return null;
	}

	public String getUri() {
		return uri;
	}

}
