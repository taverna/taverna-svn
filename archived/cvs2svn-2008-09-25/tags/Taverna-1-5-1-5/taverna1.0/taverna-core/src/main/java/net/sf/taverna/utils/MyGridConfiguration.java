package net.sf.taverna.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.log.Log4jLog;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.tools.Bootstrap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * myGrid configuration, such as services to load in the workbench or LSID
 * provider to use. The configuration is in the file mygrid.properties in the
 * classpath.
 * <p>
 * The recommended way to get the configuration is to call the static methods
 * getProperties() and getProperty().
 * <p>
 * For backwards compatability, loadMygridProperties() loads the mygrid
 * properties into the system properties. This was previously done by a static
 * block in org.embl.ebi.escience.scuflui.workbench.Workbench. This method
 * should be called by the main() methods to allow legacy classes to retrieve
 * myGrid configuration.
 * 
 * @author Stian Soiland
 * 
 */
public class MyGridConfiguration {
	
	public static final String CONFIGURATION_DIRECTORY = "conf";

    // Cache of properties for getProperty() and getProperties()  (clear with flushProperties() )
	static Properties properties = null;

	// mygrid property filename/resource name
	public final static String MYGRID_PROPERTIES = "mygrid.properties";

	// log4j property filename/resource name
	public final static String LOG4J_PROPERTIES = "log4j.properties";
	
	public final static String MINIMAL_LOG4J_PROPERTIES = "minimal-" + LOG4J_PROPERTIES;
	
	// Written to the top of the generated mygrid.properties.dist
	private final static String HEADER = 
		  "# Default values are shown for your convenience like this:\n"
		+ "# ## mygrid.example = value\n" 
		+ "# To change such values, remove ## and edit the value\n\n" ;

	// For each resource, this header will be added to mygrid.properties.dist
	private final static String SECTION_HEADER = "# Default properties from ";

	// Initialized by static block
	private static Logger logger;

	/**
     * Prepare log4j and loadMyGridProperties()
	 */
	static {
		prepareLog4J();
		loadMygridProperties();
	}

	
	/**
	 * Can't be instanciated, static methods only
	 * 
	 */
	private MyGridConfiguration() {
		// FIXME: Make possible to make a MyGridConfiguration("mygrid.properties") - this
		// can then also be used with say log4j.properties.
	}

	/**
	 * Load log4j.properties
	 * 
	 */
	
	private static void prepareLog4J() {
		// Avoid warnings before we have loaded log4j settings. Load 
		// bundled minimal log4j properties first.
		URL bundledProps = MyGridConfiguration.class.getClassLoader().getResource(
			MINIMAL_LOG4J_PROPERTIES);
		if (bundledProps == null) {
			System.err.println("Could not find " + MINIMAL_LOG4J_PROPERTIES);
			System.setProperty("log4j.defaultInitOverride", "true");
		} else {
			PropertyConfigurator.configure(bundledProps);
		}
		logger = Logger.getLogger(MyGridConfiguration.class);
		// We can now start using other methods of MyGridConfiguration
		
		// So that ${taverna.logdir} will work
		System.setProperty("taverna.logdir", 
			getUserDir("logs").getAbsolutePath());
		
		
		// Will read our bundled log4j.properties, write out new defaults to 
		// .taverna/conf/log4j.properties, and read those as well
		Properties log4jProperties = getProperties(LOG4J_PROPERTIES);
		LogManager.resetConfiguration();
		PropertyConfigurator.configure(log4jProperties);
				
		// Let Raven use log4j through our little proxy, unless log4j has been loaded
		// through Raven (that would introduce funny recursive problems)
		// (It seems to be OK to load Log4jLog through Raven)
		if (! (Logger.class.getClassLoader() instanceof LocalArtifactClassLoader)) {
			Log.setImplementation(new Log4jLog());
		} else {
			logger.warn("Cannot enable log4j logging for Raven, try " +
					"adding log4j to profile with system='true'");
		}
		Profile profile = ProfileFactory.getInstance().getProfile();
		if (profile!=null) {
			logger.info("Starting " + profile.getName() + " v" + profile.getVersion());
			logger.debug("Containing artifacts:");
			for (Artifact a : profile.getArtifacts()) {
				logger.debug(a);
			}
		}
		else {
			logger.info("No profile found");
		}
	}

