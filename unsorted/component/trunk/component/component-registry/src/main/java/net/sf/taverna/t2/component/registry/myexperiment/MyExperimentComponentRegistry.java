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

	private final URL registryURL;

	private List<ComponentFamily> componentFamilies;
	private List<ComponentProfile> componentProfiles;

	private MyExperimentComponentRegistry(URL registryURL) {
		this.registryURL = registryURL;
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
			Element packsElement = MyExperimentUtils.getResource(MyExperimentUtils.urlToString(registryURL) + "/packs.xml", "tag=component%20family");
			for (Object child : packsElement.getChildren("pack")) {
				if (child instanceof Element) {
					Element packElement = (Element) child;
					String packUri = packElement.getAttributeValue("uri");
					if (MyExperimentUtils.getResource(packUri) != null) {
						componentFamilies.add(new MyExperimentComponentFamily(this, packUri));
					}
				}
			}
		}
		return componentFamilies;
	}

	@Override
	public ComponentFamily createComponentFamily(String name, ComponentProfile componentProfile) throws ComponentRegistryException {
		Element packElement = MyExperimentUtils.createPack(registryURL, name);
		MyExperimentUtils.tagResource(registryURL, "component family", packElement.getAttributeValue("resource"));
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

	@Override
	public URL getRegistryBase() {
		return registryURL;
	}

	@Override
	public List<ComponentProfile> getComponentProfiles() {
		if (componentProfiles == null) {
			componentProfiles = new ArrayList<ComponentProfile>();
			Element filesElement = MyExperimentUtils.getResource(MyExperimentUtils.urlToString(registryURL) + "/files.xml", "tag=component%20profile");
			for (Object child : filesElement.getChildren("file")) {
				if (child instanceof Element) {
					Element fileElement = (Element) child;
					String fileUri = fileElement.getAttributeValue("uri");
					if (MyExperimentUtils.getResource(fileUri) != null) {
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
