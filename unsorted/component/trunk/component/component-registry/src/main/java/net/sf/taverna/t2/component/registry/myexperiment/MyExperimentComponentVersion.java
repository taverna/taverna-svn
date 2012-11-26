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

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentVersion;

import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentComponentVersion implements ComponentVersion {

	private final MyExperimentComponentRegistry componentRegistry;
	private final MyExperimentComponent component;
	private final String uri;

	private Integer versionNumber;
	private String description;
	private String dataflow;

	public MyExperimentComponentVersion(MyExperimentComponentRegistry componentRegistry,
			MyExperimentComponent component, String uri) {
		this.componentRegistry = componentRegistry;
		this.component = component;
		this.uri = uri;
	}

	@Override
	public Integer getVersionNumber() {
		if (versionNumber == null) {
			Element resource = componentRegistry.getResource(uri);
			if (resource != null) {
				versionNumber = Integer.getInteger(resource.getAttributeValue("version"));
			}
		}
		return versionNumber;
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
	public String getDataflowString() {
		if (dataflow == null)
			for (Element internalPackItem : componentRegistry.getResourceElements(uri,
					"internal-pack-items")) {
				if ("workflow".equals(internalPackItem.getName())) {
					String workflowResource = internalPackItem.getAttributeValue("resource");
					Element workflowElement = componentRegistry.getResource(workflowResource);
					dataflow = workflowElement.getChild("content-uri").getTextTrim();
				}
			}
		return dataflow;
	}

	@Override
	public Component getComponent() {
		return component;
	}

}
