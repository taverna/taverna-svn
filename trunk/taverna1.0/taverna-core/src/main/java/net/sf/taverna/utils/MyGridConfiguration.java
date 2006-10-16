package net.sf.taverna.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.python.modules.synchronize;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * myGrid configuration, such as services to load in the workbench or 
 * LSID provider to use. The configuration is in the file mygrid.properties 
 * in the classpath.
 * <p>
 * The recommended way to get the configuration is to call 
 * the static methods getProperties() and getProperty(). 
 * <p>
 * For backwards compatability, loadMygridProperties() 
 * loads the mygrid properties into the system properties. 
 * This was previously done by a static block in 
 * org.embl.ebi.escience.scuflui.workbench.Workbench. 
 * This method should be called by the main() methods to allow 
 * legacy classes to retrieve myGrid configuration.
 * 
 * @author Stian Soiland
 *
 */
public class MyGridConfiguration {
	
	static Properties properties = null;
	private final static String PROPERTIES = "mygrid.properties";
	private final static String APPLICATION = "Taverna";
	
	// Written to the top of the mygrid.properties.orig
	private final static String HEADER = 
		"# Default values are shown double-commented like this:\n" +
    	"# ## mygrid.example = value\n" +
    	"\n";		

	// For each source properties, this header will be added
	private final static String SECTION_HEADER = "# Default properties from ";
	

	private static Logger logger = Logger.getLogger(MyGridConfiguration.class);

	// Can't be instanciated, static methods only
	private MyGridConfiguration() {}
	
	/**
	 * Get the myGrid properties. The properties will be the combination of
	 * the default properties distributed with Taverna and the user specified properties.
	 * <p><strong>Note:</strong>
	 * This method will force creation/updating of local user properties files if needed. 
	 * This method will cache the loaded properties until flushProperties() is called.
	 * 
	 * @return myGrid properties
	 */
	synchronized public static Properties getProperties() {
		// FIXME: Make it possible for Raven to clear this cache
		if (properties != null) {
			return properties;
		}
		properties = loadDefaultProperties();
		writeDefaultProperties();
		properties.putAll(loadUserProperties());
		return properties;
	}
	
	/**
	 * Flush cache of properties. Next call to getProperties() or 
	 * getProperty() will force a reload from classloaders and user files 
	 *
	 */
	synchronized public static void flushProperties() {
		properties = null;
	}
	
	
	/**
	 * Find resources. The resource specified must be reachable by the
	 * class loader. Note that several hits might be returned.
	 * <p>
	 * Example:
	 * <pre>
	 * for (URL url: findResources("mygrid.properties") {
	 * 		..
	 * }
	 * </pre>
	 * 
	 * @param resourceName
	 * @return Iterable over URLs
	 */
	static Iterable<URL> findResources(final String resourceName) { 
		/*   Psevdo code:
		@classmethod
		def findDefaultResources(cls, resourceName):
		    loader = cls.class.classLoader or Thread.currentThread().contextClassLoader
		    try:
		        resources = loader.getResources(resourceName)
		    except IOException:
		        throw NoSuchElementException    
		    while resources.hasMoreElements():
		        yield resources.nextElement()
		*/
		
		// 15 layers of Iterable vs. Iterator courtesy of Sun
		return new Iterable<URL>() {
			public Iterator<URL> iterator() {
				// FIXME: Shouldn't we always use context class loader?
				ClassLoader loader = MyGridConfiguration.class.getClassLoader();
				if (loader == null) {
					loader = Thread.currentThread().getContextClassLoader();
				}
				Enumeration<URL> tempResources;
				try {
					tempResources = loader.getResources(resourceName);
				} catch (IOException e) {
					logger.error("Could not find " + resourceName, e);
					tempResources = null;
				} 
				final Enumeration<URL> resources = tempResources;
				return new Iterator<URL>() {
					public boolean hasNext() {
						return resources != null && resources.hasMoreElements();
					}
					public URL next() {
						if (resources == null) {
							throw new NoSuchElementException();
						}
						return resources.nextElement();
					}
					public void remove() {
						throw new NotImplementedException();
					}					
				};
			}
			
		};
	}

	
	/**
	 * Get the default myGrid properties as distributed with Taverna.
	 * <p>
	 * The property object returned is freshly created (and loaded) for 
	 * each call and can be modified without affecting
	 * later calls.
	 * 
	 * @return Default properties as provided by classloader
	 */
	public static Properties loadDefaultProperties() {
		Properties properties = new Properties();
		// Concatinate all resources
		for (URL resource : findResources(PROPERTIES)) {
			logger.info("Loading resources from " + resource.toString());
			try {
				properties.load(resource.openStream());
			} catch (IOException e) {
				logger.warn("Could not read properties from " + resource, e);
			}
		}
		return properties;
	}

