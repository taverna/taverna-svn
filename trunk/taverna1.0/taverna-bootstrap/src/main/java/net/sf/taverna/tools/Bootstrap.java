package net.sf.taverna.tools;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

public class Bootstrap {
		
	public static Properties properties = findProperties();
	
	public static Properties findProperties() {
		Properties properties = new Properties();
		String propsName = "/raven.properties";
		InputStream props = Bootstrap.class.getResourceAsStream(propsName);
		if (props == null) {
			System.err.println("Could not find " + propsName);
			System.exit(1);
		}
		try {
			properties.load(props);
		} catch (IOException e) {
			System.err.println("Could not load " + propsName);
			System.exit(2);
		}
		// Allow overriding any of those on command line
		properties.putAll(System.getProperties());
		return properties;
	}
	
	public static URL[] remoteRepositories = findRepositories(properties);	

	public static URL[] findRepositories(Properties properties) {
		// entries are named raven.repository.2 = http:// .. 
		// We'll add these in order as stated (not as in property file)
		String prefix = "raven.repository.";
		ArrayList<URL> urls = new ArrayList<URL>();
		for (Entry property : properties.entrySet()) {
			String propName = (String) property.getKey();
			if (! propName.startsWith(prefix)) {
				continue;
			}
			String propValue = (String) property.getValue();
			URL url;
			try {
				url = new URL(propValue);
			} catch (MalformedURLException e1) {
				System.err.println("Ignoring invalid URL " + propValue);
				continue;
			}
			int position;
			try {
				position = Integer.valueOf(propName.replace(prefix, "")); 
			} catch (NumberFormatException e) {
				// Just ignore the position
				System.err.println("Invalid URL position " + propName);
				urls.add(url);
				continue;
			}
			// Fill up with null's if we are to insert way out there
			while (position >= urls.size()) {
				urls.add(null);
			}
			// .add(pos, url) makes sure we don't overwrite anything
			urls.add(position, url);
		}
		// Check if .m2/repository is there, add it
		File mavenRep = new File(System.getProperty("user.home"), ".m2/repository/");
		if (mavenRep.isDirectory()) {
			try {
				// We'll put it in 1, not 0, so that raven.repository.0 can come first
				urls.add(1, mavenRep.toURI().toURL());
			} catch (MalformedURLException e) {
				System.err.println("Invalid maven repository: " + mavenRep);
			}	
		}
		// Remove nulls and export as URL[]
		while (urls.remove(null)) {}
		return urls.toArray(new URL[0]);
	} 
	
	
	// Where Raven will store its repository, discovered by main()
	public static String TAVERNA_CACHE = "";
	
	public static final String RAVEN_VERSION = properties.getProperty("raven.loader.version");
	public static final String SPLASHSCREEN = properties.getProperty("raven.splashscreen.url");
	// Application name, as in $HOME/.taverna 
	private final static String APPLICATION = "Taverna";
	
	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		findUserDir();
		File cacheDir = findCache();
		if (cacheDir == null) {
			return;
		}
		
		// Create a remote classloader referencing the raven jar within a repository
		
		String artifactLocation = artifactURI(
				properties.getProperty("raven.loader.groupid"), 
				properties.getProperty("raven.loader.artifactid"), 
				properties.getProperty("raven.loader.version"));
		List<URL> loaderURLs = new ArrayList<URL>();
		for (URL repository : remoteRepositories) {
			loaderURLs.add(new URL(repository, artifactLocation));
		}
		ClassLoader c = new URLClassLoader(
				loaderURLs.toArray(new URL[0]),
				null);

		String useSplashProp = properties.getProperty("raven.splashscreen");
		boolean useSplashscreen = !(useSplashProp == null || useSplashProp.equalsIgnoreCase("false"));
		
		// Reference to the Loader class within net.sf.taverna.raven
		Class loaderClass = c.loadClass(properties.getProperty("raven.loader.class"));
		// Find the single static method provided by the loader
		Method m = loaderClass.getDeclaredMethod(
				properties.getProperty("raven.loader.method"),
				String.class, // ravenVersion
				File.class,   // localRepositoryLocation
				URL[].class,  // remoteRepositories
				String.class, // targetGroup
				String.class, // targetArtifact
				String.class, // targetVersion
				String.class, // className
				URL.class,    // splashScreenURL
				int.class);   // splashTime
		
		// Parameters for the Raven loader call
		String ravenVersion = RAVEN_VERSION;

		// FIXME: Support other classes like WorkflowLauncher
		String groupID = properties.getProperty("raven.target.groupid");
		String artifactID = properties.getProperty("raven.target.artifactid");
		String version = properties.getProperty("raven.target.version");

