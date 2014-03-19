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

import static org.apache.log4j.Logger.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * 
 * 
 * @author David Withers
 */
final class MyExperimentComponentFamily extends ComponentFamily {
	private static Logger logger = getLogger(MyExperimentComponentFamily.class);

	private final String uri;
	private final AnnotationTools annotationTools;

	private String permissionsString;
	private License license;

	private static XMLOutputter outputter = new XMLOutputter();

	public MyExperimentComponentFamily(
			MyExperimentComponentRegistry componentRegistry, License license,
			MyExperimentSharingPolicy permissions, String uri)
			throws RegistryException {
		super(componentRegistry);
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

	private synchronized String getPermissionsString() {
		MyExperimentComponentRegistry componentRegistry = (MyExperimentComponentRegistry) this
				.getComponentRegistry();
		Element permissionsElement = componentRegistry.getResourceElement(uri,
				"permissions");
		if (permissionsElement == null) {
			return "";
		}
		String permissionsUri = permissionsElement.getAttributeValue("uri");
		String type = permissionsElement.getAttributeValue("policy-type");
		if (type.equals("group")) {
			Element policyElement = componentRegistry
					.getResource(permissionsUri);
			String name = policyElement.getChildTextTrim("name");
			String id = policyElement.getChildTextTrim("id");
			return new MyExperimentGroupPolicy(name, id).getPolicyString();
		}
		return outputter.outputString(permissionsElement);
	}

	@Override
	public synchronized String internalGetName() {
		MyExperimentComponentRegistry componentRegistry = (MyExperimentComponentRegistry) this
				.getComponentRegistry();
		String result = "";
		Element titleElement = componentRegistry.getResourceElement(uri,
				"title");
		if (titleElement != null) {
			result = titleElement.getTextTrim();
		}
		return result;
	}

	@Override
	public synchronized String internalGetDescription() {
		MyExperimentComponentRegistry componentRegistry = (MyExperimentComponentRegistry) this
				.getComponentRegistry();
		String result = "";
		Element descriptionElement = componentRegistry.getResourceElement(uri,
				"description");
		if (descriptionElement != null) {
			result = descriptionElement.getTextTrim();
		}

		return result;
	}

	@Override
	public synchronized Profile internalGetComponentProfile()
			throws RegistryException {
		MyExperimentComponentRegistry componentRegistry = (MyExperimentComponentRegistry) this
				.getComponentRegistry();
		Profile result = null;
		try {
			Element fileElement = componentRegistry.getPackItem(uri, "file",
					"component profile");
			String uri = fileElement.getAttributeValue("uri");
			String withoutVersion = StringUtils.substringBeforeLast(uri, "&");
			for (Profile p : componentRegistry.getComponentProfiles()) {
				if (!(p instanceof MyExperimentComponentProfile)) {
					continue;
				}
				String uri2 = ((MyExperimentComponentProfile) p).getUri();
				if (uri2.equals(withoutVersion)) {
					result = p;
					break;
				}
			}
			if (result == null) {
				// Assume it is external
				String resource = fileElement.getAttributeValue("resource");
				String version = fileElement.getAttributeValue("version");
				resource = StringUtils.substringBeforeLast(resource, "?");
				String downloadUri = resource + "/download?version=" + version;
				String profileString = componentRegistry
						.getFileAsString(downloadUri);
				result = new MyExperimentComponentProfile(componentRegistry,
						uri, profileString);
			}
		} catch (RegistryException e) {
			try {
				String downloadUri = componentRegistry.getExternalPackItem(uri,
						"component profile");
				try {
					result = new ComponentProfile(super.getComponentRegistry(),
							new URL(downloadUri));
				} catch (MalformedURLException ex) {
					throw new RegistryException("Unable to open profile from "
							+ downloadUri, ex);
				}
			} catch (RegistryException cre) {
				// no component profile present
			}
		}
		return result;
	}

	@Override
	protected synchronized void populateComponentCache()
			throws RegistryException {

		MyExperimentComponentRegistry componentRegistry = (MyExperimentComponentRegistry) this
				.getComponentRegistry();
		for (Element internalPackItem : componentRegistry.getResourceElements(
				uri, "internal-pack-items")) {
			if (internalPackItem.getName().equals("pack")) {
				String resourceUri = internalPackItem
						.getAttributeValue("resource");
				Element resource = componentRegistry.getResource(resourceUri
						+ ".xml");
				logger.info("Getting resource " + resourceUri + ".xml");
				if (resource == null) {
					continue;
				}
				String packUri = resource.getAttributeValue("uri");
				for (Element tag : componentRegistry.getResourceElements(
						packUri, "tags")) {
					String tagText = tag.getTextTrim();
					if ("component".equals(tagText)) {
						MyExperimentComponent newComponent = new MyExperimentComponent(
								componentRegistry, this, license, permissionsString,
								packUri);
						componentCache
								.put(newComponent.getName(), newComponent);
						break;
					}
				}
			}
		}
	}

	@Override
	public Version internalCreateComponentBasedOn(String componentName,
			String description, Dataflow dataflow) throws RegistryException {
		Component component;

		MyExperimentComponentRegistry componentRegistry = (MyExperimentComponentRegistry) this
				.getComponentRegistry();
		// upload the workflow
		String title = annotationTools.getAnnotationString(dataflow,
				DescriptiveTitle.class, "Untitled");
		// String description = annotationTools.getAnnotationString(dataflow,
		// FreeTextDescription.class, "No description");
		String dataflowString;
		try {
			ByteArrayOutputStream dataflowStream = new ByteArrayOutputStream();
			FileManager.getInstance().saveDataflowSilently(dataflow,
					new T2FlowFileType(), dataflowStream, false);
			dataflowString = dataflowStream.toString("UTF-8");
		} catch (OverwriteException e) {
			throw new RegistryException(e);
		} catch (SaveException e) {
			throw new RegistryException(e);
		} catch (IllegalStateException e) {
			throw new RegistryException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RegistryException(e);
		}
		Element componentWorkflow = componentRegistry.uploadWorkflow(
				dataflowString, title, "Initial version", license,
				this.permissionsString);

		// create the component
		Element componentPack = componentRegistry.createPack(componentName,
				description, this.license, this.permissionsString);
		componentRegistry.tagResource("component",
				componentPack.getAttributeValue("resource"));
		component = new MyExperimentComponent(componentRegistry, this, this.license,
				this.permissionsString, componentPack.getAttributeValue("uri"));

		// add the component to the family

		componentRegistry.addPackItem(componentRegistry.getResource(uri),
				componentPack);

		// add the workflow to the pack
		componentRegistry.addPackItem(componentPack, componentWorkflow);

		componentPack = componentRegistry.snapshotPack(componentPack
				.getAttributeValue("uri"));
		String uri = componentPack.getAttributeValue("uri");
		String version = componentPack.getAttributeValue("version");
		return new MyExperimentComponentVersion(componentRegistry, component,
				uri + "&version=" + version);
	}

	String getUri() {
		return uri;
	}

	@Override
	public void internalRemoveComponent(Component component)
			throws RegistryException {
		throw new RegistryException("Not yet implemented");
	}

}
