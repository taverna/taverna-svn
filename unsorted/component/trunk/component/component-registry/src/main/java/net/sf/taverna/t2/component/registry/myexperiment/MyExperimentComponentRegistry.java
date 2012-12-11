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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.help.UnsupportedOperationException;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Base64;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.ServerResponse;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentComponentRegistry implements ComponentRegistry {

	private static Logger logger = Logger.getLogger(MyExperimentComponentRegistry.class);

	private static Map<URL, MyExperimentComponentRegistry> componentRegistries = new HashMap<URL, MyExperimentComponentRegistry>();

	private final MyExperimentClient myExperimentClient;
	private final URL registryURL;

	private List<ComponentFamily> componentFamilies;
	private List<ComponentProfile> componentProfiles;

	private MyExperimentComponentRegistry(URL registryURL) {
		this.registryURL = registryURL;
		myExperimentClient = new MyExperimentClient(logger);
		myExperimentClient.setBaseURL(registryURL.toExternalForm());
		myExperimentClient.doLogin();
	}

	public static MyExperimentComponentRegistry getComponentRegistry(URL registryURL) {
		if (!componentRegistries.containsKey(registryURL)) {
			componentRegistries.put(registryURL, new MyExperimentComponentRegistry(registryURL));
		}
		return componentRegistries.get(registryURL);
	}

	@Override
	public List<ComponentFamily> getComponentFamilies() throws ComponentRegistryException {
		if (componentFamilies == null) {
			componentFamilies = new ArrayList<ComponentFamily>();
			Element packsElement = getResource(urlToString(registryURL) + "/packs.xml", "tag=component%20family");
			for (Object child : packsElement.getChildren("pack")) {
				if (child instanceof Element) {
					Element packElement = (Element) child;
					String packUri = packElement.getAttributeValue("uri");
					if (getResource(packUri) != null) {
						componentFamilies.add(new MyExperimentComponentFamily(this, packUri));
					}
				}
			}
		}
		return componentFamilies;
	}

	@Override
	public ComponentFamily getComponentFamily(String familyName) throws ComponentRegistryException {
		for (ComponentFamily componentFamily : getComponentFamilies()) {
			if (familyName.equals(componentFamily.getName())) {
				return componentFamily;
			}
		}
		return null;
	}

	@Override
	public ComponentFamily createComponentFamily(String name, ComponentProfile componentProfile) throws ComponentRegistryException {
		Element packElement = createPack(name);
		tagResource("component family", packElement.getAttributeValue("resource"));
		ComponentFamily componentFamily = new MyExperimentComponentFamily(this, packElement.getAttributeValue("uri"));
		Element profileElement = addComponentProfileInternal(componentProfile);
		addPackItem(packElement, profileElement);
		if (componentFamilies != null) {
			componentFamilies.add(componentFamily);
		}
		return componentFamily;
	}

	@Override
	public void removeComponentFamily(ComponentFamily componentFamily) throws ComponentRegistryException {
		if (componentFamilies.contains(componentFamily)) {
			if (componentFamily instanceof MyExperimentComponentFamily) {
				MyExperimentComponentFamily myExperimentComponentFamily = (MyExperimentComponentFamily) componentFamily;
				deleteResource(myExperimentComponentFamily.getUri());
				componentFamilies.remove(componentFamily);
			}
		}
	}

	@Override
	public URL getRegistryBase() {
		return registryURL;
	}

	@Override
	public List<ComponentProfile> getComponentProfiles() {
		if (componentProfiles == null) {
			componentProfiles = new ArrayList<ComponentProfile>();
			Element filesElement = getResource(urlToString(registryURL) + "/files.xml", "tag=component%20profile");
			for (Object child : filesElement.getChildren("file")) {
				if (child instanceof Element) {
					Element fileElement = (Element) child;
					String fileUri = fileElement.getAttributeValue("uri");
					String resourceUri = fileElement.getAttributeValue("resource");
					String version = fileElement.getAttributeValue("version");
					String downloadUri = resourceUri + "/download?version=" + version;
					if (getResource(fileUri) != null) {
						try {
							componentProfiles.add(new MyExperimentComponentProfile(this, fileUri, new URL(downloadUri)));
						} catch (MalformedURLException e) {
							logger.warn("URL for component profile is invalid : " + fileUri, e);
						}
					}
				}
			}
		}
		return componentProfiles;
	}

	@Override
	public ComponentProfile addComponentProfile(ComponentProfile componentProfile) throws ComponentRegistryException {
		Element element = addComponentProfileInternal(componentProfile);
		String fileUri = element.getAttributeValue("uri");
		String resourceUri = element.getAttributeValue("resource");
		String version = element.getAttributeValue("version");
		String downloadUri = resourceUri + "/download?version=" + version;
		try {
			componentProfile = new MyExperimentComponentProfile(this, fileUri, new URL(downloadUri));
			if (componentProfiles != null) {
				componentProfiles.add(componentProfile);
			}
		} catch (MalformedURLException e) {
			logger.warn("URL for component profile is invalid : " + fileUri, e);
		}
		return componentProfile;
	}

	private Element addComponentProfileInternal(ComponentProfile componentProfile) throws ComponentRegistryException {
		Element profileElement = null;
		if (componentProfile instanceof MyExperimentComponentProfile) {
			MyExperimentComponentProfile myExperimentComponentProfile = (MyExperimentComponentProfile) componentProfile;
			if (myExperimentComponentProfile.getComponentRegistry().getRegistryBase().equals(registryURL)) {
				profileElement = getResource(myExperimentComponentProfile.getUri());
			}
		}
		if (profileElement == null) {
			profileElement = uploadFile(componentProfile.getName(), componentProfile.getDescription(), "XML", componentProfile.getXML());
			tagResource("component profile", profileElement.getAttributeValue("resource"));
		}
		return profileElement;
	}

	public Element createPack(String title) throws ComponentRegistryException {
		String content = "<pack><title>" + title + "</title></pack>";
		try {
			ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/pack.xml", content);
			return packResponse.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new ComponentRegistryException("Error while creating a pack with title : " + title, e);
		}
	}

	public Element snapshotPack(String packUri) throws ComponentRegistryException {
		try {
			ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(packUri, "");
			return packResponse.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	public void addPackItem(Element packElement, Element itemElement) throws ComponentRegistryException {
		StringBuilder item = new StringBuilder();
		item.append("<internal-pack-item>");
		item.append("<pack resource=\"").append(packElement.getAttributeValue("resource")).append("\"/>");
		item.append("<item resource=\"").append(itemElement.getAttributeValue("resource")).append("\"");
		item.append(" version=\"").append(itemElement.getAttributeValue("version")).append("\"/>");
		item.append("</internal-pack-item>");
		try {
			myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/internal-pack-item.xml", item.toString());
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	public void deletePackItem(Element packElement, String item) throws ComponentRegistryException {
		for (Element internalPackItem : getResourceElements(packElement.getAttributeValue("uri"), "internal-pack-items")) {
			if (item.equals(internalPackItem.getName())) {
				deleteResource(internalPackItem.getAttributeValue("uri"));
				break;
			}
		}
	}

	public Element getPackItem(String packUri, String item, String... tags) throws ComponentRegistryException {
		for (Element internalPackItem : getResourceElements(packUri, "internal-pack-items")) {
			if (item.equals(internalPackItem.getName())) {
				String internalPackItemUri = internalPackItem.getAttributeValue("uri");
				Element itemElement = getResourceElement(internalPackItemUri, "item");
				if (itemElement == null) {
					throw new ComponentRegistryException("Element 'item' not found in internal-pack-item at " + packUri);
				}
				Element itemResourceElement = itemElement.getChild(item);
				if (itemResourceElement == null) {
					throw new ComponentRegistryException("Element 'item' does not contain " + item + " at " + packUri);
				}
				if (hasTags(itemResourceElement.getAttributeValue("uri"), tags)) {
					return itemResourceElement;
				}
			}
		}
		throw new ComponentRegistryException("Item " + item + " not found in internal-pack-items at " + packUri);
	}

	public boolean hasTags(String uri, String... tags) {
		if (tags != null && tags.length > 0) {
			Set<String> resourceTags = new HashSet<String>();
			for (Element tagElement : getResourceElements(uri, "tags")) {
				resourceTags.add(tagElement.getTextTrim());
			}
			for (String tag : tags) {
				if (!resourceTags.contains(tag)) {
					return false;
				}
			}
		}
		return true;
	}

	public Element uploadWorkflow(String dataflow, String sharing) throws ComponentRegistryException {
		ServerResponse postWorkflowResponse = myExperimentClient.postWorkflow(dataflow, "", "", "", sharing);
		return postWorkflowResponse.getResponseBody().getRootElement();
	}

	public Element updateWorkflow(String uri, String dataflow) throws ComponentRegistryException {
		Resource resource = Resource.buildFromXML(getResource(uri),myExperimentClient, logger);
		ServerResponse postWorkflowResponse = myExperimentClient.updateWorkflowVersionOrMetadata(resource, dataflow, "", "", "", "");
		return postWorkflowResponse.getResponseBody().getRootElement();
	}

	public Element uploadFile(String title, String description, String type, String content) throws ComponentRegistryException {
		StringBuilder contentXml = new StringBuilder();
		contentXml.append("<file>");
		contentXml.append("<filename>").append(title).append(".xml</filename>");
		contentXml.append("<title>").append(title).append("</title>");
		contentXml.append("<description>").append(description).append("</description>");
		contentXml.append("<type>").append(type).append("</type>");
		contentXml.append("<content encoding=\"base64\" type=\"binary\">");
		contentXml.append(Base64.encodeBytes(content.getBytes()));
		contentXml.append("</content>");
		contentXml.append("<license-type>by-nd</license-type>");
		contentXml.append("</file>");
		try {
			ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/file.xml", contentXml.toString());
			return packResponse.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	public void tagResource(String tag, String resource) throws ComponentRegistryException {
		String taggingToSend = "<tagging><subject resource=\"" + resource + "\"/><label>"+tag+"</label></tagging>";
		try {
			myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/tagging.xml", taggingToSend);
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	public Element getResource(String uri, String... query) {
		StringBuilder uriBuilder = new StringBuilder(uri);
		for (String queryElement : query) {
			uriBuilder.append(uriBuilder.indexOf("?") < 0 ? "?" : "&");
			uriBuilder.append(queryElement);
		}
		try {
			ServerResponse response = myExperimentClient.doMyExperimentGET(uriBuilder.toString());
			if (response.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			} else {
				return response.getResponseBody().getRootElement();
			}
		} catch (Exception e) {
			return null;
		}
	}

	public void deleteResource(String uri) throws ComponentRegistryException {
		try {
			myExperimentClient.doMyExperimentDELETE(uri);
		} catch (Exception e) {
			throw new ComponentRegistryException("Failed to delete " + uri, e);
		}
	}

	public List<Element> getResourceElements(String uri, String elementName) {
		List<Element> elements = new ArrayList<Element>();
		Element element = getResource(uri, "elements=" + elementName);
		if (element != null) {
			Element items = element.getChild(elementName);
			if (items != null) {
				for (Object child : items.getChildren()) {
					if (child instanceof Element) {
						elements.add((Element) child);
					}
				}
			}
		}
		return elements;
	}

	public Element getResourceElement(String uri, String elementName) {
		Element element = getResource(uri, "elements=" + elementName);
		if (element == null) {
			return null;
		} else {
			return element.getChild(elementName);
		}
	}

	public String urlToString(URL url) {
		String urlString = url.toString();
		if (urlString.endsWith("/")) {
			urlString = urlString.substring(0, urlString.length() - 1);
		}
		return urlString;
	}

}
