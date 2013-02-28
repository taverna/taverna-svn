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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.SharingPolicy;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Base64;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.ServerResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * Implementation of a ComponentRegistry that uses myExperiment.
 *
 * @author David Withers
 */
public class MyExperimentComponentRegistry implements ComponentRegistry {

	private static Logger logger = Logger.getLogger(MyExperimentComponentRegistry.class);

	private static Map<String, MyExperimentComponentRegistry> componentRegistries = new HashMap<String, MyExperimentComponentRegistry>();

	private final MyExperimentClient myExperimentClient;
	private final URL registryURL;

	private Map<String, ComponentFamily> familyCache;
	private List<ComponentProfile> profileCache;
	
	public static MyExperimentSharingPolicy PRIVATE = new MyExperimentPrivatePolicy();
	public static MyExperimentSharingPolicy PUBLIC = new MyExperimentPublicPolicy();

	private MyExperimentComponentRegistry(URL registryURL) {
		this.registryURL = registryURL;
		myExperimentClient = new MyExperimentClient(logger);
		myExperimentClient.setBaseURL(registryURL.toExternalForm());
		myExperimentClient.doLogin();
	}

	public static MyExperimentComponentRegistry getComponentRegistry(URL registryURL) {
		if (!componentRegistries.containsKey(registryURL.toExternalForm())) {
			componentRegistries.put(registryURL.toExternalForm(), new MyExperimentComponentRegistry(registryURL));
		}
		return componentRegistries.get(registryURL.toExternalForm());
	}

	@Override
	public List<ComponentFamily> getComponentFamilies() throws ComponentRegistryException {
		List<ComponentFamily> result = new ArrayList<ComponentFamily>();
		if (familyCache == null) {
			familyCache = new HashMap<String, ComponentFamily>();
			Element packsElement = getResource(urlToString(registryURL) + "/packs.xml", "tag=component%20family", "elements=permissions");
			for (Object child : packsElement.getChildren("pack")) {
				if (child instanceof Element) {
					Element packElement = (Element) child;
					String packUri = packElement.getAttributeValue("uri");
					if (getResource(packUri) != null) {

						MyExperimentComponentFamily newFamily = new MyExperimentComponentFamily(this, null, packUri);
						familyCache.put(newFamily.getName(), newFamily);
					}
				}
			}
		}
		result.addAll(familyCache.values());
		return result;
	}

	@Override
	public ComponentFamily getComponentFamily(String familyName) throws ComponentRegistryException {
		if (familyCache == null) {
			getComponentFamilies();
		}
		return familyCache.get(familyName);
	}

	public ComponentFamily createComponentFamily(String name, ComponentProfile componentProfile, SharingPolicy sharingPolicy) throws ComponentRegistryException {
		if (name == null) {
			throw new ComponentRegistryException(("Component name must not be null"));
		}
		if (componentProfile == null) {
			throw new ComponentRegistryException(("Component profile must not be null"));
		}
		if (getComponentFamily(name) != null) {
			throw new ComponentRegistryException(("Component family already exists"));
		}
		MyExperimentSharingPolicy permissions = (MyExperimentSharingPolicy) sharingPolicy;
		if (permissions == null) {
			permissions = MyExperimentComponentRegistry.PRIVATE;
		}
		Element packElement = createPack(name, permissions.getPolicyString());
		tagResource("component family", packElement.getAttributeValue("resource"));
		ComponentFamily componentFamily = new MyExperimentComponentFamily(this, permissions, packElement.getAttributeValue("uri"));

		Element profileElement = addComponentProfileInternal(componentProfile);
		addPackItem(packElement, profileElement);

		if (familyCache == null) {
			getComponentFamilies();
		}
		familyCache.put(name, componentFamily);
		return componentFamily;
	}

	@Override
	public void removeComponentFamily(ComponentFamily componentFamily) throws ComponentRegistryException {
		if (componentFamily != null) {
			if (familyCache == null) {
				getComponentFamilies();
			}
			familyCache.remove(componentFamily.getName());
		}
		if (componentFamily instanceof MyExperimentComponentFamily) {
			MyExperimentComponentFamily myExperimentComponentFamily = (MyExperimentComponentFamily) componentFamily;
			deleteResource(myExperimentComponentFamily.getUri());
		}		
	}

	@Override
	public URL getRegistryBase() {
		return registryURL;
	}

	@Override
	public List<ComponentProfile> getComponentProfiles() throws ComponentRegistryException {
		List<ComponentProfile> result = new ArrayList<ComponentProfile>();
		if (profileCache == null) {
			profileCache = new ArrayList<ComponentProfile>();
			Element filesElement = getResource(urlToString(registryURL) + "/files.xml", "tag=component%20profile");
			for (Object child : filesElement.getChildren("file")) {
				if (child instanceof Element) {
					Element fileElement = (Element) child;
					String fileUri = fileElement.getAttributeValue("uri");
					String resourceUri = fileElement.getAttributeValue("resource");
					String version = fileElement.getAttributeValue("version");
					String downloadUri = resourceUri + "/download?version=" + version;
					if (getResource(fileUri) != null) {
							String profileString = getFileAsString(downloadUri);
							profileCache.add(new MyExperimentComponentProfile(this, fileUri, profileString));
					}
				}
			}
		}
		result.addAll(profileCache);
		return result;
	}

	@Override
	public ComponentProfile addComponentProfile(ComponentProfile componentProfile) throws ComponentRegistryException {
		if (profileCache == null) {
			getComponentProfiles();
		}
		Element element = addComponentProfileInternal(componentProfile);
		String fileUri = element.getAttributeValue("uri");
		ComponentProfile result = new MyExperimentComponentProfile(this, fileUri, componentProfile.getXML());
				profileCache.add(result);

		return result;
	}

