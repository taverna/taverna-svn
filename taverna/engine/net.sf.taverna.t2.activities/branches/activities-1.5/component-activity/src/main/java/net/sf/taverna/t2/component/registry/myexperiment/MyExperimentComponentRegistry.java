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

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.URLEncoder.encode;
import static net.sf.taverna.t2.component.registry.myexperiment.client.utils.Base64.encodeBytes;
import static org.apache.log4j.Logger.getLogger;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.registry.myexperiment.client.MyExperimentClient;
import net.sf.taverna.t2.component.registry.myexperiment.client.ServerResponse;
import net.sf.taverna.t2.component.registry.myexperiment.client.utils.Base64;

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
class MyExperimentComponentRegistry extends ComponentRegistry {
	private static Logger logger = getLogger(MyExperimentComponentRegistry.class);
	public static MyExperimentSharingPolicy PRIVATE = new MyExperimentPrivatePolicy();
	public static MyExperimentSharingPolicy PUBLIC = new MyExperimentPublicPolicy();
	private static final String DO_PUT = "_DO_UPDATE_SIGNAL_";

	private final MyExperimentClient myExperimentClient;

	MyExperimentComponentRegistry(URL registryURL) throws RegistryException {
		super(registryURL);
		try {
			myExperimentClient = new MyExperimentClient(logger);
			myExperimentClient.setBaseURL(registryURL.toExternalForm());
			myExperimentClient.doLogin();
		} catch (Exception e) {
			throw new RegistryException("Unable to access registry", e);
		}
	}

	@Override
	protected void populateFamilyCache() throws RegistryException {
		Element packsElement = getResource(getRegistryBaseString()
				+ "/packs.xml", "tag=component%20family",
				"elements=permissions");
		for (Object child : packsElement.getChildren("pack"))
			if (child instanceof Element) {
				Element packElement = (Element) child;
				String packUri = packElement.getAttributeValue("uri");
				if (getResource(packUri) != null) {
					MyExperimentComponentFamily newFamily = new MyExperimentComponentFamily(
							this, null, null, packUri);
					familyCache.put(newFamily.getName(), newFamily);
				}
			}
	}

	@Override
	protected Family internalCreateComponentFamily(String name,
			Profile componentProfile, String description, License license,
			SharingPolicy sharingPolicy) throws RegistryException {
		MyExperimentSharingPolicy permissions = (MyExperimentSharingPolicy) sharingPolicy;
		if (permissions == null)
			permissions = MyExperimentComponentRegistry.PRIVATE;
		Element packElement = createPack(name, description, license,
				permissions.getPolicyString());
		tagResource("component family",
				packElement.getAttributeValue("resource"));
		Family componentFamily = new MyExperimentComponentFamily(this, license,
				permissions, packElement.getAttributeValue("uri"));

		Element profileElement = addComponentProfileInternal(componentProfile,
				license, permissions);
		addPackItem(packElement, profileElement);
		return componentFamily;
	}

	@Override
	protected void internalRemoveComponentFamily(Family componentFamily)
			throws RegistryException {
		if (componentFamily instanceof MyExperimentComponentFamily) {
			MyExperimentComponentFamily myExperimentComponentFamily = (MyExperimentComponentFamily) componentFamily;
			deleteResource(myExperimentComponentFamily.getUri());
		}
	}

	@Override
	protected void populateProfileCache() throws RegistryException {
		Element filesElement = getResource(getRegistryBaseString()
				+ "/files.xml", "tag=component%20profile");
		for (Object child : filesElement.getChildren("file"))
			if (child instanceof Element) {
				Element fileElement = (Element) child;
				String fileUri = fileElement.getAttributeValue("uri");
				String resourceUri = fileElement.getAttributeValue("resource");
				String version = fileElement.getAttributeValue("version");
				resourceUri = StringUtils.substringBeforeLast(resourceUri, "?");
				String downloadUri = resourceUri + "/download?version="
						+ version;
				if (getResource(fileUri) != null) {
					String profileString = getFileAsString(downloadUri);
					profileCache.add(new MyExperimentComponentProfile(this,
							fileUri, profileString));
				}
			}
	}

	@Override
	public Profile internalAddComponentProfile(Profile componentProfile,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		Element element = addComponentProfileInternal(componentProfile,
				license, sharingPolicy);
		String fileUri = element.getAttributeValue("uri");
		Profile result = new MyExperimentComponentProfile(this, fileUri,
				componentProfile.getXML());
		return result;
	}

