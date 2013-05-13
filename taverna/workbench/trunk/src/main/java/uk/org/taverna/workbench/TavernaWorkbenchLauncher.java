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
package uk.org.taverna.workbench;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import uk.org.taverna.commons.profile.xml.jaxb.ApplicationProfile;
import uk.org.taverna.commons.profile.xml.jaxb.BundleInfo;
import uk.org.taverna.commons.profile.xml.jaxb.FrameworkConfiguration;
import uk.org.taverna.configuration.app.ApplicationConfiguration;
import uk.org.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import uk.org.taverna.configuration.app.impl.Log4JConfiguration;
import uk.org.taverna.osgi.OsgiLauncher;

/**
 * Launcher for the Taverna Workbench.
 *
 * @author David Withers
 */
public class TavernaWorkbenchLauncher {

	private static final String WORKBENCH_BUNDLE_NAME = "net.sf.taverna.t2.ui-impl.workbench-impl";

	private static File workbenchBundle;

	private static ApplicationConfiguration applicationConfiguration = new ApplicationConfigurationImpl();

	private static Log4JConfiguration log4jConfiguration = new Log4JConfiguration();

	private void launch() {
		try {
			log4jConfiguration.setApplicationConfiguration(applicationConfiguration);
			log4jConfiguration.prepareLog4J();
			setDerbyPaths();
			OsgiLauncher osgilauncher = new OsgiLauncher(getAppDirectory(), getBundleURIs());
			setFrameworkConfiguration(osgilauncher);
			System.out.println("Starting OSGi framework");
			osgilauncher.start();
			System.out.println("Starting OSGi services");
			osgilauncher.startServices(true);
			System.out.println("Starting workbench");
			osgilauncher.startBundle(osgilauncher.installBundle(workbenchBundle.toURI()));

//			BundleContext context = osgilauncher.getContext();
//			Bundle[] bundles = context.getBundles();
//			for (Bundle bundle : bundles) {
//				System.out.println(bundle.getSymbolicName());
//				BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
//				List<BundleCapability> capabilities = bundleWiring.getCapabilities("osgi.wiring.package");
//				for (BundleCapability capability : capabilities) {
//					System.out.println(capability.getAttributes());
//				}
//			}
		} catch (BundleException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Sets the OSGi Framework configuration.
	 *
	 * @param osgilauncher
	 */
	private void setFrameworkConfiguration(OsgiLauncher osgilauncher) {
		ApplicationProfile applicationProfile = applicationConfiguration.getApplicationProfile();
		List<FrameworkConfiguration> frameworkConfigurations = applicationProfile.getFrameworkConfiguration();
		if (!frameworkConfigurations.isEmpty()) {
			Map<String, String> configurationMap = new HashMap<String, String>();
			for (FrameworkConfiguration frameworkConfiguration : frameworkConfigurations) {
				configurationMap.put(frameworkConfiguration.getName(), frameworkConfiguration.getValue());
			}
			osgilauncher.setFrameworkConfiguration(configurationMap);
		}
	}

	private List<URI> getBundleURIs() {
		List<URI> bundleURIs = new ArrayList<URI>();
		ApplicationProfile applicationProfile = applicationConfiguration.getApplicationProfile();
		File libDir = new File(applicationConfiguration.getStartupDir(), "lib");
		if (applicationProfile != null) {
			for (BundleInfo bundle : applicationProfile.getBundle()) {
				File bundleFile = new File (libDir, bundle.getFileName());
				if (bundle.getSymbolicName().equals(WORKBENCH_BUNDLE_NAME)) {
					workbenchBundle = bundleFile;
				} else {
					bundleURIs.add(bundleFile.toURI());
				}
			}
		}
		return bundleURIs;
	}

	private File getAppDirectory() {
		return applicationConfiguration.getApplicationHomeDir();
	}

	private void setDerbyPaths() {
		System.setProperty("derby.system.home", getAppDirectory().getAbsolutePath());
		File logFile = new File(applicationConfiguration.getLogDir(), "derby.log");
		System.setProperty("derby.stream.error.file", logFile.getAbsolutePath());
	}

	/**
	 * Starts the Taverna Workbench.
	 *
	 * @param args
	 *            Taverna Command Line arguments
	 */
	public static void main(final String[] args) {
		new TavernaWorkbenchLauncher().launch();
	}

}
