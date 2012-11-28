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
import java.util.List;
import java.util.Map;

import javax.help.UnsupportedOperationException;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
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

	private static Map<URL, ComponentRegistry> componentRegistries = new HashMap<URL, ComponentRegistry>();

	private final MyExperimentClient myExperimentClient;
	private final URL registryURL;
	private String registryLocation;

	private List<ComponentFamily> componentFamilies;
	private List<ComponentProfile> componentProfiles;

	private MyExperimentComponentRegistry(URL registryURL) {
		this.registryURL = registryURL;
		registryLocation = registryURL.toString();
		if (!registryLocation.endsWith("/")) {
			registryLocation = registryLocation + "/";
		}
		myExperimentClient = new MyExperimentClient(logger);
	}

	public static ComponentRegistry getComponentRegistry(URL registryURL) {
		if (!componentRegistries.containsKey(registryURL)) {
			componentRegistries.put(registryURL, new MyExperimentComponentRegistry(registryURL));
		}
		return componentRegistries.get(registryURL);
	}

	@Override
	public List<ComponentFamily> getComponentFamilies() throws ComponentRegistryException {
		if (componentFamilies == null) {
			componentFamilies = new ArrayList<ComponentFamily>();
			Element packsElement = getResource(registryLocation + "packs.xml", "tag=component%20family");
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
	public ComponentFamily createComponentFamily(String name, ComponentProfile componentProfile) throws ComponentRegistryException {
		Element packElement = createPack(name);
		tagResource("component family", packElement.getAttributeValue("resource"));
		ComponentFamily componentFamily = new MyExperimentComponentFamily(this, packElement.getAttributeValue("uri"));
		if (componentFamilies != null) {
			componentFamilies.add(componentFamily);
		}
		return componentFamily;
	}

	@Override
	public void removeComponentFamily(ComponentFamily componentFamily) {
		throw new UnsupportedOperationException();
	}

	private Element createPack(String title) throws ComponentRegistryException {
		String packTitle = "<pack><title>" + title + "</title></pack>";
		try {
			ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(registryLocation + "pack.xml", packTitle);
			return packResponse.getResponseBody().getRootElement();
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	private void tagResource(String tag, String resource) throws ComponentRegistryException {
		String taggingToSend = "<tagging><subject resource=\"" + resource + "\"/><label>"+tag+"</label></tagging>";
		try {
			myExperimentClient.doMyExperimentPOST(registryLocation + "tagging.xml", taggingToSend);
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
	}

	Element getResource(String uri, String... query) {
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

	List<Element> getResourceElements(String uri, String elementName) {
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

	Element getResourceElement(String uri, String elementName) {
		Element element = getResource(uri, "elements=" + elementName);
		if (element == null) {
			return null;
		} else {
			return element.getChild(elementName);
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
			Element packsElement = getResource(registryLocation + "packs.xml", "tag=component%20profile");
			for (Object child : packsElement.getChildren("file")) {
				if (child instanceof Element) {
					Element fileElement = (Element) child;
					String fileUri = fileElement.getAttributeValue("uri");
					if (getResource(fileUri) != null) {
						try {
							componentProfiles.add(new ComponentProfile(new URL(fileUri)));
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
	public ComponentFamily getComponentFamily(String familyName) throws ComponentRegistryException {
		for (ComponentFamily componentFamily : getComponentFamilies()) {
			if (familyName.equals(componentFamily.getName())) {
				return componentFamily;
			}
		}
		return null;
	}

}
