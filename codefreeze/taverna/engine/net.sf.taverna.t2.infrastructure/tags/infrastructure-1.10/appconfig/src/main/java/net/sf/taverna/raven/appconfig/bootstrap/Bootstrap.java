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
package net.sf.taverna.raven.appconfig.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.prelauncher.ClassLocation;
import net.sf.taverna.raven.prelauncher.PreLauncher;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Bootstrap {

	private static final PreLauncher preLauncher = PreLauncher.getInstance();

	private static Logger logger = Logger.getLogger(Bootstrap.class);

	public static final String VERSION = "1.7.1";

	public static final String APPLICATION = "Taverna-" + VERSION;

	public static Properties properties;

	// Where Raven will store its repository, discovered by main()
	public static String TAVERNA_CACHE = "";

	public static URL[] remoteRepositories;

	private static String loaderVersion;

	private static final String SPLASHSCREEN = "splashscreen-1.7.png";

	@Deprecated
	public static void main(String[] args) throws MalformedURLException,
			ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		findUserDir();
		if (System.getProperty("taverna.startup") == null) {
			determineStartup();
		}
		new ProxyConfiguration().initialiseProxySettings();
		properties = findProperties();

		if (properties == null) {
			System.out
					.println("Unable to find raven.properties. This should either be within a conf folder in your startup directory, or within the conf folder of your $taverna.home.");
			System.exit(-1);
		} else {
			System.getProperties().putAll(properties);
		}

		remoteRepositories = new Repositories().find();

		List<URL> localLoaderUrls = new ArrayList<URL>();
		List<URL> remoteLoaderUrls = new ArrayList<URL>();
		getLoaderUrls(localLoaderUrls, remoteLoaderUrls);

		Method loaderMethod = createLoaderMethod(localLoaderUrls,
				remoteLoaderUrls);

		Class workbenchClass = createWorkbenchClass(loaderVersion, loaderMethod);

		addSystemLoaderArtifacts();

		invokeWorkbench(args, workbenchClass);
	}

	public static void addSystemLoaderArtifacts() throws MalformedURLException {
		URL cacheURL = findCache().toURI().toURL();

		try {
			String localProfile = RavenProperties.getInstance()
					.getRavenProfileLocation();
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
					if (systemNode != null
							&& "true".equals(systemNode.getNodeValue())) {
						Node groupNode = attributes.getNamedItem("groupId");
						Node artifactNode = attributes
								.getNamedItem("artifactId");
						Node versionNode = attributes.getNamedItem("version");
						if (groupNode != null && artifactNode != null
								&& versionNode != null) {
							URL artifactURL = new URL(cacheURL, artifactURI(
									groupNode.getNodeValue(), artifactNode
											.getNodeValue(), versionNode
											.getNodeValue()));
							preLauncher.addURLToClassPath(artifactURL);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not add system artifacts", e);

		}
	}

	public static void addSystemArtifact(String groupID, String artifactID,
			String versionID) throws MalformedURLException {
		URL cacheURL = findCache().toURI().toURL();
		URL artifactURL = new URL(cacheURL, artifactURI(groupID, artifactID,
				versionID));
		preLauncher.addURLToClassPath(artifactURL);
	}

	/**
	 * Returns a copy of the raven.properties, which are overridden by any
	 * System.properties.
	 * 
	 * @return the properties
	 */
	public static Properties findProperties() {
		if (System.getProperty("taverna.startup") == null) {
			determineStartup();
		}
		Properties result = null;
		try {
			result = RavenProperties.getInstance().getProperties();
		} catch (Exception e) {
			System.err
					.println("Unable to find raven.properties, either remotely, or locally.");

			// continue using System properties.
			// user may have decided to define all the properties as system
			// properties
			result = new Properties();
			result.putAll(System.getProperties());
		}
		return result;
	}

	/**
	 * Determines the location that Taverna was started from, used for finding
	 * the default configurations and plugin files which determine the default
	 * behaviour for Taverna.
	 * 
	 * If determined succesfully the system property $taverna.startup is set to
	 * this value.
	 * 
	 */
	private static void determineStartup() {
		try {
			File startupDir = ClassLocation
					.getClassLocationDir(Bootstrap.class);
			if (startupDir != null) {
				String startup = startupDir.getAbsolutePath();
				System.setProperty("taverna.startup", startup);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void invokeWorkbench(String[] args, Class workbenchClass)
			throws IllegalAccessException, NoSuchMethodException {
		String methodName = properties.getProperty("raven.target.method");
		try {
			try {
				// Try m(String[] args) first
				Method workbenchStatic = workbenchClass.getMethod(methodName,
						String[].class);
				workbenchStatic.invoke(null, new Object[] { args });
			} catch (NoSuchMethodException ex) {
				// Try m() instead
				Method workbenchStatic = workbenchClass.getMethod(methodName);
				workbenchStatic.invoke(null);
			}
		} catch (NoSuchMethodException ex) {
			System.err.println("Could not find method " + methodName);
			System.exit(6);
		} catch (InvocationTargetException e) {
			System.err.println("Exception occured in " + methodName);
			e.getCause().printStackTrace();
			System.exit(5);
		}
	}

	public static Class createWorkbenchClass(String ravenVersion,
			Method loaderMethod) throws MalformedURLException,
			IllegalAccessException {
		Class workbenchClass = null;
		File cacheDir = findCache();
		String useSplashProp = properties.getProperty("raven.splashscreen");
		boolean useSplashscreen = useSplashProp != null
				&& !useSplashProp.equalsIgnoreCase("false");

		String groupID = properties.getProperty("raven.target.groupid");
		String artifactID = properties.getProperty("raven.target.artifactid");
		String version = properties.getProperty("raven.target.version");
		String targetClassName = properties.getProperty("raven.target.class");

		if (properties.getProperty("raven.profile") != null) {
			String targetVersion = getProfileArtifactVersion(groupID,
					artifactID);
			if (targetVersion != null) {
				version = targetVersion;
			}
		}

		// System.out.println("Using version " + version + " of " + groupID +
		// ":"
		// + artifactID);

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
			String msg = e.getCause().getMessage() != null ? e.getCause()
					.getMessage() : "you should check you have network access.";
			System.err.println("Could not launch Raven: " + msg);
			System.exit(4);
		}
		return workbenchClass;
	}

	public static URL getSplashScreenURL() throws MalformedURLException {
		return Bootstrap.class.getResource("/" + SPLASHSCREEN);
	}

	public static void getLoaderUrls(List<URL> localUrls, List<URL> remoteUrls)
			throws MalformedURLException {
		File cacheDir = findCache();

		if (cacheDir == null) {
			System.err.println("Unable to create repository directory");
			System.exit(-1);
			return;
		}

		// Create a remote classloader referencing the raven jar within a
		// repository
		String loaderGroupId = properties.getProperty("raven.loader.groupid");
		String loaderArtifactId = properties
				.getProperty("raven.loader.artifactid");
		loaderVersion = properties.getProperty("raven.loader.version");
		if (properties.getProperty("raven.profile") != null) {
			String version = getProfileArtifactVersion(loaderGroupId,
					loaderArtifactId);
			if (version != null) {
				loaderVersion = version;
			}
		}

		// System.out.println("Using version " + loaderVersion + " of "
		// + loaderGroupId + ":" + loaderArtifactId);
		String artifactLocation = artifactURI(loaderGroupId, loaderArtifactId,
				loaderVersion);

		// Use our local repository if possible
		URL cacheUrl = new URL(cacheDir.toURI().toURL(), artifactLocation);
		if (cacheUrl.getProtocol().equals("file")) {
			localUrls.add(cacheUrl);
		} else {
			remoteUrls.add(cacheUrl);
		}

		for (URL repository : remoteRepositories) {
			URL loaderUrl = null;
			if (loaderVersion.endsWith("-SNAPSHOT")) {
				loaderUrl = getSnapshotUrl(loaderArtifactId, loaderGroupId,
						loaderVersion, repository);
				if (loaderUrl != null) {
					if (loaderUrl.getProtocol().equals("file")) {
						localUrls.add(loaderUrl);
					} else {
						remoteUrls.add(loaderUrl);
					}
					break; // for snapshots leave loop once we've found the
					// first.
				}
			} else {
				loaderUrl = new URL(repository, artifactLocation);
				if (loaderUrl.getProtocol().equals("file")) {
					localUrls.add(loaderUrl);
				} else {
					remoteUrls.add(loaderUrl);
				}
			}
		}
	}

	private static URL getSnapshotUrl(String artifact, String group,
			String version, URL repository) {
		URL result = null;
		String path = group.replaceAll("\\.", "/") + "/" + artifact + "/"
				+ version;

		// try concrete path first
		String loc = artifactURI(group, artifact, version);

		try {
			// test if the URL exists, with a short timeout
			result = new URL(repository, loc);
			URLConnection con = result.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.getInputStream();
		} catch (IOException e) {
			result = null;
			// try metadata
			try {
				URL metadata = new URL(repository, path + "/maven-metadata.xml");
				URLConnection con = metadata.openConnection();
				con.setConnectTimeout(5000);
				Document doc = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(con.getInputStream());
				// generate file =
				// <artifact>-<version>-<timestamp>-<buildnumber>.jar
				NodeList timestamps = doc.getElementsByTagName("timestamp");
				NodeList buildnumbers = doc.getElementsByTagName("buildNumber");

				if (timestamps.getLength() > 0) {
					String timestamp = "";
					String buildnumber;

					if (timestamps.getLength() > 1) {
						System.out
								.println("More than 1 timestamp for snapshot");
					}
					Node el = timestamps.item(0);
					timestamp = el.getTextContent();
					if (buildnumbers.getLength() > 0) {
						if (buildnumbers.getLength() > 1) {
							System.out
									.println("More than 1 buildnumber for snapshot");
						}
						el = buildnumbers.item(0);
						buildnumber = el.getTextContent();

						String file = artifact + "-"
								+ version.replace("-SNAPSHOT", "") + "-"
								+ timestamp + "-" + buildnumber + ".jar";
						URL snapshot = new URL(repository, path + "/" + file);

						// test that snapshot file exists.
						con = snapshot.openConnection();
						con.setConnectTimeout(5000);
						con.setReadTimeout(5000);
						con.getInputStream();
						result = snapshot;
					}
				}
			} catch (IOException ex) {
				// no metadata at this repository either so give up
			} catch (SAXException sex) {
				System.out
						.println("SAX Exception parsing maven-metadata.xml for location "
								+ path);
				sex.printStackTrace();
			} catch (ParserConfigurationException pcex) {
				System.out
						.println("ParserConfigurationException parsing maven-metadata.xml for location "
								+ path);
				pcex.printStackTrace();
			}
		}

		return result;
	}

	public static Method createLoaderMethod(List<URL> localUrls,
			List<URL> remoteUrls) throws ClassNotFoundException,
			NoSuchMethodException {
		Method result = null;

		// first try with just the local urls, since raven should be local by
		// now
		try {
			ClassLoader c = new URLClassLoader(localUrls.toArray(new URL[0]),
					null);
			result = createLoaderMethodWithClassloader(c);
		} catch (Exception e) {
			// now try with the remote too, this is probably fhe first run and
			// raven needs fetching
			List<URL> allUrls = new ArrayList<URL>();
			allUrls.addAll(localUrls);
			allUrls.addAll(remoteUrls);
			ClassLoader c = new URLClassLoader(allUrls.toArray(new URL[0]),
					null);
			result = createLoaderMethodWithClassloader(c);
		}

		return result;
	}

	private static Method createLoaderMethodWithClassloader(ClassLoader c)
			throws ClassNotFoundException, NoSuchMethodException {
		Method loaderMethod;
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
		return ApplicationRuntime.getInstance().getLocalRepositoryDir();
	}

	private static String getProfileArtifactVersion(String groupId,
			String artifactId) {
		String result = null;
		try {
			String localProfile = properties.getProperty("raven.profile");
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
	 *      	C:\Document and settings\MyUsername\Application Data\MyApplication
	 * </pre>
	 * 
	 * while on Mac OS X it will be something like:
	 * 
	 * <pre>
	 *      	/Users/MyUsername/Library/Application Support/MyApplication
	 * </pre>
	 * 
	 * All other OS'es are assumed to be UNIX-alike, returning something like:
	 * 
	 * <pre>
	 *      	/user/myusername/.myapplication
	 * </pre>
	 * 
	 * <p>
	 * If the directory does not already exist, it will be created. It will also
	 * create the 'conf' directory within it if it doesn't exist.
	 * </p>
	 * 
	 * @return System property <code>taverna.home</code> contains path of an
	 *         existing directory for Taverna user-centric files.
	 */
	public static void findUserDir() {
		File appHome;
		String application = APPLICATION;
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

		// create conf folder
		File conf = new File(appHome, "conf");
		if (!conf.exists()) {
			conf.mkdir();
		}

		System.setProperty("taverna.home", appHome.getAbsolutePath());
		return;
	}

	public static String getLoaderVersion() {
		return loaderVersion;
	}
}
