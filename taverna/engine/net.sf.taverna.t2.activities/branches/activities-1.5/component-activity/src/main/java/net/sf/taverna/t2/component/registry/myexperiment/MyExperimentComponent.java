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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 *
 * @author David Withers
 */
class MyExperimentComponent extends Component {

	private final MyExperimentComponentRegistry componentRegistry;
	private final MyExperimentComponentFamily componentFamily;
	private final AnnotationTools annotationTools;
	
	private final String permissionsString;
	private License license;
	private String urlString;

	public MyExperimentComponent(MyExperimentComponentRegistry componentRegistry, MyExperimentComponentFamily family,
			License license, String permissionsString, String uri) {
		super(uri);
		this.urlString = uri;
		this.componentRegistry = componentRegistry;
		this.componentFamily = family;
		this.permissionsString = permissionsString;
		this.license = license;
		annotationTools = new AnnotationTools();
	}

	@Override
	protected final String internalGetName() {
		String result = "";
			Element titleElement = componentRegistry.getResourceElement(urlString, "title");
			if (titleElement != null) {
				result = titleElement.getTextTrim();
			}
			
		return result;
	}

	@Override
	protected final String internalGetDescription() {
		String result = "";
			Element descriptionElement = componentRegistry.getResourceElement(urlString, "description");
			if (descriptionElement != null) {
				result = descriptionElement.getTextTrim();
			}
		return result;
	}

	@Override
	protected final void populateComponentVersionMap() {
		for (Element version : componentRegistry.getResourceElements(urlString, "versions")) {
			String versionUri = version.getAttributeValue("uri");
			Version componentVersion = new MyExperimentComponentVersion(componentRegistry, this, versionUri);
			versionMap.put(componentVersion.getVersionNumber(), componentVersion);
		}
	}

	@Override
	protected final MyExperimentComponentVersion internalAddVersionBasedOn(Dataflow dataflow, String revisionComment) throws RegistryException {
		return internalAddVersionBasedOn(dataflow, revisionComment, this.permissionsString);
	}

	private MyExperimentComponentVersion internalAddVersionBasedOn(Dataflow dataflow, String revisionComment, String permissionsString) throws RegistryException {
		String title = annotationTools.getAnnotationString(dataflow, DescriptiveTitle.class, "Untitled");
		String dataflowString;
		try {
			ByteArrayOutputStream dataflowStream = new ByteArrayOutputStream();
			FileManager.getInstance().saveDataflowSilently(dataflow, new T2FlowFileType(),
					dataflowStream, false);
			dataflowString = dataflowStream.toString("UTF-8");
		} catch (OverwriteException e) {
			throw new RegistryException(e);
		} catch (SaveException e) {
			throw new RegistryException(e);
		} catch (IllegalStateException e) {
			throw new RegistryException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RegistryException(e);
		}

		Element workflowElement = componentRegistry.getPackItem(urlString, "workflow");
		String versionUri = workflowElement.getAttributeValue("uri");
		String workflowUri = StringUtils.substringBeforeLast(versionUri, "&");
		Element componentWorkflow = componentRegistry.updateWorkflow(workflowUri, dataflowString,
				title, revisionComment, license, permissionsString);

		Element componentElement = componentRegistry.getResource(urlString);
		componentRegistry.deletePackItem(componentElement, "workflow");
		componentRegistry.addPackItem(componentElement, componentWorkflow);

		Element componentPack = componentRegistry.snapshotPack(urlString);
		String version = componentPack.getAttributeValue("version");
		MyExperimentComponentVersion myExperimentComponentVersion = new MyExperimentComponentVersion(componentRegistry, this, urlString+"&version="+version);
		return myExperimentComponentVersion;
	}

	@Override
	public Registry getRegistry() {
		return componentRegistry;
	}

	@Override
	public Family getFamily() {
		return componentFamily;
	}

}