	private Element addComponentProfileInternal(Profile componentProfile,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		if (componentProfile == null)
			throw new RegistryException(("Component profile must not be null"));
		Element profileElement = null;
		if (componentProfile instanceof MyExperimentComponentProfile) {
			MyExperimentComponentProfile myExperimentComponentProfile = (MyExperimentComponentProfile) componentProfile;
			if (myExperimentComponentProfile.getComponentRegistry()
					.equals(this))
				profileElement = getResource(myExperimentComponentProfile
						.getUri());
		}
		MyExperimentSharingPolicy permissions = (MyExperimentSharingPolicy) sharingPolicy;
		if (permissions == null)
			permissions = MyExperimentComponentRegistry.PRIVATE;
		if (profileElement == null) {
			profileElement = uploadFile(componentProfile.getName(),
					componentProfile.getDescription(), "XML",
					componentProfile.getXML(), license,
					permissions.getPolicyString());
			tagResource("component profile",
					profileElement.getAttributeValue("resource"));
		}
		return profileElement;
	}

	public Element createPack(String title, String description,
			License license, String permissionsString) throws RegistryException {
		StringBuilder contentXml = new StringBuilder("<pack>");
		contentXml.append("<description>" + description + "</description>");
		contentXml.append("<title>" + title + "</title>");
		if (license != null)
			contentXml.append("<license-type>")
					.append(license.getAbbreviation())
					.append("</license-type>");
		contentXml.append(permissionsString);
		contentXml.append("</pack>");
		ServerResponse response;
		try {
			response = myExperimentClient.doMyExperimentPOST(
					getRegistryBaseString() + "/pack.xml",
					contentXml.toString());
			checkResponseCode(response);
			return response.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new RegistryException(
					"Error while creating a pack with title : " + title, e);
		}
	}

