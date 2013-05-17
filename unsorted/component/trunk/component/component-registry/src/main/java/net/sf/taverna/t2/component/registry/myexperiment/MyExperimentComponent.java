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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.License;
import net.sf.taverna.t2.component.registry.SharingPolicy;
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
public class MyExperimentComponent implements Component {

	private final MyExperimentComponentRegistry componentRegistry;
	private final String uri;
	private final AnnotationTools annotationTools;

	private String name;
	private String description;
	
	private SortedMap<Integer, ComponentVersion> versionCache;
	private final String permissionsString;
	private License license;

	public MyExperimentComponent(MyExperimentComponentRegistry componentRegistry,
			License license, String permissionsString, String uri) {
		this.componentRegistry = componentRegistry;
		this.uri = uri;
		this.permissionsString = permissionsString;
		this.license = license;
		annotationTools = new AnnotationTools();
	}

	@Override
	public synchronized String getName() {
		if (name == null) {
			Element titleElement = componentRegistry.getResourceElement(uri, "title");
			if (titleElement == null) {
				name = "";
			}
			name = titleElement.getTextTrim();
		}
		return name;
	}

	@Override
	public synchronized String getDescription() {
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
	public synchronized SortedMap<Integer, ComponentVersion> getComponentVersionMap() {
		if (versionCache == null) {
		versionCache = new TreeMap<Integer, ComponentVersion>();
		for (Element version : componentRegistry.getResourceElements(uri, "versions")) {
			String versionUri = version.getAttributeValue("uri");
			ComponentVersion componentVersion = new MyExperimentComponentVersion(componentRegistry, this, versionUri);
			versionCache.put(componentVersion.getVersionNumber(), componentVersion);
		}
		}
		return Collections.unmodifiableSortedMap(versionCache);
	}

	@Override
	public ComponentVersion getComponentVersion(Integer versionNumber) {
		return getComponentVersionMap().get(versionNumber);
	}

	@Override
	public MyExperimentComponentVersion addVersionBasedOn(Dataflow dataflow, String revisionComment) throws ComponentRegistryException {
		return addVersionBasedOn(dataflow, revisionComment, this.permissionsString);
	}

	public MyExperimentComponentVersion addVersionBasedOn(Dataflow dataflow, String revisionComment, String permissionsString) throws ComponentRegistryException {
		String title = annotationTools.getAnnotationString(dataflow, DescriptiveTitle.class, "Untitled");
		String dataflowString;
		try {
			ByteArrayOutputStream dataflowStream = new ByteArrayOutputStream();
			FileManager.getInstance().saveDataflowSilently(dataflow, new T2FlowFileType(),
					dataflowStream, false);
			dataflowString = dataflowStream.toString("UTF-8");
		} catch (OverwriteException e) {
			throw new ComponentRegistryException(e);
		} catch (SaveException e) {
			throw new ComponentRegistryException(e);
		} catch (IllegalStateException e) {
			throw new ComponentRegistryException(e);
		} catch (UnsupportedEncodingException e) {
			throw new ComponentRegistryException(e);
		}

		Element workflowElement = componentRegistry.getPackItem(uri, "workflow");
		String versionUri = workflowElement.getAttributeValue("uri");
		String workflowUri = StringUtils.substringBeforeLast(versionUri, "&");
		Element componentWorkflow = componentRegistry.updateWorkflow(workflowUri, dataflowString,
				title, revisionComment, license, permissionsString);

		Element componentElement = componentRegistry.getResource(uri);
		componentRegistry.deletePackItem(componentElement, "workflow");
		componentRegistry.addPackItem(componentElement, componentWorkflow);

		Element componentPack = componentRegistry.snapshotPack(uri);
		String version = componentPack.getAttributeValue("version");
		MyExperimentComponentVersion myExperimentComponentVersion = new MyExperimentComponentVersion(componentRegistry, this, uri+"&version="+version);
		if (versionCache == null) {
			getComponentVersionMap();
		}
		versionCache.put(Integer.valueOf(version), myExperimentComponentVersion);
		return myExperimentComponentVersion;
	}

	@Override
	public URL getComponentURL() {
		try {
			return new URL(uri);
		} catch (MalformedURLException e) {
			return null;
		}
	}

}
