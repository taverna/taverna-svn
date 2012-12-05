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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.ServerResponse;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 *
 */
public class MyExperimentUtils {

	private static final Logger logger = Logger.getLogger(MyExperimentComponentRegistry.class);
	private static final MyExperimentClient myExperimentClient = new MyExperimentClient(logger);;

	public static Element createPack(URL registryURL, String title) throws ComponentRegistryException {
		String packTitle = "<pack><title>" + title + "</title></pack>";
		try {
			ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/pack.xml", packTitle);
			return packResponse.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	public static void addPackItem(URL registryURL, String packResource, String itemResource) throws ComponentRegistryException {
		StringBuilder item = new StringBuilder();
		item.append("<internal-pack-item>");
		item.append("<pack resource=\"");
		item.append(packResource);
		item.append("\"/>");
		item.append("<item resource=\"");
		item.append(itemResource);
		item.append("\">");
		item.append("</internal-pack-item>");
		try {
			myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/internal-pack-item.xml", item.toString());
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	public static Element getInternalPackItem(String packUri, String item, String... tags) throws ComponentRegistryException {
		for (Element internalPackItem : MyExperimentUtils.getResourceElements(packUri, "internal-pack-items")) {
			if (item.equals(internalPackItem.getName())) {
				String internalPackItemUri = internalPackItem.getAttributeValue("uri");
				Element itemElement = MyExperimentUtils.getResourceElement(internalPackItemUri, "item");
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

	public static boolean hasTags(String uri, String... tags) {
		if (tags != null && tags.length > 0) {
			Set<String> resourceTags = new HashSet<String>();
			for (Element tagElement : MyExperimentUtils.getResourceElements(uri, "tags")) {
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

	public static Element uploadWorkflow(URL registryURL, String dataflow, String sharing) throws ComponentRegistryException {
		myExperimentClient.setBaseURL(urlToString(registryURL));
		ServerResponse postWorkflowResponse = myExperimentClient.postWorkflow(dataflow, "", "", "", sharing);
		return postWorkflowResponse.getResponseBody().getRootElement();
	}

	public static void tagResource(URL registryURL, String tag, String resource) throws ComponentRegistryException {
		String taggingToSend = "<tagging><subject resource=\"" + resource + "\"/><label>"+tag+"</label></tagging>";
		try {
			myExperimentClient.doMyExperimentPOST(urlToString(registryURL) + "/tagging.xml", taggingToSend);
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	public static Element getResource(String uri, String... query) {
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

	public static List<Element> getResourceElements(String uri, String elementName) {
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

	public static Element getResourceElement(String uri, String elementName) {
		Element element = getResource(uri, "elements=" + elementName);
		if (element == null) {
			return null;
		} else {
			return element.getChild(elementName);
		}
	}

	public static String urlToString(URL url) {
		String urlString = url.toString();
		if (urlString.endsWith("/")) {
			urlString = urlString.substring(0, urlString.length() - 1);
		}
		return urlString;
	}

}