	/**
	 * Get the user's application directory according to operating system.
	 * 
	 * <p>
	 * On Windows, this will typically be something like:
	 * 	<pre>
	 * 	C:\Document and settings\MyUsername\Application Data\MyApplication
	 * 	</pre>
	 * while on Mac OS X it will be something like:
	 * 	<pre>
	 * 	/Users/MyUsername/Library/Application Support/MyApplication
	 * 	</pre>
	 * All other OS'es are assumed to be UNIX-alike, returning something like:
	 * 	<pre>
	 * 	/user/myusername/.myapplication
	 * 	</pre>
	 * 
	 * <p>
	 * If the directory does not already exist, it will be created. 
	 * </p>
	 * 
	 * @return <code>File</code> object representing the OS-specific user
	 *         directoryfor this application, or <code>null</code> if it 
	 *         could not be found or created.
	 */
	public static File getUserDir() {
		File home = new File(System.getProperty("user.home"));
		if (! home.isDirectory()) {
			logger.error("User home not a valid directory: " + home);
			return null;
		}
		String os = System.getProperty("os.name");
		logger.debug("OS is " + os);
		File appHome;
		
		if (os.equals("Mac OS X")) {
			File libDir = new File(home, "Library/Application Support");
			if (! libDir.isDirectory()) {
				logger.warn("Could not find Application support directory:" + libDir);
				// We'll make it, we could be the first one
				new File(home, "Library").mkdir();
				libDir.mkdir();
			}
			appHome = new File(libDir, APPLICATION);
			
		} else if (os.startsWith("Windows")) {
			String APPDATA = System.getenv("APPDATA");
			File appData = null;
			if (APPDATA != null) {
				appData = new File(APPDATA);
			}
			if (appData != null && appData.isDirectory()) {
				appHome = new File(appData, APPLICATION);
			} else {
				logger.warn("Could not find %APPDATA%: " + APPDATA);
				appHome = new File(home, APPLICATION);
			}
			
		} else {
			// We'll assume UNIX style is OK
			appHome = new File(home, "." + APPLICATION.toLowerCase());
		}
		
		if (! appHome.exists()) {
			if (appHome.mkdir()) {
				logger.info("Created " + appHome);
			} else {
				logger.error("Could not create " + appHome);
				return null;
			}
		}
		if (! appHome.isDirectory()) {
			logger.error(APPLICATION + " user home not a valid directory: " 
					     + appHome);
			return null;
		}
		return appHome;
	}
	
