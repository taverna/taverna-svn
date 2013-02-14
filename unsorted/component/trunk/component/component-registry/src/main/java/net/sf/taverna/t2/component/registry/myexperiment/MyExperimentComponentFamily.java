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

import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
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
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentComponentFamily implements ComponentFamily {

	private final MyExperimentComponentRegistry componentRegistry;
	private final String uri;
	private final AnnotationTools annotationTools;

	private String name;
	private String description;
	private ComponentProfile componentProfile;
	private List<Component> components;
	private final MyExperimentPermissions permissions;

	public MyExperimentComponentFamily(MyExperimentComponentRegistry componentRegistry, MyExperimentPermissions permissions, String uri) {
		this.componentRegistry = componentRegistry;
		this.permissions = permissions;
		this.uri = uri;
		annotationTools = new AnnotationTools();
	}

	@Override
	public ComponentRegistry getComponentRegistry() {
		return componentRegistry;
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
	public String getDescription() {
		if (description == null) {
			Element descriptionElement = componentRegistry.getResourceElement(uri, "description");
			if (descriptionElement == null) {
				description = "";
			}
			description = descriptionElement.getTextTrim();
		}
		return description;
	}

	@Override
	public ComponentProfile getComponentProfile() throws ComponentRegistryException {
		if (componentProfile == null) {
			Element fileElement = componentRegistry.getPackItem(uri, "file", "component profile");
			String uri = fileElement.getAttributeValue("uri");
			String resource = fileElement.getAttributeValue("resource");
			String version = fileElement.getAttributeValue("version");
			String downloadUri = resource + "/download?version=" + version;
			try {
				componentProfile = new MyExperimentComponentProfile(componentRegistry, uri, new URL(downloadUri));
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
	public ComponentVersion createComponentBasedOn(String componentName, Dataflow dataflow) throws ComponentRegistryException {
		if (componentName == null) {
			throw new ComponentRegistryException(("Component name must not be null"));
		}
		if (dataflow == null) {
			throw new ComponentRegistryException(("Dataflow must not be null"));
		}
		Component component = getComponent(componentName);
		if (component != null) {
			throw new ComponentRegistryException("Component " + componentName + " already exists");
		}
		// upload the workflow
		String title = annotationTools.getAnnotationString(dataflow, DescriptiveTitle.class, "Untitled");
		String description = annotationTools.getAnnotationString(dataflow, FreeTextDescription.class, "No description");
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
		Element componentWorkflow = componentRegistry.uploadWorkflow(dataflowString, title, description, permissions);

		// create the component
		Element componentPack = componentRegistry.createPack(componentName, permissions);
		componentRegistry.tagResource("component", componentPack.getAttributeValue("resource"));
		component = new MyExperimentComponent(componentRegistry, componentPack.getAttributeValue("uri"));

		// add the component to the family
		Element resource = componentRegistry.getResource(uri);
		String attributeValue = resource.getAttributeValue("resource");
		componentRegistry.addPackItem(componentRegistry.getResource(uri), componentPack);
		components.add(component);


		// add the workflow to the pack
		componentRegistry.addPackItem(componentPack, componentWorkflow);


		componentPack = componentRegistry.snapshotPack(componentPack.getAttributeValue("uri"));
		String uri = componentPack.getAttributeValue("uri");
		String version = componentPack.getAttributeValue("uri");
		return new MyExperimentComponentVersion(componentRegistry, component, uri+"&version="+version);
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
