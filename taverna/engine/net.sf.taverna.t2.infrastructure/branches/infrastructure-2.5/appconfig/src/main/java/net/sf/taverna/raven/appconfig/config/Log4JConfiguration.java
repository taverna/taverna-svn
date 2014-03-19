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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.appconfig.bootstrap.AbstractConfiguration;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.log.Log4jLog;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;

public class Log4JConfiguration extends AbstractConfiguration {
	public final static String LOG4J_PROPERTIES = "log4j.properties";
	
	protected static class Singleton {
		protected static Log4JConfiguration instance = new Log4JConfiguration();
	}
	
	protected static boolean log4jConfigured = false;
	
	protected Log4JConfiguration() {
		prepareLog4J();
	}

	public static Log4JConfiguration getInstance() {
		return Singleton.instance;
	}
	
	public void prepareLog4J() {
		if (log4jConfigured) {
			return;
		}
		LogManager.resetConfiguration();
		//BasicConfigurator.configure();
		
		
		Properties log4jProperties = getProperties();
		if (log4jProperties != null && ! log4jProperties.isEmpty()) {
			LogManager.resetConfiguration();
			PropertyConfigurator.configure(log4jProperties);
			//System.out.println("Loaded log4j settings:");
			//log4jProperties.save(System.out, "");
		}

		String logFilePath = getLogFile().getAbsolutePath();
		PatternLayout layout = new PatternLayout("%-5p %d{ISO8601} (%c:%L) - %m%n");

		// Add file appender
		RollingFileAppender appender;
		try {
			appender = new RollingFileAppender(layout, logFilePath);
			appender.setMaxFileSize("1MB");
			appender.setEncoding("UTF-8");
			appender.setMaxBackupIndex(4);
			// Let root logger decide level
			appender.setThreshold(Level.ALL);
			LogManager.getRootLogger().addAppender(appender);
		} catch (IOException e) {
			System.err.println("Could not log to " + logFilePath);
		}
		
		
		
		if (! (Log.getImplementation() instanceof Log4jLog)) {
			Log.setImplementation(new Log4jLog());
		}
		log4jConfigured = true;
		// FIXME: Why is this here?
		// Profile profile = ProfileFactory.getInstance().getProfile();
		
	}

	public File getLogFile() {
		return new File(getLogDir(), ApplicationConfig.getInstance().getName() + ".log");
	}

	public File getLogDir() {
		File logDir = new File(ApplicationRuntime.getInstance().getApplicationHomeDir(), "logs");
		logDir.mkdirs();
		if (!logDir.isDirectory()) {
			throw new IllegalStateException(
					"Could not create log directory " + logDir);
		}
		return logDir;
	}
	

	@Override
	protected String getConfigurationFilename() {
		return LOG4J_PROPERTIES;
	}

}
