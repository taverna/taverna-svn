package org.taverna.launcher.environment.impl;

import java.io.File;

import org.taverna.launcher.Main;
import org.taverna.launcher.environment.ApplicationConfiguration;

/**
 * Simple provider of application configuration information.
 * 
 * @author Donal Fellows
 */
public class ConfigProvider implements ApplicationConfiguration {
	private Main main;

	/**
	 * Create a simple provider interface to the given main application entry
	 * point.
	 * 
	 * @param main
	 *            The application entry point.
	 */
	public ConfigProvider(Main main) {
		this.main = main;
	}

	@Override
	public String getApplicationName() {
		return main.applicationName;
	}

	@Override
	public File getApplicationSettingsDir() {
		return main.userHome;
	}
}