	public Element snapshotPack(String packUri) throws RegistryException {
		try {
			ServerResponse response = myExperimentClient.doMyExperimentPOST(
					packUri, "<snapshot/>");
			checkResponseCode(response);
			return response.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	public void addPackItem(Element packElement, Element itemElement)
			throws RegistryException {
		StringBuilder item = new StringBuilder();
		item.append("<internal-pack-item>");
		item.append("<pack resource=\"")
				.append(packElement.getAttributeValue("resource"))
				.append("\"/>");
		item.append("<item resource=\"")
				.append(itemElement.getAttributeValue("resource")).append("\"");
		String version = itemElement.getAttributeValue("version");
		if ((version == null) || version.isEmpty())
			item.append("/>");
		else
			item.append(" version=\"").append(version).append("\"/>");
		item.append("</internal-pack-item>");
		try {
			ServerResponse response = myExperimentClient.doMyExperimentPOST(
					getRegistryBaseString() + "/internal-pack-item.xml",
					item.toString());
			checkResponseCode(response);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	public void checkResponseCode(ServerResponse response)
			throws RegistryException {
		if (response.getResponseCode() >= 400)
			throw new RegistryException("Unable to perform request "
					+ response.getResponseCode());
	}

	public void deletePackItem(Element packElement, String item)
			throws RegistryException {
		for (Element internalPackItem : getResourceElements(
				packElement.getAttributeValue("uri"), "internal-pack-items"))
			if (item.equals(internalPackItem.getName())) {
				deleteResource(internalPackItem.getAttributeValue("uri"));
				break;
			}
	}

	public Element getPackItem(String packUri, String item, String... tags)
			throws RegistryException {
		for (Element internalPackItem : getResourceElements(packUri,
				"internal-pack-items"))
			if (item.equals(internalPackItem.getName())) {
				String internalPackItemUri = internalPackItem
						.getAttributeValue("uri");
				Element itemElement = getResourceElement(internalPackItemUri,
						"item");
				if (itemElement == null)
					throw new RegistryException(
							"Element 'item' not found in internal-pack-item at "
									+ packUri);
				Element itemResourceElement = itemElement.getChild(item);
				if (itemResourceElement == null)
					throw new RegistryException(
							"Element 'item' does not contain " + item + " at "
									+ packUri);
				if (hasTags(itemResourceElement.getAttributeValue("uri"), tags))
					return itemResourceElement;
			}
		throw new RegistryException("Item " + item
				+ " not found in internal-pack-items at " + packUri);
	}

	public String getExternalPackItem(String packUri, String title)
			throws RegistryException {
		for (Element externalPackItem : getResourceElements(packUri,
				"external-pack-items")) {
			String itemTitle = externalPackItem.getTextTrim();
			if (title.equals(itemTitle))
				return externalPackItem.getAttributeValue("resource");
		}
		throw new RegistryException("Item " + title
				+ " not found in external-pack-items at " + packUri);
	}

	public boolean hasTags(String uri, String... tags) {
		if (tags != null && tags.length > 0) {
			Set<String> resourceTags = new HashSet<String>();
			for (Element tagElement : getResourceElements(uri, "tags"))
				resourceTags.add(tagElement.getTextTrim());
			for (String tag : tags)
				if (!resourceTags.contains(tag))
					return false;
		}
		return true;
	}

	public Element uploadWorkflow(String dataflow, String title,
			String description, License license, String permissionsString)
			throws RegistryException {
		String workflowElement = prepareWorkflowPostContent(dataflow, title,
				"Initial version", license, permissionsString);
		try {
			logger.info("Uploading " + workflowElement);
			ServerResponse response = myExperimentClient.doMyExperimentPOST(
					getRegistryBaseString() + "/workflow.xml", workflowElement);
			checkResponseCode(response);
			return response.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new RegistryException("Unable to upload workflow", e);
		}
	}

	public Element updateWorkflow(String uri, String dataflow, String title,
			String revisionComment, License license, String permissionsString)
			throws RegistryException {
		String workflowElement = prepareWorkflowPostContent(dataflow, title,
				revisionComment, license, permissionsString);
		try {
			logger.info("Uploading " + workflowElement);
			ServerResponse response = myExperimentClient.doMyExperimentPOST(uri
					+ DO_PUT, workflowElement);
			checkResponseCode(response);
			return response.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new RegistryException("Unable to update workflow at " + uri,
					e);
		}
	}

	private String prepareWorkflowPostContent(String dataflow, String title,
			String description, License license, String permissionsString)
			throws RegistryException {
		StringBuilder contentXml = new StringBuilder("<workflow>");
		if (title.length() > 0)
			contentXml.append("<title>").append(title).append("</title>");
		if (description.length() > 0)
			contentXml.append("<description>").append(description)
					.append("</description>");
		if (license != null)
			contentXml.append("<license-type>")
					.append(license.getAbbreviation())
					.append("</license-type>");
		contentXml.append(permissionsString);

		if (dataflow.length() > 0) {
			contentXml
					.append("<content-type>application/vnd.taverna.t2flow+xml</content-type>");
			contentXml.append("<content encoding=\"base64\" type=\"binary\">");
			try {
				contentXml.append(encodeBytes(dataflow.getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				throw new RegistryException("Unable to encode workflow", e);
			}
			contentXml.append("</content>");
		}

		contentXml.append("</workflow>");

		return contentXml.toString();
	}

	public String getFileAsString(String url) {
		try {
			ServerResponse response = myExperimentClient.doMyExperimentGET(url);
			checkResponseCode(response);
			Document responseBody = response.getResponseBody();

			String content = new XMLOutputter().outputString(responseBody);
			return content;
		} catch (Exception e) {
			logger.error("Unable to read file: " + url, e);
			return null;
		}
	}

	public Element uploadFile(String title, String description, String type,
			String content, License license, String permissionsString)
			throws RegistryException {
		StringBuilder contentXml = new StringBuilder();
		contentXml.append("<file>");
		contentXml.append("<filename>").append(title).append(".xml</filename>");
		contentXml.append("<title>").append(title).append("</title>");
		contentXml.append("<description>").append(description)
				.append("</description>");
		contentXml.append("<type>").append(type).append("</type>");
		contentXml.append("<content encoding=\"base64\" type=\"binary\">");
		try {
			contentXml.append(Base64.encodeBytes(content.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			throw new RegistryException("Unknown encoding", e1);
		}
		contentXml.append("</content>");
		if (license != null) {
			contentXml.append("<license-type>")
					.append(license.getAbbreviation())
					.append("</license-type>");
		}
		contentXml.append(permissionsString);
		contentXml.append("</file>");
		try {
			ServerResponse response = myExperimentClient.doMyExperimentPOST(
					getRegistryBaseString() + "/file.xml",
					contentXml.toString());
			checkResponseCode(response);
			return response.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	public void tagResource(String tag, String resource)
			throws RegistryException {
		String taggingToSend = "<tagging><subject resource=\"" + resource
				+ "\"/><label>" + tag + "</label></tagging>";
		try {
			ServerResponse response = myExperimentClient.doMyExperimentPOST(
					getRegistryBaseString() + "/tagging.xml", taggingToSend);
			checkResponseCode(response);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	public Element getResource(String uri, String... query) {
		StringBuilder uriBuilder = new StringBuilder(uri);
		for (String queryElement : query) {
			uriBuilder.append(uriBuilder.indexOf("?") < 0 ? "?" : "&");
			uriBuilder.append(queryElement);
		}
		try {
			ServerResponse response = myExperimentClient
					.doMyExperimentGET(uriBuilder.toString());
			if (response.getResponseCode() != HTTP_OK)
				return null;
			return response.getResponseBody().getRootElement();
		} catch (Exception e) {
			return null;
		}
	}

	public void deleteResource(String uri) throws RegistryException {
		try {
			ServerResponse response = myExperimentClient
					.doMyExperimentDELETE(uri);
			checkResponseCode(response);
		} catch (Exception e) {
			throw new RegistryException("Failed to delete " + uri, e);
		}
	}

	public List<Element> getResourceElements(String uri, String elementName) {
		List<Element> elements = new ArrayList<Element>();
		Element element = getResource(uri, "elements=" + elementName);
		if (element != null) {
			Element items = element.getChild(elementName);
			if (items != null)
				for (Object child : items.getChildren())
					if (child instanceof Element)
						elements.add((Element) child);
		}
		return elements;
	}

	public Element getResourceElement(String uri, String elementName) {
		Element element = getResource(uri, "elements=" + elementName);
		if (element == null)
			return null;
		return element.getChild(elementName);
	}

	public static String urlToString(URL url) {
		String urlString = url.toString();
		if (urlString.endsWith("/"))
			urlString = urlString.substring(0, urlString.length() - 1);
		return urlString;
	}

	@Override
	protected void populatePermissionCache() {
		permissionCache.add(PUBLIC);
/*		Element policiesElement = getResource(getRegistryBaseString()
				+ "/policies.xml", "type=group");
		for (Object child : policiesElement.getChildren("policy"))
			if (child instanceof Element) {
				Element policyElement = (Element) child;
				String fullId = policyElement.getAttributeValue("uri");
				String id = StringUtils.substringAfterLast(fullId, "=");
				String name = policyElement.getTextTrim();
				permissionCache.add(new MyExperimentGroupPolicy(name, id));
			}
*/
		permissionCache.add(PRIVATE);
	}

	@Override
	protected void populateLicenseCache() {
		Element licensesElement = getResource(getRegistryBaseString()
				+ "/licenses.xml");
		for (Object child : licensesElement.getChildren("license"))
			if (child instanceof Element) {
				Element licenseElement = (Element) child;
				String uri = licenseElement.getAttributeValue("uri");
				License newLicense = new MyExperimentLicense(this, uri);
				licenseCache.add(newLicense);
			}
	}

	public License getLicenseOnObject(String uri) throws RegistryException {
		String licenseString = getResource(uri)
				.getChildTextTrim("license-type");
		return getLicenseByAbbreviation(licenseString);
	}

	@Override
	public License getPreferredLicense() throws RegistryException {
		return getLicenseByAbbreviation("by-nd");
	}

	@Override
	public Set<Version.ID> searchForComponents(String prefixString, String text)
			throws RegistryException {
		Set<Version.ID> result = new HashSet<Version.ID>();
		Element resultElement;
		try {
			resultElement = getResource(getRegistryBaseString()
					+ "/components.xml",
					"prefixes=" + encode(prefixString, "UTF-8"), "query="
							+ encode(text, "UTF-8"));
			if (resultElement == null)
				throw new RegistryException(
						"ComponentRegistry could not perform search");
		} catch (UnsupportedEncodingException e) {
			throw new RegistryException(
					"ComponentRegistry could not perform search", e);
		}
		for (Object child : resultElement.getChildren("workflow"))
			if (child instanceof Element) {
				Version.ID foundComponentIdentification = null;
				foundComponentIdentification = findComponentVersionWithWorkflow((Element) child);
				if (foundComponentIdentification != null)
					result.add(foundComponentIdentification);
			}
		return result;
	}

	private Version.ID findComponentVersionWithWorkflow(Element wfElement)
			throws RegistryException {
		String resourceUri = wfElement.getAttributeValue("resource");
		for (Family f : this.getComponentFamilies())
			for (Component c : f.getComponents())
				for (Version cv : c.getComponentVersionMap().values())
					if (cv instanceof MyExperimentComponentVersion)
						if (((MyExperimentComponentVersion) cv)
								.hasWorkflowUri(resourceUri))
							return new ComponentVersionIdentification(
									this.getRegistryBase(), f.getName(),
									c.getName(), cv.getVersionNumber());
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof MyExperimentComponentRegistry) {
			MyExperimentComponentRegistry other = (MyExperimentComponentRegistry) o;
			return getRegistryBaseString()
					.equals(other.getRegistryBaseString());
		}
		return false;
	}

	private static final int BASEHASH = MyExperimentComponentRegistry.class
			.hashCode();

	@Override
	public int hashCode() {
		return BASEHASH ^ getRegistryBaseString().hashCode();
	}

	@Override
	public String getRegistryTypeName() {
		return "Legacy API";
	}
}