	/**
	 * Look up a myGrid property.
	 * <p>
	 * This is a shorthand for <code>getProperties().getProperty(key)</code>. 
	 * Use this instead of the legacy <code>System.getProperty("taverna.X")</code>,
	 * which depends on <code>loadMygridProperties()</code> being previously called.
	 * 
	 * @param key
	 * @return value
	 */
	public static String getProperty(String key) {
		Properties props = getProperties();
		if (props == null) {
			logger.warn("mygrid properties not found, returning null for "
					+ key);
			return null;
		}
		return props.getProperty(key);
	}
	
	/**
	 * Look up a myGrid property.
	 * <p>
	 * Like getProperty(String key) - but return <code>def</code>
	 * if the key is unknown.
	 * 
	 * @param key
	 * @param def Default string if key not found
	 * @return
	 */
	public static String getProperty(String key, String def) {
		String value = getProperty(key);
		if (value == null) {
			return def;
		} 
		return value;
	}

	/**
	 * Get all of the myGrid properties. The properties will be the combination 
	 * of the default properties distributed with Taverna and the user specified
	 * properties.
	 * <p>
	 * <strong>Note:</strong> This method will force creation/updating of local
	 * user properties files if needed. This method will cache the loaded
	 * properties until flushProperties() is called.
	 * 
	 * @return myGrid properties
	 */
	synchronized public static Properties getProperties() {
		// FIXME: Make it possible for Raven to clear this cache
		if (properties != null) {
			return properties;
		}
		properties = getProperties(MYGRID_PROPERTIES);
		// FIXME: Support overriding through system properties (but for this to
		// work we can no longer do loadMygridProperties() - as that would mean 
		// we will get the old values even after flushProperties(). 
		
		// Also, we don't want to put non-Taverna properties (like java.*)
		// into this property object. How to do it? Something like..
		// for key,value in SystemProperties.entrySet():
		//    if key in properties:  // Known mygrid key
		//           properties.put(key, value);
		return properties;
	}
	
	/**
	 * Get all properties for the given resource name. The properties will be the combination 
	 * of the default properties distributed with Taverna and the user specified
	 * properties.
	 * <p>
	 * <strong>Note:</strong> This method will force creation/updating of local
	 * user properties files if needed.
	 * 
	 * @param name Resource name, like "mygrid.properties"
	 * @param classLoader Optional {@link ClassLoader} to search for property
	 * @return properties specified by name
	 */
	public static Properties getProperties(String name) {
		return getProperties(name, null);
	}
	
	/**
	 * Get all properties for the given resource name. The properties will be the combination 
	 * of the default properties distributed with Taverna and the user specified
	 * properties.
	 * <p>
	 * <strong>Note:</strong> This method will force creation/updating of local
	 * user properties files if needed.
	 * 
	 * @param name Resource name, like "mygrid.properties"
	 * @param classLoader Optional {@link ClassLoader} to include in search for resource
	 * @return properties specified by name
	 */
	public static Properties getProperties(String name, ClassLoader classLoader) {
		Properties properties = loadDefaultProperties(name, classLoader);
		writeDefaultProperties(name, classLoader);
		properties.putAll(loadUserProperties(name));
		return properties;
	}

	/**
	 * Flush cache of properties. Next call to getProperties() or getProperty()
	 * will force a reload from classloaders and user files.
	 * 
	 */
	synchronized public static void flushProperties() {
		properties = null;
	}

	/**
	 * Load the myGrid properties and store them into the System properties. 
	 * This function should be called as early as possible in your program.
	 * <p>
	 * This is provided for backwards compatibility <em>only</em>, as old
	 * code relies on System.getProperties() to be prepopulated with the myGrid
	 * configuration.
	 * <p>
	 * Do not rely on properties to have been loaded globally, instead use
	 * MyGridConfiguration.getProperties()
	 * 
	 * @see MyGridConfiguration.getProperties()
	 * 
	 */
	@Deprecated
	synchronized public static void loadMygridProperties() {
		Properties myGridProps = getProperties();
		if (myGridProps == null) {
			logger.warn("mygrid properties not found/initialized");
			return;
		}
		Properties sysProps = System.getProperties();
		sysProps.putAll(myGridProps);
	}

