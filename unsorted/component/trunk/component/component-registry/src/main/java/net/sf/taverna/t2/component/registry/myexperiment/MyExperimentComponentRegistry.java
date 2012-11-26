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

import java.util.ArrayList;
import java.util.List;

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

	private final MyExperimentClient myExperimentClient;
	private final String registryLocation;
	private final String packsUri;

	public MyExperimentComponentRegistry(String registryLocation) {
		this.registryLocation = registryLocation;
		packsUri = registryLocation + "/packs.xml";
		myExperimentClient = new MyExperimentClient(logger);
	}

	@Override
	public List<ComponentFamily> getComponentFamilies() throws ComponentRegistryException {
		List<ComponentFamily> componentFamilies = new ArrayList<ComponentFamily>();
		try {
			Element packsElement = getResource(packsUri, "tag=component%20family");
			for (Object child : packsElement.getChildren("pack")) {
				if (child instanceof Element) {
					Element packElement = (Element) child;
					componentFamilies.add(new MyExperimentComponentFamily(this, packElement.getAttributeValue("uri")));
				}
			}
		} catch (Exception e) {
			throw new ComponentRegistryException(e);
		}
		return componentFamilies;
	}

	@Override
	public ComponentFamily createComponentFamily(String name, ComponentProfile componentProfile) throws ComponentRegistryException {
		try {
			Element packElement = createPack(name);
			tagResource("component family", packElement.getAttributeValue("resource"));
			return new MyExperimentComponentFamily(this, packElement.getAttributeValue("uri"));
		} catch (Exception e) {
			throw new ComponentRegistryException();
		}
	}

	@Override
	public void addComponentFamily(ComponentFamily componentFamily) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeComponentFamily(ComponentFamily componentFamily) {
		throw new UnsupportedOperationException();
	}

	private Element createPack(String title) throws Exception {
		String packTitle = "<pack><title>" + title + "</title></pack>";
		ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(registryLocation + "/pack.xml", packTitle);
		return packResponse.getResponseBody().getRootElement();
	}

	private void tagResource(String tag, String resource) throws Exception {
		String taggingToSend = "<tagging><subject resource=\"" + resource + "\"/><label>"+tag+"</label></tagging>";
		ServerResponse taggingResponse = myExperimentClient.doMyExperimentPOST(registryLocation + "/tagging.xml", taggingToSend);
	}

	Element getResource(String uri, String... query) throws Exception {
		for (String queryElement : query) {
			if (uri.contains("?")) {
				uri +=	"&" + queryElement;
			} else {

				uri +=	"?" + queryElement;
			}
		}
		ServerResponse response = myExperimentClient.doMyExperimentGET(uri);
		return response.getResponseBody().getRootElement();
	}

	List<Element> getResourceElements(String uri, String elementName) throws Exception {
		List<Element> elements = new ArrayList<Element>();
		Element element = getResource(uri, "elements=" + elementName);
		Element packItems = element.getChild(elementName);
		for (Object child : packItems.getChildren()) {
			if (child instanceof Element) {
				elements.add((Element) child);
			}
		}
		return elements;
	}

}
