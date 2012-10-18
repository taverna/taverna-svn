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
package uk.org.taverna.osgi.starter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import uk.org.taverna.osgi.OsgiLauncher;
import uk.org.taverna.platform.data.api.DataService;
import uk.org.taverna.platform.run.api.RunService;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
import uk.org.taverna.scufl2.api.io.WorkflowBundleReader;
import uk.org.taverna.scufl2.api.io.WorkflowBundleWriter;

/**
 *
 *
 * @author David Withers
 */
public class TavernaStarter {

	private static final String scufl2Version = "0.11.0";

	private static final String systemPackages =
			"uk.org.taverna.platform.data.api;version=1.0.0," +
			"uk.org.taverna.platform.execution.api;version=1.0.0," +
			"uk.org.taverna.platform.run.api;version=1.0.0," +
			"uk.org.taverna.configuration.app;version=0.1.1," +
			"uk.org.taverna.scufl2.api.activity;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.annotation;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.common;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.configurations;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.container;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.core;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.dispatchstack;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.io;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.io.structure;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.iterationstrategy;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.port;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.profiles;version="+scufl2Version+"," +
			"uk.org.taverna.scufl2.api.property;version="+scufl2Version+"," +
			"net.sf.taverna.t2.security.credentialmanager;version=2.0.1," +
			"net.sf.taverna.t2.lang.observer;version=2.0.1," +
			"uk.org.taverna.platform.report;version=0.1.3," +
			"org.apache.log4j;version=1.2.16";

	private OsgiLauncher osgiLauncher;
	private BundleContext context;
	private DataService dataService;
	private RunService runService;
	private CredentialManager credentialManager;
	private WorkflowBundleIO workflowBundleIO;

	public TavernaStarter(File storageDirectory) throws IOException {
		URL bundleList = getClass().getClassLoader().getResource("config/taverna.osgi.bundles");
		BufferedReader bundleListReader = new BufferedReader(new InputStreamReader(
				bundleList.openStream()));
		String[] bundles = bundleListReader.readLine().split(",");
		List<URI> bundlesToInstall = new ArrayList<URI>();
		for (String bundle : bundles) {
			URL bundleURL = getClass().getClassLoader().getResource(bundle);
			try {
				bundlesToInstall.add(bundleURL.toURI());
			} catch (URISyntaxException ex) {
				throw new RuntimeException("Invalid URL from getResource(): " + bundleURL, ex);
			}
		}
		osgiLauncher = new OsgiLauncher(storageDirectory, bundlesToInstall);
		osgiLauncher.addBootDelegationPackages("org.xml.*,org.w3c.*");
		osgiLauncher.setCleanStorageDirectory(true);
		osgiLauncher.addSystemPackages(systemPackages);
	}


	public void start() throws BundleException {
		osgiLauncher.start();
		context = osgiLauncher.getContext();
		osgiLauncher.startServices(true);
	}

	public void stop() throws BundleException, InterruptedException {
		osgiLauncher.stop();
	}

	/**
	 * Returns the context.
	 *
	 * @return the context
	 */
	public BundleContext getContext() {
		return context;
	}

	/**
	 * Returns the dataService.
	 *
	 * @return the dataService
	 */
	public DataService getDataService() {
		if (dataService == null && context != null) {
			ServiceReference serviceReference = context
					.getServiceReference("uk.org.taverna.platform.data.api.DataService");
			if (serviceReference == null) {
				System.out.println("Can't find DataService");
			} else {
				dataService = (DataService) context.getService(serviceReference);
			}
		}
		return dataService;
	}

	/**
	 * Returns the runService.
	 *
	 * @return the runService
	 */
	public RunService getRunService() {
		if (runService == null && context != null) {
			ServiceReference serviceReference = context
					.getServiceReference("uk.org.taverna.platform.run.api.RunService");
			if (serviceReference == null) {
				System.out.println("Can't find RunService");
			} else {
				runService = (RunService) context.getService(serviceReference);
			}
		}
		return runService;
	}

	/**
	 * Returns the credentialManager.
	 *
	 * @return the credentialManager
	 */
	public CredentialManager getCredentialManager() {
		if (credentialManager == null && context != null) {
			ServiceReference serviceReference = context
					.getServiceReference("net.sf.taverna.t2.security.credentialmanager.CredentialManager");
			if (serviceReference == null) {
				System.out.println("Can't find CredentialManager");
			} else {
				credentialManager = (CredentialManager) context.getService(serviceReference);
			}
		}
		return credentialManager;
	}

	public WorkflowBundleIO getWorkflowBundleIO() {
		if (workflowBundleIO == null && context != null) {
			List<WorkflowBundleReader> workflowBundleReaders = new ArrayList<WorkflowBundleReader>();
			List<WorkflowBundleWriter> workflowBundleWriteers = new ArrayList<WorkflowBundleWriter>();
			ServiceReference[] serviceReferences = null;
			try {
				serviceReferences = context.getServiceReferences(
						"uk.org.taverna.scufl2.api.io.WorkflowBundleReader", null);
			} catch (InvalidSyntaxException e) {
			}
			if (serviceReferences == null) {
				System.out.println("Can't find WorkflowBundleReaders");
			} else {
				for (ServiceReference serviceReference : serviceReferences) {
					workflowBundleReaders.add((WorkflowBundleReader) context.getService(serviceReference));
				}
			}
			try {
				serviceReferences = context.getServiceReferences(
						"uk.org.taverna.scufl2.api.io.WorkflowBundleReader", null);
			} catch (InvalidSyntaxException e) {
			}
			if (serviceReferences == null) {
				System.out.println("Can't find WorkflowBundleReaders");
			} else {
				for (ServiceReference serviceReference : serviceReferences) {
					workflowBundleReaders.add((WorkflowBundleReader) context.getService(serviceReference));
				}
			}
			workflowBundleIO = new WorkflowBundleIO();
			workflowBundleIO.setReaders(workflowBundleReaders);
			workflowBundleIO.setWriters(workflowBundleWriteers);
		}
		return workflowBundleIO;
	}

}