	/**
	 * Get a subdirectory of the user's application directory.
	 * <p>
	 * Like getUserDir(), but one level deeper. For instance, 
	 * getUserDir("conf") on UNIX would return the file of
	 * <code>/user/myusername/.myapplication/conf</code> and assure
	 * that both <code>.myapplication</code> and <code>conf</code> are
	 * created.
	 * 
	 * @see getUserDir()
	 * @param subDirectory
	 * @return
	 */
	public static File getUserDir(String subDirectory) {
		File dir = new File(getUserDir(), subDirectory);
		if (! dir.isDirectory()) {
			if (dir.mkdir()) {
				logger.info("Created " + dir);
			} else {
				logger.error("Could not create " + dir);
				return null;
			}
		}
		return dir;
	}
	
	
	public static Properties loadUserProperties() {
		Properties userProps = new Properties();
		File confDir = getUserDir("conf");
		File propertyFile = new File(confDir, PROPERTIES);
		if (! propertyFile.isFile()) {
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
	 * 
	 * Look up a myGrid property.
	 * <p>
	 * This is a shorthand for getProperties().getProperty(key). Use this instead of
	 * System.getProperty("taverna.X"), which depends on loadMygridProperties() to 
	 * have been called.
	 * 
	 * @param key 
	 * @return value 
	 */
	public static String getProperty(String key) {
		Properties props = getProperties();
		if (props == null) {
			logger.warn("mygrid properties not found, returning null for " + key);
			return null;
		}
		return props.getProperty(key);
	}

	
	/**
	 * Load the myGrid properties and store them into the System properties.
	 * <p>
	 * This is provided for backwards compatibility <em>only</em>, as old code relies 
	 * on System.getProperties() to be prepopulated with the myGrid configuration.
	 * <p>
	 * Do not rely on properties to have been loaded globally, instead use 
	 * MyGridConfiguration.getProperties()
	 *
	 * @see MyGridConfiguration.getProperties()
	 *
	 */
	@Deprecated
	synchronized public static void loadMygridProperties() {
		Properties myGridProps = MyGridConfiguration.getProperties();
		if (myGridProps == null) {
			logger.warn("mygrid properties not found and initialized, expect lots of NullPointerExceptions");
			return;
		}
		Properties sysProps = System.getProperties();
		sysProps.putAll(myGridProps);
	}

	public static void writeDefaultProperties() {
		File confDir = getUserDir("conf");
		File propOrig = new File(confDir, PROPERTIES+".orig");
		File prop = new File(confDir, PROPERTIES);
		// If it does not exist, we can replace it
		boolean replaceProp = ! prop.canRead();
		try {
			if (propOrig.canRead() && FileUtils.contentEquals(propOrig, prop)) {
				// Not changed, replace it
				replaceProp = true;
			}
		} catch (IOException e) {
			logger.warn("Could not compare " + propOrig + " with " + prop, e);
		}
		propOrig.delete();  // Delete if exist
		if (replaceProp) {
			prop.delete();
		}
		
		// Write out mygrid.properties.orig
		try {
			Writer propWriter = new BufferedWriter(new FileWriter(propOrig));
			propWriter.write(HEADER);
			for (URL resource : findResources(PROPERTIES)) {
				writeDefaults(resource, propWriter);
			}
			propWriter.close();
		} catch (IOException e) {
			logger.error("Could not write default properties to " + propOrig, e);
			// Delete it as it might be incomplete and should be rewritten
			propOrig.delete();
			return;
		}
		
		// Upgrade mygrid.properties unless there exists a user-modified version
		if (replaceProp && propOrig.isFile()) {
			try {
				FileUtils.copyFile(propOrig, prop);
			} catch (IOException e) {
				logger.error("Could not copy " + propOrig + " to " + prop, e);
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
	 * except active lines (non-comments) will be double-commented (##) 
	 * as to indicate the defaults. A header is added including the source URL.
	 * <p>
	 * If the source cannot be read, it will be silently ignored with a log message.
	 * 
	 * @param source The URL to the source property file to append
	 * @param destination An already opened Writer instance for appending. Will not be closed on exit.
	 * @throws IOException If writing to the destination fails.
	 */
	private static void writeDefaults(URL source, Writer destination) throws IOException {
		LineIterator lines;
		try {
			lines  = IOUtils.lineIterator(source.openStream(), "utf8");
		} catch (IOException e) {
			logger.warn("Could not read " + source, e);
			return;
		}
		
		destination.write(SECTION_HEADER + source + "\n");
		while (lines.hasNext()) {
			String line = lines.nextLine();
			if (! line.equals("") && ! line.startsWith("#")) {
				// Comment out non-comments
				line = "##" + line;
			}
			destination.write(line + "\n");
		}
	}

}