	private Element addComponentProfileInternal(ComponentProfile componentProfile) throws ComponentRegistryException {
		if (componentProfile == null) {
			throw new ComponentRegistryException(("Component profile must not be null"));
		}
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

	public Element createPack(String title, String permissionsString) throws ComponentRegistryException {
		StringBuilder contentXml = new StringBuilder("<pack>");
		contentXml.append("<title>" + title + "</title>");
		contentXml.append(permissionsString);
		contentXml.append("</pack>");
		try {
			ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/pack.xml", contentXml.toString());
			return packResponse.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new ComponentRegistryException("Error while creating a pack with title : " + title, e);
		}
	}

	public Element snapshotPack(String packUri) throws ComponentRegistryException {
		try {
			ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(packUri, "<snapshot/>");
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
		String version = itemElement.getAttributeValue("version");
		if (version.isEmpty()) {
			item.append("/>");
		} else {
			item.append(" version=\"").append(version).append("\"/>");
		}
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

	public String getExternalPackItem(String packUri, String title) throws ComponentRegistryException {
		for (Element externalPackItem : getResourceElements(packUri, "external-pack-items")) {
			String itemTitle = externalPackItem.getTextTrim();
			if (title.equals(itemTitle)) {
				return externalPackItem.getAttributeValue("resource");
			}
		}
		throw new ComponentRegistryException("Item " + title + " not found in external-pack-items at " + packUri);
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

	public Element uploadWorkflow(String dataflow, String title, String description, String permissionsString) throws ComponentRegistryException {
		String workflowElement = prepareWorkflowPostContent(dataflow, title, description, "by-nd", permissionsString);
		try {
			ServerResponse response = myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/workflow.xml", workflowElement);
			return response.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new ComponentRegistryException("Unable to upload workflow", e);
		}
	}

	public Element updateWorkflow(String uri, String dataflow, String title, String description, String permissionsString) throws ComponentRegistryException {
		String workflowElement = prepareWorkflowPostContent(dataflow, title, description, "by-nd", permissionsString);
		try {
			ServerResponse response = myExperimentClient.doMyExperimentPOST(uri, workflowElement);
			return response.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new ComponentRegistryException("Unable to update workflow at " + uri, e);
		}
	}

	private String prepareWorkflowPostContent(String dataflow, String title, String description, String license, String permissionsString) {
		StringBuilder contentXml = new StringBuilder("<workflow>");
		if (title.length() > 0) contentXml.append("<title>").append(title).append("</title>");
		if (description.length() > 0) {
			contentXml.append("<description>").append(description).append("</description>");
		}
		if (license.length() > 0) {
			contentXml.append("<license-type>").append(license).append("</license-type>");
		}
		contentXml.append(permissionsString);

		// check the format of the workflow
		String scuflSchemaDef = "xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"";
		String t2flowSchemaDef = "xmlns=\"http://taverna.sf.net/2008/xml/t2flow\"";

		if (dataflow.length() > 0) {
			String contentType;
			if (dataflow.contains(scuflSchemaDef)) contentType = "application/vnd.taverna.scufl+xml";
			else if (dataflow.contains(t2flowSchemaDef)) contentType = "application/vnd.taverna.t2flow+xml";
			else contentType = "";

			contentXml.append("<content-type>").append(contentType).append("</content-type>");
			contentXml.append("<content encoding=\"base64\" type=\"binary\">");
			contentXml.append(Base64.encodeBytes(dataflow.getBytes())).append("</content>");
		}

		contentXml.append("</workflow>");

		return contentXml.toString();
	}
	
	public String getFileAsString(String url) {
		try {
			ServerResponse response = myExperimentClient.doMyExperimentGET(url);
			int responseCode = response.getResponseCode();
			if (responseCode >= 400) {
				logger.error("Received response code " + responseCode + " when reading from " + url);
				return null;
			}
			Document responseBody = response.getResponseBody();
			Element root = responseBody.getRootElement();
			String content = new XMLOutputter().outputString(responseBody);
			return content;
		} catch (Exception e) {
			logger.error("Unable to read file: " + url, e);
			return null;
		}
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

	public List<MyExperimentGroup> getGroups() {
		List<MyExperimentGroup> groups = new ArrayList<MyExperimentGroup>();
		String user = getUser();
		if (user != null) {
			List<Element> resourceElements = getResourceElements(user, "groups");
			for (Element element : resourceElements) {
				groups.add(new MyExperimentGroup(getResource(element.getAttributeValue("uri"))));
			}
		}
		return groups;
	}

	public String getUser() {
		Element userElement = getResource(urlToString(registryURL) + "/whoami.xml");
		if (userElement == null) {
			return null;
		}
		return userElement.getAttributeValue("uri");
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

	@Override
	public List<SharingPolicy> getPermissions() throws ComponentRegistryException {
		List<SharingPolicy> result = new ArrayList<SharingPolicy>();
		result.add(PUBLIC);
		Element policiesElement = getResource(urlToString(registryURL) + "/policies.xml", "type=group");
		for (Object child : policiesElement.getChildren("policy")) {
			if (child instanceof Element) {
				Element policyElement = (Element) child;
				String fullId = policyElement.getAttributeValue("uri");
				String id = StringUtils.substringAfterLast(fullId, "=");
				String name = policyElement.getTextTrim();
				result.add(new MyExperimentGroupPolicy(name, id));
			}
		}
		result.add(PRIVATE);
		return result;

	}

}