		String targetClassName = properties.getProperty("raven.target.class");
		// Call method via reflection, 'null' target as this is a static method
		URL splashScreenImage = null;
		int minimumDisplayTime = 1;
		if (useSplashscreen) {
			// FIXME: Support offline splashscreen
			splashScreenImage = new URL(SPLASHSCREEN);
			minimumDisplayTime = Integer.valueOf(properties.getProperty("raven.splashscreen.timeout")) 
									* 1000; // seconds
		}
		Class workbenchClass = (Class)m.invoke(
				null,
				ravenVersion,
				cacheDir,
				remoteRepositories,
				groupID,
				artifactID,
				version,
				targetClassName,
				splashScreenImage,
				minimumDisplayTime);
		
		try {
			// Try m(String[] args) first
			Method workbenchStatic = workbenchClass.getMethod(
					 properties.getProperty("raven.target.method"), 
					 String[].class);
				workbenchStatic.invoke(null, new Object[]{args});
		} catch (NoSuchMethodException ex) {
			// Then with m()
			Method workbenchStatic = workbenchClass.getMethod(
				 properties.getProperty("raven.target.method"));
			workbenchStatic.invoke(null);
		}
	}

	private static String artifactURI(String groupid, String artifactid, String version) {
		String filename = artifactid + "-" + version +  ".jar";
		String groupLocation = groupid.replace(".", "/");
		return groupLocation + "/" + artifactid + "/" + version + "/" + filename;
	}

	private static File findCache() {
		String tavernaCache = System.getProperty("taverna.repository");
		File cacheDir;
		if (tavernaCache != null) {
			cacheDir = new File(tavernaCache);
		} else {
			String TAVERNA_HOME = System.getProperty("taverna.home");
			if (TAVERNA_HOME == null) {
				System.err.println("Could not locate a Taverna home / local repository");
				return null;
			}
			cacheDir = new File(TAVERNA_HOME, "repository");
		}
		cacheDir.mkdirs();
		if (! cacheDir.isDirectory()) {
			System.err.println("Not a valid repository directory: " + cacheDir);
			return null;
		}
		TAVERNA_CACHE = cacheDir.getAbsolutePath();
		return cacheDir;
	}

	/**
	 * Find and create if neccessary the user's application directory, 
	 * according to operating system standards. The resolved directory
	 * is then stored in the system property <code>taverna.home</code>
	 * <p>
	 * If the system property <code>taverna.home</code> already exists, 
	 * the directory specified by that path will be used instead and created
	 * if needed.
	 * <p>
	 * If any exception occurs (such as out of diskspace), taverna.home will be unset.
	 * 
	 * <p>
	 * On Windows, this will typically be something like:
	 * 
	 * <pre>
	 *  	C:\Document and settings\MyUsername\Application Data\MyApplication
	 * </pre>
	 * 
	 * while on Mac OS X it will be something like:
	 * 
	 * <pre>
	 *  	/Users/MyUsername/Library/Application Support/MyApplication
	 * </pre>
	 * 
	 * All other OS'es are assumed to be UNIX-alike, returning something like:
	 * 
	 * <pre>
	 *  	/user/myusername/.myapplication
	 * </pre>
	 * 
	 * <p>
	 * If the directory does not already exist, it will be created.
	 * </p>
	 * 
	 * @return System property <code>taverna.home</code> contains
	 *         path of an existing directory for Taverna user-centric
	 *         files.
	 */
	public static void findUserDir() {
		File appHome;
		String tavHome = System.getProperty("taverna.home");
		if (tavHome != null) {
			appHome = new File(tavHome);
		} else {
			File home = new File(System.getProperty("user.home"));
			if (!home.isDirectory()) {
				// logger.error("User home not a valid directory: " + home);
				return;
			}
			String os = System.getProperty("os.name");
			// logger.debug("OS is " + os);
			if (os.equals("Mac OS X")) {
				File libDir = new File(home, "Library/Application Support");
				libDir.mkdirs();
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
					// logger.warn("Could not find %APPDATA%: " + APPDATA);
					appHome = new File(home, APPLICATION);
				}
			} else {
				// We'll assume UNIX style is OK
				appHome = new File(home, "." + APPLICATION.toLowerCase());
			}
		}
		if (!appHome.exists()) {
			if (appHome.mkdir()) {
				// logger.info("Created " + appHome);
			} else {
				// logger.error("Could not create " + appHome);				
				System.clearProperty("taverna.home");
				return;
			}
		}
		if (!appHome.isDirectory()) {
			// logger.error(APPLICATION + " user home not a valid directory: "
			// + appHome);
			System.clearProperty("taverna.home");
			return;
		}
		System.setProperty("taverna.home", appHome.getAbsolutePath());
		return;
	}
}
