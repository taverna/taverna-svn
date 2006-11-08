package net.sf.taverna.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Bootstrap {

	public static Properties properties = findProperties();

	// Where Raven will store its repository, discovered by main()
	public static String TAVERNA_CACHE = "";

	public static URL[] remoteRepositories = findRepositories(properties);

	private static String loaderVersion;
	
	private static final String SPLASHSCREEN = "splashscreen-1.5.png";

	public static void main(String[] args) throws MalformedURLException,
			ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		findUserDir();

		if (properties.getProperty("raven.remoteprofile") != null) {
			initialiseProfile(properties.getProperty("raven.remoteprofile"));
		}

		List<URL> loaderURLs = getLoaderUrls();

		Method loaderMethod = createLoaderMethod(loaderURLs);

		Class workbenchClass = createWorkbenchClass(loaderVersion, loaderMethod);

		addSystemLoaderArtifacts();

		invokeWorkbench(args, workbenchClass);
	}

	private static void addSystemLoaderArtifacts() throws MalformedURLException {
		ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
		if (systemClassLoader instanceof BootstrapClassLoader) {
			BootstrapClassLoader bootstrapClassLoader = (BootstrapClassLoader) systemClassLoader;
			URL cacheURL = findCache().toURI().toURL();			

			try {
				String localProfile = System.getProperty("raven.profile");
				if (localProfile != null) {
					URL url = new URL(localProfile);
					DocumentBuilder builder = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder();
					Document doc = builder.parse(url.toURI().toURL().openStream());
					NodeList nodes = doc.getElementsByTagName("artifact");
					for (int i = 0; i < nodes.getLength(); i++) {
						Node node = nodes.item(i);
						NamedNodeMap attributes = node.getAttributes();
						Node systemNode = attributes.getNamedItem("system");
						if (systemNode != null && "true".equals(systemNode.getNodeValue())) {
							Node groupNode = attributes.getNamedItem("groupId");
							Node artifactNode = attributes.getNamedItem("artifactId");
							Node versionNode = attributes.getNamedItem("version");
							if (groupNode != null && artifactNode != null && versionNode != null) {
								bootstrapClassLoader.addURL(new URL(cacheURL, artifactURI(groupNode.getNodeValue(),
										artifactNode.getNodeValue(), versionNode.getNodeValue())));
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Properties findProperties() {
		Properties props = new Properties();
		String propsName = "/raven.properties";
		InputStream propStream = Bootstrap.class.getResourceAsStream(propsName);
		if (propStream == null) {
			System.err.println("Could not find " + propsName);
			System.exit(1);
		}
		try {
			props.load(propStream);
		} catch (IOException e) {
			System.err.println("Could not load " + propsName);
			System.exit(2);
		}
		// Allow overriding any of those on command line
		props.putAll(System.getProperties());
		return props;
	}

	public static URL[] findRepositories(Properties props) {
		// entries are named raven.repository.2 = http:// ..
		// We'll add these in order as stated (not as in property file)
		String prefix = "raven.repository.";
		ArrayList<URL> urls = new ArrayList<URL>();
		for (Entry property : props.entrySet()) {
			String propName = (String) property.getKey();
			if (!propName.startsWith(prefix)) {
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
		// We'll put it in 1, not 0, so that raven.repository.0 can 
		// override .m2/repository
		urls.add(1, findLocalMavenRepository());
		// Remove nulls and export as URL[]
		while (urls.remove(null)) {
			// nothing
		}
		return urls.toArray(new URL[0]);
	}

	/** 
	 * Find URL to local .m2/repository or return null.
	 */
	private static URL findLocalMavenRepository() {
		File mavenRep = new File(System.getProperty("user.home"),
				".m2/repository/");
		if (! mavenRep.isDirectory()) {
			return null;
		}
		try {
			return mavenRep.toURI().toURL();
		} catch (MalformedURLException e) {
			System.err.println("Invalid maven repository: " + mavenRep);
			return null;
		}
	}

	private static void invokeWorkbench(String[] args, Class workbenchClass)
			throws IllegalAccessException, NoSuchMethodException {
		try {
			try {
				// Try m(String[] args) first
				Method workbenchStatic = workbenchClass.getMethod(properties
						.getProperty("raven.target.method"), String[].class);
				workbenchStatic.invoke(null, new Object[] { args });
			} catch (NoSuchMethodException ex) {
				// Then with m()
				Method workbenchStatic = workbenchClass.getMethod(properties
						.getProperty("raven.target.method"));
				workbenchStatic.invoke(null);
			}
		} catch (InvocationTargetException e) {
			String methodName = workbenchClass
					+ properties.getProperty("raven.target.method");
			System.err.println("Exception occured in " + methodName);
			e.getCause().printStackTrace();
			System.exit(5);
		}
	}

	private static Class createWorkbenchClass(String ravenVersion,
			Method loaderMethod) throws MalformedURLException,
			IllegalAccessException {
		Class workbenchClass = null;
		File cacheDir = findCache();
		String useSplashProp = properties.getProperty("raven.splashscreen");
		boolean useSplashscreen = useSplashProp != null && 
			! useSplashProp.equalsIgnoreCase("false");

		String groupID = properties.getProperty("raven.target.groupid");
		String artifactID = properties.getProperty("raven.target.artifactid");
		String version = properties.getProperty("raven.target.version");
		String targetClassName = properties.getProperty("raven.target.class");

		if (properties.getProperty("raven.remoteprofile") != null) {
			String targetVersion = getProfileArtifactVersion(groupID,
					artifactID);
			if (targetVersion != null) {
				version = targetVersion;
			}
		}

		//System.out.println("Using version " + version + " of " + groupID + ":"
			//	+ artifactID);

		// Call method via reflection, 'null' target as this is a static method
		URL splashScreenURL = null;
		int splashScreenTime = 1;
		if (useSplashscreen) {
			splashScreenURL = getSplashScreenURL();			
			splashScreenTime = Integer.valueOf(properties
					.getProperty("raven.splashscreen.timeout")) * 1000; // seconds
		}

		try {
			workbenchClass = (Class) loaderMethod.invoke(null, ravenVersion,
					cacheDir, remoteRepositories, groupID, artifactID, version,
					targetClassName, splashScreenURL, splashScreenTime);
		} catch (InvocationTargetException e) {
			System.err.println("Could not launch Raven");
			e.getCause().printStackTrace();
			System.exit(4);
		}
		return workbenchClass;
	}

	private static URL getSplashScreenURL() throws MalformedURLException{
		return Bootstrap.class.getResource("/"+SPLASHSCREEN);
	}	
	
	private static List<URL> getLoaderUrls() throws MalformedURLException {
		File cacheDir = findCache();

		if (cacheDir == null) {
			System.err.println("Unable to create repository directory");
			System.exit(-1);
			return null;
		}

		// Create a remote classloader referencing the raven jar within a
		// repository
		String loaderGroupId = properties.getProperty("raven.loader.groupid");
		String loaderArtifactId = properties
				.getProperty("raven.loader.artifactid");
		loaderVersion = properties.getProperty("raven.loader.version");
		if (properties.getProperty("raven.remoteprofile") != null) {
			String version = getProfileArtifactVersion(loaderGroupId,
					loaderArtifactId);
			if (version != null) {
				loaderVersion = version;
			}
		}

		//System.out.println("Using version " + loaderVersion + " of "
			//	+ loaderGroupId + ":" + loaderArtifactId);
		String artifactLocation = artifactURI(loaderGroupId, loaderArtifactId,
				loaderVersion);

		// Use our local repository if possible
		List<URL> loaderURLs = new ArrayList<URL>();
		// loaderURLs.add(new URL(cacheDir.toURI().toURL(),artifactLocation));
		loaderURLs.add(cacheDir.toURI().toURL());
		for (URL repository : remoteRepositories) {
			loaderURLs.add(new URL(repository, artifactLocation));
		}
		return loaderURLs;
	}

	private static Method createLoaderMethod(List<URL> loaderURLs)
			throws ClassNotFoundException, NoSuchMethodException {
		Method loaderMethod;
		ClassLoader c = new URLClassLoader(loaderURLs.toArray(new URL[0]), null);

		// override with system classloader if running in eclipse
		if (properties.getProperty("raven.eclipse") != null) {
			c = ClassLoader.getSystemClassLoader();
		}

		// Reference to the Loader class within net.sf.taverna.raven
		Class loaderClass = c.loadClass(properties
				.getProperty("raven.loader.class"));
		// Find the single static method provided by the loader
		loaderMethod = loaderClass.getDeclaredMethod(properties
				.getProperty("raven.loader.method"), String.class, // ravenVersion
				File.class, // localRepositoryLocation
				URL[].class, // remoteRepositories
				String.class, // targetGroup
				String.class, // targetArtifact
				String.class, // targetVersion
				String.class, // className
				URL.class, // splashScreenURL
				int.class); // splashTime
		return loaderMethod;
	}

	private static String artifactURI(String groupid, String artifactid,
			String version) {
		String filename = artifactid + "-" + version + ".jar";
		String groupLocation = groupid.replace(".", "/");
		return groupLocation + "/" + artifactid + "/" + version + "/"
				+ filename;
	}

	private static File findCache() {
		String tavernaCache = System.getProperty("taverna.repository");
		File cacheDir;
		if (tavernaCache != null) {
			cacheDir = new File(tavernaCache);
		} else {
			String TAVERNA_HOME = System.getProperty("taverna.home");
			if (TAVERNA_HOME == null) {
				System.err
						.println("Could not locate a Taverna home / local repository");
				return null;
			}
			cacheDir = new File(TAVERNA_HOME, "repository");
		}
		cacheDir.mkdirs();
		if (!cacheDir.isDirectory()) {
			System.err.println("Not a valid repository directory: " + cacheDir);
			return null;
		}
		TAVERNA_CACHE = cacheDir.getAbsolutePath();
		return cacheDir;
	}

	/**
	 * Checks for local profile, whos name is dervied from raven.remoteprofile
	 * and the user dir. If not exists then copies the bundled default profile.
	 * The property raven.profile is set to the locally stored profile If
	 * default profile cannot be accessed then raven.profile and
	 * raven.remoteprofile property is cleared, disabling the profile
	 * 
	 * @param profileURL
	 */
	private static void initialiseProfile(String profileUrlStr) {
		System.setProperty("raven.remoteprofile", profileUrlStr);
		File localProfile = getLocalProfileFile(profileUrlStr);
		try {
			System.setProperty("raven.profile", localProfile.toURI().toURL()
					.toString());
			if (!localProfile.exists()) {
				storeDefaultProfile(localProfile);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// disable profile. May be better to just bail out completely here.
			System.clearProperty("raven.profile");
			System.clearProperty("raven.remoteprofile");
		}

	}

	private static File getLocalProfileFile(String profileUrlStr) {
		File tavernaHome = new File(System.getProperty("taverna.home"));
		File userdir = new File(tavernaHome, "conf");
		String fileStr = profileUrlStr;
		if (fileStr.contains("/")) {
			int i = fileStr.lastIndexOf("/");
			fileStr = fileStr.substring(i + 1);
		}
		File profileFile = new File(userdir, fileStr);
		return profileFile;
	}

	private static void storeDefaultProfile(File localProfile)
			throws Exception, IOException, URISyntaxException {
		InputStream defaultStream = Bootstrap.class
				.getResourceAsStream("/default-profile.xml");
		if (defaultStream == null)
			throw new Exception("Unable to find default profile");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				defaultStream));
		BufferedWriter writer = new BufferedWriter(new FileWriter(localProfile));
		String line = reader.readLine();
		while (line != null) {
			writer.write(line + "\n");
			line = reader.readLine();
		}
		writer.flush();
		writer.close();

		reader.close();
	}

	private static String getProfileArtifactVersion(String groupId,
			String artifactId) {
		String result = null;
		try {
			String localProfile = System.getProperty("raven.profile");
			if (localProfile != null) {
				URL url = new URL(localProfile);
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(url.toURI().toURL().openStream());
				NodeList nodes = doc.getElementsByTagName("artifact");
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					Node artifactNode = node.getAttributes().getNamedItem(
							"artifactId");
					if (artifactNode != null) {
						String a = artifactNode.getNodeValue();
						if (a.equals(artifactId)) {
							Node groupNode = node.getAttributes().getNamedItem(
									"groupId");
							if (groupNode != null) {
								String g = groupNode.getNodeValue();
								if (g.equals(groupId)) {
									Node versionNode = node.getAttributes()
											.getNamedItem("version");
									if (versionNode != null) {
										result = versionNode.getNodeValue();
										break;
									}
								}
							}
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Find and create if neccessary the user's application directory, according
	 * to operating system standards. The resolved directory is then stored in
	 * the system property <code>taverna.home</code>
	 * <p>
	 * If the system property <code>taverna.home</code> already exists, the
	 * directory specified by that path will be used instead and created if
	 * needed.
	 * <p>
	 * If any exception occurs (such as out of diskspace), taverna.home will be
	 * unset.
	 * 
	 * <p>
	 * On Windows, this will typically be something like:
	 * 
	 * <pre>
	 *     	C:\Document and settings\MyUsername\Application Data\MyApplication
	 * </pre>
	 * 
	 * while on Mac OS X it will be something like:
	 * 
	 * <pre>
	 *     	/Users/MyUsername/Library/Application Support/MyApplication
	 * </pre>
	 * 
	 * All other OS'es are assumed to be UNIX-alike, returning something like:
	 * 
	 * <pre>
	 *     	/user/myusername/.myapplication
	 * </pre>
	 * 
	 * <p>
	 * If the directory does not already exist, it will be created.
	 * </p>
	 * 
	 * @return System property <code>taverna.home</code> contains path of an
	 *         existing directory for Taverna user-centric files.
	 */
	public static void findUserDir() {
		File appHome;
		String application = "Taverna";
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
				appHome = new File(libDir, application);
			} else if (os.startsWith("Windows")) {
				String APPDATA = System.getenv("APPDATA");
				File appData = null;
				if (APPDATA != null) {
					appData = new File(APPDATA);
				}
				if (appData != null && appData.isDirectory()) {
					appHome = new File(appData, application);
				} else {
					// logger.warn("Could not find %APPDATA%: " + APPDATA);
					appHome = new File(home, application);
				}
			} else {
				// We'll assume UNIX style is OK
				appHome = new File(home, "." + application.toLowerCase());
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
