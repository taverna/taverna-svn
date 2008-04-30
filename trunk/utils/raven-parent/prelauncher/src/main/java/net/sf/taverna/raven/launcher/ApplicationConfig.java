package net.sf.taverna.raven.launcher;

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
import java.util.Map.Entry;

public class ApplicationConfig {

	private static ApplicationConfig instance;

	protected static final String PREFIX = "raven.launcher.";
	protected static final String APP_NAME = PREFIX + "app.name";
	protected static final String APP_TITLE = PREFIX + "app.title";
	protected static final String PROPERTIES = "raven-launcher.properties";

	private static final boolean DEBUG = false;

	public static synchronized ApplicationConfig getInstance() {
		if (instance == null) {
			instance = new ApplicationConfig();
		}
		return instance;
	}

	private File applicationStartupDir;

	private Properties properties;

	protected ApplicationConfig() {
	}

	public String getApplicationName() {
		String name = (String) getApplicationProperties().get(APP_NAME);
		if (name == null) {
			throw new IllegalStateException("Can't find application name");
		}
		return name;
	}

	public synchronized Properties getApplicationProperties() {
		if (properties == null) {
			properties = loadProperties(PROPERTIES);
			// Fill in from system properties
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

	public synchronized File getApplicationStartupDir() throws IOException {
		if (applicationStartupDir != null) {
			return applicationStartupDir;
		}
		File bootstrapDir;
		bootstrapDir = BootstrapLocation.getBootstrapDir();
		if (bootstrapDir.getName().equalsIgnoreCase("lib")) {
			return bootstrapDir.getParentFile();
		}
		applicationStartupDir = bootstrapDir;
		return applicationStartupDir;
	}

	public String getApplicationTitle() {
		String title = (String) getApplicationProperties().get(APP_TITLE);
		if (title == null) {
			return getApplicationName();
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
	 * determined by {@link #getApplicationStartupDir()}, and
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
			File startupDir = getApplicationStartupDir();
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

}
