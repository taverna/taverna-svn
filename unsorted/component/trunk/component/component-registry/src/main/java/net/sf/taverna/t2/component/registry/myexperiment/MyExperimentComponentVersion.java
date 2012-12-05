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

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentComponentVersion implements ComponentVersion {

	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private final MyExperimentComponentRegistry componentRegistry;
	private final MyExperimentComponent component;
	private final String uri;

	private Integer versionNumber;
	private String description;
	private Dataflow dataflow;

	public MyExperimentComponentVersion(MyExperimentComponentRegistry componentRegistry,
			MyExperimentComponent component, String uri) {
		this.componentRegistry = componentRegistry;
		this.component = component;
		this.uri = uri;
	}

	@Override
	public Integer getVersionNumber() {
		if (versionNumber == null) {
			Element resource = MyExperimentUtils.getResource(uri);
			if (resource != null) {
				versionNumber = new Integer(resource.getAttributeValue("version"));
			}
		}
		return versionNumber;
	}

	@Override
	public String getDescription() {
		if (description == null) {
			Element descriptionElement = MyExperimentUtils.getResourceElement(uri, "description");
			if (descriptionElement == null) {
				description = "";
			}
			description = descriptionElement.getTextTrim();
		}
		return description;
	}

	@Override
	public Dataflow getDataflow() throws ComponentRegistryException {
		if (dataflow == null) {
			Element workflowElement = MyExperimentUtils.getInternalPackItem(uri, "workflow");
			String workflowUri = workflowElement.getAttributeValue("uri");
			String version = workflowElement.getAttributeValue("version");
			Element contentUriElement = MyExperimentUtils.getResourceElement(workflowUri+"&version="+version, "content-uri");
			String dataflowUri = contentUriElement.getTextTrim();
			try {
				DataflowInfo info = FileManager.getInstance().openDataflowSilently(T2_FLOW_FILE_TYPE, new URL(dataflowUri));
				dataflow = info.getDataflow();
			} catch (OpenException e) {
				throw new ComponentRegistryException("Unable to open dataflow from " + dataflowUri, e);
			} catch (MalformedURLException e) {
				throw new ComponentRegistryException("Unable to open dataflow from " + dataflowUri, e);
			}
		}
		return dataflow;
	}

	@Override
	public Component getComponent() {
		return component;
	}

}
