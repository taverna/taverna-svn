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
package net.sf.taverna.raven.appconfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import net.sf.taverna.raven.prelauncher.ClassLocation;

/**
 * Represent the application config as it has been specified in
 * {@value #PROPERTIES}. This configuration specifies which application is to be
 * launched by {@link net.sf.taverna.raven.launcher.Launcher}, what is it's name
 * and title, etc.
 * <p>
 * An application built using {@link net.sf.taverna.raven.launcher.Launcher}
 * would typically provide the {@value #PROPERTIES} file on the classpath under
 * a <code>conf</code> directory, or in a <code>conf</code> directory in the
 * application's distribution directory (assuming that {@link ApplicationConfig}
 * is loaded from a JAR-file placed in the directory <code>lib</code> below the
 * distribution directory).
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class ApplicationConfig {

	public static final String UNKNOWN_APPLICATION = "unknownApplication-"
			+ UUID.randomUUID().toString();
	public static final String PREFIX = "raven.launcher.";
	public static final String APP_MAIN = PREFIX + "app.main";
	public static final String APP_NAME = PREFIX + "app.name";
	public static final String APP_TITLE = PREFIX + "app.title";
	public static final String APP_USER_HOME = PREFIX + "app.home";
	public static final String APP_LOCAL_REPOSITORY = PREFIX
			+ "repository.local";
	public static final String APP_USING_RAVEN = PREFIX + "use_raven";
	private static final String APP_SHOW_SPLASHSCREEN = PREFIX
			+ "show_splashscreen";
	public static final String PROPERTIES = "raven-launcher.properties";
	private static final boolean DEBUG = false;

	private static class Singleton {
		public final static ApplicationConfig instance = new ApplicationConfig();
	}

	public static ApplicationConfig getInstance() {
		return Singleton.instance;
	}

	private File startupDir;

	private Properties properties;

	protected ApplicationConfig() {
	}

	public String getMainClass() {
		String name = (String) getProperties().get(APP_MAIN);
		if (name == null) {
			throw new IllegalStateException(
					"Can't find application main method");
		}
		return name;
	}

	public String getName() {
		String name = (String) getProperties().get(APP_NAME);
		if (name == null) {
			System.err
					.println("ApplicationConfig could not determine application name, using "
							+ UNKNOWN_APPLICATION);
			return UNKNOWN_APPLICATION;
			// throw new IllegalStateException("Can't find application name");
		}
		return name;
	}

	public synchronized Properties getProperties() {
		if (properties == null) {
			properties = loadProperties(PROPERTIES);
			// Fill in overrides from system properties
			for (Entry<Object, Object> property : System.getProperties()
					.entrySet()) {
				String key = (String) property.getKey();
				if (key.startsWith(PREFIX)) {
					properties.put(key, property.getValue());
				}
			}
		}
		return properties;
	}

	public String getTitle() {
		String title = (String) getProperties().get(APP_TITLE);
		if (title == null) {
			return getName();
		}
		return title;
	}

	private void findInClassLoader(List<URI> configs, ClassLoader classLoader,
			String resourcePath) {
		Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(resourcePath);
		} catch (IOException ex) {
			System.err.println("Error looking for " + resourcePath + " in "
					+ classLoader);
			ex.printStackTrace();
			return;
		}
		while (resources.hasMoreElements()) {
			URL configURL = resources.nextElement();
			try {
				configs.add(configURL.toURI());
			} catch (URISyntaxException ex) {
				throw new RuntimeException("Invalid URL from getResource(): "
						+ configURL, ex);
			}
		}
	}

	public synchronized File getStartupDir() throws IOException {
		if (startupDir != null) {
			return startupDir;
		}
		File bootstrapDir;
		bootstrapDir = ClassLocation.getClassLocationDir(getClass());
		if (bootstrapDir.getName().equalsIgnoreCase("lib")) {
			return bootstrapDir.getParentFile();
		}
		startupDir = bootstrapDir;
		return startupDir;
	}

	/**
	 * Attempt to load application properties from propertyFileName.
	 * <p>
	 * Will attempt to load a property file from the locations below. The first
	 * non-empty properties successfully loaded will be returned.
	 * <ol>
	 * <li>$startup/conf/$resourceName</li>
	 * <li>$startup/$resourceName</li>
	 * <li>$contextClassPath/conf/$resourceName</li>
	 * <li>$contextClassPath/$resourceName</li>
	 * <li>$classpath/conf/$resourceName</li>
	 * <li>$classpath/$resourceName</li>
	 * </ol>
	 * <p>
	 * Where <code>$startup</code> is this application's startup directory as
	 * determined by {@link #getStartupDir()}, and
	 * <code>$contextClassPath</code> means a search using
	 * {@link ClassLoader#getResources(String)} from the classloader returned by
	 * {@link Thread#getContextClassLoader()} and then again
	 * <code>$classpath</code> for the classloader of {@link #getClass()} of
	 * this instance.
	 * </p>
	 * <p>
	 * If none of these sources could find a non-empty property file, a warning
	 * is logged, and an empty {@link Properties} instance is returned.
	 * 
	 * @param resourceName
	 *            Relative filename of property file
	 * 
	 * @return Loaded or empty {@link Properties} instance.
	 */
	protected Properties loadProperties(String resourceName) {
		// Ordered list of config locations to attempt to load
		// properties from
		List<URI> configs = new ArrayList<URI>();

		try {
			File startupDir = getStartupDir();
			configs.add(startupDir.toURI().resolve("conf/").resolve(
					resourceName));
			configs.add(startupDir.toURI().resolve(resourceName));

		} catch (IOException e) {
			System.err.println("Can't find startup directory");
			e.printStackTrace();
		}

		ClassLoader contextClassLoader = Thread.currentThread()
				.getContextClassLoader();
		findInClassLoader(configs, contextClassLoader, "conf/" + resourceName);
		findInClassLoader(configs, contextClassLoader, resourceName);

		findInClassLoader(configs, getClass().getClassLoader(), "conf/"
				+ resourceName);
		findInClassLoader(configs, getClass().getClassLoader(), resourceName);

		Properties loadedProps = new Properties();
		for (URI config : configs) {
			try {
				InputStream inputStream = config.toURL().openStream();
				loadedProps.load(inputStream);
			} catch (MalformedURLException ex) {
				throw new RuntimeException("Invalid URL from URI: " + config,
						ex);
			} catch (IOException ex) {
				continue; // Probably not found/access denied
			}
			if (!loadedProps.isEmpty()) {
				if (DEBUG)
					System.out.println("Loaded " + resourceName + " from "
							+ config);
				return loadedProps;
			}
		}
		if (DEBUG)
			System.err.println("Could not find application properties file "
					+ resourceName);
		return loadedProps;
	}

	/**
	 * Get the application's user home, if it has been preconfigured.
	 * <p>
	 * See {@link ApplicationUserHome#getAppUserHome()} that will also calculate
	 * the user's home directory for most normal cases.
	 * 
	 * @return
	 */
	public String getApplicationHome() {
		return getProperties().getProperty(APP_USER_HOME);
	}

	public String getLocalRavenRepository() {
		return getProperties().getProperty(APP_LOCAL_REPOSITORY);
	}

	public boolean isUsingRaven() {
		// Note - the default is "false" if the property has not been set
		// or has been set to an invalid value
		return Boolean.parseBoolean(getProperties()
				.getProperty(APP_USING_RAVEN));
	}

	public boolean isShowingSplashscreen() {
		return Boolean.parseBoolean(getProperties().getProperty(
				APP_SHOW_SPLASHSCREEN));
	}

}
