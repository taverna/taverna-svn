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
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentComponentVersion extends ComponentVersion {
	
	private static Logger logger = Logger.getLogger(MyExperimentComponentVersion.class);



	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private final MyExperimentComponentRegistry componentRegistry;
	private final String uri;

	private Integer versionNumber;
	private String description;

	public MyExperimentComponentVersion(MyExperimentComponentRegistry componentRegistry,
			Component component, String uri) {
		super(component);
		this.componentRegistry = componentRegistry;
		this.uri = uri;
	}

	@Override
	protected final Integer internalGetVersionNumber() {
		if (versionNumber == null) {
			Element resource = componentRegistry.getResource(uri);
			if (resource != null) {
				versionNumber = new Integer(resource.getAttributeValue("version"));
			}
		}
		return versionNumber;
	}

	@Override
	protected final String internalGetDescription() {
		logger.info("Getting workflow from myExperiment");
		if (description == null) {
			try {
				Element workflowElement = componentRegistry.getPackItem(uri, "workflow");
				String resourceUri = workflowElement.getAttributeValue("uri");
				Element descriptionElement = componentRegistry.getResourceElement(resourceUri, "description");
				description = descriptionElement.getTextTrim();
			} catch (ComponentRegistryException e) {
				logger.error(e);
				return ("");
			}
		}
		return description;
	}

	@Override
	protected final Dataflow internalGetDataflow() throws ComponentRegistryException {
		Element workflowElement = componentRegistry.getPackItem(uri, "workflow");
			String resourceUri = workflowElement.getAttributeValue("resource");
			resourceUri = StringUtils.substringBeforeLast(resourceUri, "?");
			String version = workflowElement.getAttributeValue("version");
			String downloadUri = resourceUri + "/download?version=" + version;
			T2DataflowOpener opener = new T2DataflowOpener();
			
			DataflowInfo info;
			try {
				info = opener.openDataflow(T2_FLOW_FILE_TYPE, new URL(downloadUri));
			} catch (OpenException e) {
				logger.error(e);
				throw new ComponentRegistryException("Unable to open dataflow", e);
			} catch (MalformedURLException e) {
				logger.error(e);
				throw new ComponentRegistryException("Unable to open dataflow", e);
			}

//			try {
//				DataflowInfo info = FileManager.getInstance().openDataflowSilently(T2_FLOW_FILE_TYPE, new URL(downloadUri));
//				dataflow = info.getDataflow();
//			} catch (OpenException e) {
//				System.err.println("Unable to open dataflow from " + downloadUri);
//				throw new ComponentRegistryException("Unable to open dataflow from " + downloadUri, e);
//			} catch (MalformedURLException e) {
//				System.err.println("Unable to open dataflow from " + downloadUri);
//				throw new ComponentRegistryException("Unable to open dataflow from " + downloadUri, e);
//			}
//			catch (Exception e) {
//				System.err.println("Unable to open dataflow from " + downloadUri);
//				throw new ComponentRegistryException("Unable to open dataflow from " + downloadUri, e);
//			}
		return info.getDataflow();
	}

	public boolean hasWorkflowUri(String resourceUri) throws ComponentRegistryException {
		Element workflowElement = null;
		try {
			workflowElement = componentRegistry.getPackItem(uri, "workflow");
			if (workflowElement == null) {
				return false;
			}
		}
		catch (ComponentRegistryException e) {
			logger.error(e);
			return false;
		}
		String wfUri = workflowElement.getAttributeValue("resource");
		wfUri = StringUtils.substringBeforeLast(wfUri, "?");
		return wfUri.equals(resourceUri);
	}

}
