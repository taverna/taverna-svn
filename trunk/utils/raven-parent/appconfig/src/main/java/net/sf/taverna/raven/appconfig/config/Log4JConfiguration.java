/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.raven.appconfig.config;

import java.net.URL;
import java.util.Properties;

import net.sf.taverna.raven.appconfig.bootstrap.AbstractConfiguration;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.log.Log4jLog;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log4JConfiguration extends AbstractConfiguration {

	private MyGridConfiguration mygridConfig = null;

	private static Logger logger = Logger.getLogger(Log4JConfiguration.class);

	// log4j property filename/resource name
	public final static String LOG4J_PROPERTIES = "log4j.properties";

	public final static String MINIMAL_LOG4J_PROPERTIES = "minimal-"
			+ LOG4J_PROPERTIES;
	
	public Log4JConfiguration() {
		prepareLog4J();
	}

	@Override
	protected String getConfigurationFilename() {
		return LOG4J_PROPERTIES;
	}

	private void prepareLog4J() {
		// Avoid warnings before we have loaded log4j settings. Load
		// bundled minimal log4j properties first.
		URL bundledProps = MyGridConfiguration.class.getClassLoader()
				.getResource(MINIMAL_LOG4J_PROPERTIES);
		if (bundledProps == null) {
			System.err.println("Could not find " + MINIMAL_LOG4J_PROPERTIES);
			System.setProperty("log4j.defaultInitOverride", "true");
		} else {
			PropertyConfigurator.configure(bundledProps);
		}
		logger = Logger.getLogger(MyGridConfiguration.class);
		// We can now start using other methods of MyGridConfiguration

		// So that ${taverna.logdir} will work
		System.setProperty("taverna.logdir", MyGridConfiguration.getUserDir(
				"logs").getAbsolutePath());

		// Will read our bundled log4j.properties, write out new defaults to
		// .taverna/conf/log4j.properties, and read those as well
		Properties log4jProperties = getProperties();
		if (log4jProperties==null) { //read from classpath
			log4jProperties=new Properties();
		}
		LogManager.resetConfiguration();
		PropertyConfigurator.configure(log4jProperties);

		// Let Raven use log4j through our little proxy, unless log4j has been
		// loaded
		// through Raven (that would introduce funny recursive problems)
		// (It seems to be OK to load Log4jLog through Raven)
		if (!(Logger.class.getClassLoader() instanceof LocalArtifactClassLoader)) {
			Log.setImplementation(new Log4jLog());
		} else {
			logger.warn("Cannot enable log4j logging for Raven, try "
					+ "adding log4j to profile with system='true'");
		}
		Profile profile = ProfileFactory.getInstance().getProfile();
		if (profile != null) {
			logger.info("Starting " + profile.getName() + " v"
					+ profile.getVersion());
			logger.debug("Containing artifacts:");
			for (Artifact a : profile.getArtifacts()) {
				logger.debug(a);
			}
		} else {
			logger.info("No profile found");
		}
	}

}