	/**
	 * Get the user's application directory according to taverna.home.
	 * <p>
	 * The system property <code>taverna.home</code> is assumed to have been
	 * set by Bootstrap.findUserDir() or externally. The directory is created
	 * if needed.
	 * 
	 * @see net.sf.taverna.tools.Bootstrap.findUserDir()
	 * @return <code>File</code> object representing the user
	 *         directory for this application, or <code>null</code> if it could
	 *         not be found or created.
	 */
	public static File getUserDir() {
		String tavernaHome = System.getProperty("taverna.home");
		if (tavernaHome == null) {
			Bootstrap.findUserDir();
			tavernaHome = System.getProperty("taverna.home");
			if (tavernaHome == null) {
				logger.error("Could not find Taverna home. Try setting -Dtaverna.home");
				return null;
			}
		}
		File dir = new File(tavernaHome);
		dir.mkdirs();
		if (! dir.isDirectory()) {
			logger.warn("Invalid taverna.home directory " + dir);
			return null;
		}
		return dir;
	}

	/**
	 * Get a subdirectory of the user's application directory.
	 * <p>
	 * Like getUserDir(), but one level deeper. For instance, getUserDir("conf")
	 * on UNIX would return the file of
	 * <code>/user/myusername/.myapplication/conf</code> and assure that both
	 * <code>.myapplication</code> and <code>conf</code> are created.
	 * 
	 * @see getUserDir()
	 * @param subDirectory
	 * @return
	 */
	public static File getUserDir(String subDirectory) {
		File dir = new File(getUserDir(), subDirectory);
		dir.mkdirs();
		if (! dir.isDirectory()) {
			logger.warn("Could not create directory " + dir);
			return null;
		}
		return dir;
	}

	/**
	 * Load user properties from .taverna/conf/mygrid.properties. 
	 * If user properties are not found, an empty property object 
	 * is returned.
	 * 
	 * @return Properties as loaded from user directory
	 */
	static Properties loadUserProperties(String resourceName) {
		Properties userProps = new Properties();
		File confDir = getUserDir(CONFIGURATION_DIRECTORY);
		File propertyFile = new File(confDir, resourceName);
		if (!propertyFile.isFile()) {
			return userProps;
		}
		try {
			userProps.load(propertyFile.toURL().openStream());
		} catch (MalformedURLException e) {
			logger.error("Invalid property location " + propertyFile, e);
		} catch (IOException e) {
			logger.warn("Could not load user properties", e);
		}
		return userProps;
	}

	/**
	 * Get the default myGrid properties as distributed with Taverna.
	 * <p>
	 * The property object returned is freshly created (and loaded) for each
	 * call and can be modified without affecting later calls.
	 * 
	 * @return Default properties as provided by classloader
	 */
	static Properties loadDefaultProperties(String resourceName) {
		return loadDefaultProperties(resourceName, null);
	}
	
	/**
	 * Get the default myGrid properties as distributed with Taverna.
	 * <p>
	 * The property object returned is freshly created (and loaded) for each
	 * call and can be modified without affecting later calls.
	 *
	 * @param resourceName Name of resource to be loaded
 	 * @param classLoader Optional {@link ClassLoader} to include in search for resource
	 * @return Default properties as provided by classloader
	 */
	static Properties loadDefaultProperties(String resourceName, ClassLoader classLoader) {
		Properties properties = new Properties();
		// Concatinate all resources
		for (URL resource : findResources(resourceName, classLoader)) {
			logger.info("Loading resources from " + resource);
			try {
				properties.load(resource.openStream());
			} catch (IOException e) {
				logger.warn("Could not read properties from " + resource, e);
			}
		}
		if (properties.isEmpty()) {
			logger.warn("Empty properties " + resourceName);
		}
		return properties;
	}
	
	/**
	 * Write default properties to user directory. .taverna/conf/NAME.dist will
	 * always be created/overwritten, which will be a dump of the properties as
	 * in getDefaultProperties(name). However, all active lines will be
	 * commented out with ##.
	 * <p>
	 * In addition, .taverna/conf/NAME will be written if it does not exist, or
	 * if the existing copy matched the old .dist version.
	 * 
	 * @param resourceName
	 *            Name of resource which properties are to be written, like
	 *            "mygrid.properties"
	 */
	static void writeDefaultProperties(String resourceName) {
		writeDefaultProperties(resourceName, null);
	}
	
