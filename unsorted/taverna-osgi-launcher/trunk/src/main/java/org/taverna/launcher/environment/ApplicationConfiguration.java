package org.taverna.launcher.environment;

import java.io.File;

/**
 * How to get information about the application.
 * 
 * @author Donal Fellows
 */
public interface ApplicationConfiguration {
	/**
	 * What is the name of the application?
	 * 
	 * @return The application name (as determined by the application JAR name).
	 */
	String getApplicationName();

	/**
	 * Where do settings associated with the application reside?
	 * 
	 * @return The settings directory (which will exist).
	 */
	File getApplicationSettingsDir();
}
