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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.License;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

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
	private Map<String, Component> componentsCache;
	private String permissionsString;
	private License license;
	
	private static XMLOutputter outputter = new XMLOutputter();

	public MyExperimentComponentFamily(MyExperimentComponentRegistry componentRegistry, License license,
			MyExperimentSharingPolicy permissions, String uri) throws ComponentRegistryException {
		this.componentRegistry = componentRegistry;
		this.license = license;
		this.uri = uri;
		annotationTools = new AnnotationTools();
		if (permissions == null) {
			this.permissionsString = this.getPermissionsString();
		} else {
			this.permissionsString = permissions.getPolicyString();
		}
		
		if (license == null) {
			this.license = componentRegistry.getLicenseOnObject(uri);
		}
	}

	private String getPermissionsString() {
		Element permissionsElement = componentRegistry.getResourceElement(uri, "permissions");
		if (permissionsElement == null) {
			return "";
		}
		String permissionsUri = permissionsElement.getAttributeValue("uri");
		String type = permissionsElement.getAttributeValue("policy-type");
		if (type.equals("group")) {
			Element policyElement = componentRegistry.getResource(permissionsUri);	
			String name = policyElement.getChildTextTrim("name");
			String id = policyElement.getChildTextTrim("id");
			return new MyExperimentGroupPolicy(name, id).getPolicyString();
		}
		return outputter.outputString(permissionsElement);
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
			try {
				Element fileElement = componentRegistry.getPackItem(uri, "file", "component profile");
				String uri = fileElement.getAttributeValue("uri");
				String withoutVersion = StringUtils.substringBeforeLast(uri, "&");
				for (ComponentProfile p : componentRegistry.getComponentProfiles()) {
					String uri2 = ((MyExperimentComponentProfile) p).getUri();
					if (uri2.equals(withoutVersion)) {
						componentProfile = p;
						break;
					}
				}
				if (componentProfile == null) {
					// Assume it is external
					String resource = fileElement.getAttributeValue("resource");
					String version = fileElement.getAttributeValue("version");
					resource = StringUtils.substringBeforeLast(resource, "?");
					String downloadUri = resource + "/download?version=" + version;
						String profileString = componentRegistry.getFileAsString(downloadUri);
						componentProfile = new MyExperimentComponentProfile(componentRegistry, uri, profileString);
				}
			} catch (ComponentRegistryException e) {
				try {
					String downloadUri = componentRegistry.getExternalPackItem(uri, "component profile");
					try {
						componentProfile = new ComponentProfile(new URL(downloadUri));
					} catch (MalformedURLException ex) {
						throw new ComponentRegistryException("Unable to open profile from " + downloadUri, ex);
					}
				} catch (ComponentRegistryException cre) {
					// no component profile present
				}
			}
		}
		return componentProfile;
	}

	@Override
	public List<Component> getComponents() throws ComponentRegistryException {
		return getComponentsIfNecessary();
	}

	private synchronized List<Component> getComponentsIfNecessary() throws ComponentRegistryException {
		List<Component> result = new ArrayList<Component>();
		if (componentsCache == null) {
			componentsCache = new HashMap<String, Component>();
			for (Element internalPackItem : componentRegistry.getResourceElements(uri,
					"internal-pack-items")) {
				if (internalPackItem.getName().equals("pack")) {
					String resourceUri = internalPackItem.getAttributeValue("resource");
					Element resource = componentRegistry.getResource(resourceUri + ".xml");
					String packUri = resource.getAttributeValue("uri");
					for (Element tag : componentRegistry.getResourceElements(packUri, "tags")) {
						String tagText = tag.getTextTrim();
						if ("component".equals(tagText)) {
							MyExperimentComponent newComponent = new MyExperimentComponent(componentRegistry, license, permissionsString, packUri);
							componentsCache.put(newComponent.getName(), newComponent);
							break;
						}
					}
				}
			}
		}
		result.addAll(componentsCache.values());
		return result;
	}

	@Override
	public ComponentVersion createComponentBasedOn(String componentName, String description, Dataflow dataflow) throws ComponentRegistryException {
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
//		String description = annotationTools.getAnnotationString(dataflow, FreeTextDescription.class, "No description");
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
		Element componentWorkflow = componentRegistry.uploadWorkflow(dataflowString, title,
				"Initial version", license, this.permissionsString);

		// create the component
		Element componentPack = componentRegistry.createPack(componentName, description, this.license, this.permissionsString);
		componentRegistry.tagResource("component", componentPack.getAttributeValue("resource"));
		component = new MyExperimentComponent(componentRegistry, this.license, this.permissionsString, componentPack.getAttributeValue("uri"));

		// add the component to the family
		Element resource = componentRegistry.getResource(uri);
		String attributeValue = resource.getAttributeValue("resource");
		componentRegistry.addPackItem(componentRegistry.getResource(uri), componentPack);
		if (componentsCache == null) {
			getComponents();
		}
		componentsCache.put(componentName, component);


		// add the workflow to the pack
		componentRegistry.addPackItem(componentPack, componentWorkflow);


		componentPack = componentRegistry.snapshotPack(componentPack.getAttributeValue("uri"));
		String uri = componentPack.getAttributeValue("uri");
		String version = componentPack.getAttributeValue("version");
		return new MyExperimentComponentVersion(componentRegistry, component, uri+"&version="+version);
	}

	@Override
	public Component getComponent(String componentName) throws ComponentRegistryException {
		if (componentsCache == null) {
			getComponents();
		}
		return (componentsCache.get(componentName));
	}

	public String getUri() {
		return uri;
	}

	@Override
	public void removeComponent(Component component)
			throws ComponentRegistryException {
		throw new ComponentRegistryException("Not yet implemented");
	}

}