	/**
	 * Write default properties to user directory. .taverna/conf/NAME.dist will
	 * always be created/overwritten, which will be a dump of the properties as
	 * in getDefaultProperties(name). However, all active lines will be
	 * commented out with ##.
	 * <p>
	 * In addition, .taverna/conf/NAME will be written if it does not exist, or
	 * if the existing copy matched the old .dist version.
	 * 
	 * @param resourceName
	 *            Name of resource which properties are to be written, like
	 *            "mygrid.properties"
	 * @param classLoader
	 *            Optional {@link ClassLoader} to include in search for resource
	 */
	static void writeDefaultProperties(String resourceName, ClassLoader classLoader) {
		File confDir = getUserDir(CONFIGURATION_DIRECTORY);
		File propDist = new File(confDir, resourceName + ".dist");
		File prop = new File(confDir, resourceName);
		boolean replaceProp; 
		try {
			// If it does not exist, or is the same as the .dist, we can replace it
			replaceProp = !prop.exists() || FileUtils.contentEquals(propDist, prop) ;
		} catch (IOException e) {
			logger.warn("Could not compare " + propDist + " with " + prop, e);
			// Better play safe and don't touch it
			replaceProp = false;
		}
		propDist.delete(); // Delete if exist
		if (replaceProp) {
			prop.delete();
		}

		// Write out blah.properties.dist
		try {
			Writer propWriter = new BufferedWriter(new FileWriter(propDist));
			propWriter.write(HEADER);
			for (URL resource : findResources(resourceName, classLoader)) {
				writeDefaults(resource, propWriter);
			}
			propWriter.close();
		} catch (IOException e) {
			logger
			.error("Could not write default properties to " + propDist,
					e);
			// Delete it as it might be incomplete and should be rewritten
			propDist.delete();
			return;
		}

		// Upgrade blah.properties unless there exists a user-modified version
		if (replaceProp && propDist.isFile()) {
			try {
				FileUtils.copyFile(propDist, prop);
			} catch (IOException e) {
				logger.error("Could not copy " + propDist + " to " + prop, e);
				// Delete it so that we can try again next time
				prop.delete();
				return;
			}
			logger.info("Replaced " + prop);
		} else {
			logger.info("Did not replace " + prop);
		}
	}

	/**
	 * Write default properties from the given URL.
	 * <p>
	 * The source will be appended to the destination writer line by line,
	 * except active lines (non-comments) will be double-commented (##) as to
	 * indicate the defaults. A header is added including the source URL.
	 * <p>
	 * If the source cannot be read, it will be silently ignored with a log
	 * message.
	 * 
	 * @param source
	 *            The URL to the source property file to append
	 * @param destination
	 *            An already opened Writer instance for appending. Will not be
	 *            closed on exit.
	 * @throws IOException
	 *             If writing to the destination fails.
	 */
	static void writeDefaults(URL source, Writer destination)
	throws IOException {
		LineIterator lines;
		try {
			lines = IOUtils.lineIterator(source.openStream(), "utf8");
		} catch (IOException e) {
			logger.warn("Could not read " + source, e);
			return;
		}

		destination.write(SECTION_HEADER + source + "\n");
		while (lines.hasNext()) {
			String line = lines.nextLine();
			if (!line.equals("") && !line.startsWith("#")) {
				// Comment out non-comments
				line = "##" + line;
			}
			destination.write(line + "\n");
		}
	}

	/**
	 * Find resources. The resource specified must be reachable by the class
	 * loader. Note that several URLs might be returned.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 *  for (URL url: findResources(&quot;mygrid.properties&quot;) {
	 *  		..
	 *  }
	 * </pre>
	 * 
	 * @param resourceName Resource to find
	 * @param classLoader Optional {@link ClassLoader} to include in search for resource
	 * @return Collection of URLs
	 */
	static Collection<URL> findResources(String resourceName, ClassLoader classLoader) {
		// Avoid finding it twice
		LinkedHashSet<URL> resources = new LinkedHashSet<URL>();

		// We will check in both our own class loader and the Thread's
		// classloader. NOTE: might contain null
		List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();		
		classLoaders.add(MyGridConfiguration.class.getClassLoader());
		classLoaders.add(Thread.currentThread().getContextClassLoader());
		classLoaders.add(classLoader);
		for (ClassLoader loader : classLoaders) {
			if (loader == null) {
				continue;
			}
			Enumeration<URL> tempResources;
			try {
				tempResources = loader.getResources(resourceName);
			} catch (IOException e) {
				logger.warn("Could not find " + resourceName + " in " + loader, e);
				continue;
			}
			while (tempResources.hasMoreElements()) {
				resources.add(tempResources.nextElement());
			}
		}
		return resources;
	}

}
