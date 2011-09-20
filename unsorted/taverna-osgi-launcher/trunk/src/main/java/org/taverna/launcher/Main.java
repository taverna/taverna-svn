package org.taverna.launcher;

import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.exit;
import static java.util.Arrays.asList;
import static java.util.ServiceLoader.load;
import static java.util.UUID.randomUUID;
import static java.util.regex.Pattern.compile;
import static org.osgi.framework.Bundle.START_ACTIVATION_POLICY;
import static org.osgi.framework.Constants.FRAGMENT_HOST;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;
import org.taverna.launcher.environment.ApplicationConfiguration;
import org.taverna.launcher.environment.CommandLineArgumentProvider;
import org.taverna.launcher.environment.impl.CommandLineImpl;
import org.taverna.launcher.environment.impl.ConfigProvider;

/**
 * Implementation of the application boot processor and command line argument
 * handler.
 * 
 * @author Donal Fellows
 */
public class Main {
	static final boolean DEBUG = getBoolean("taverna.launcher.debug");

	BundleContext context;
	boolean started;
	private Framework framework;
	private CommandLineArgumentProvider clargs;
	private ApplicationConfiguration config;

	private Main(String[] argv) {
		CommandLineImpl clImpl = new CommandLineImpl(this);
		clImpl.setArguments(argv);
		clImpl.setHelpTemplate("java " + Main.class + " ?args...?");
		clargs = clImpl;
		started = clargs.consumeArgumentOnce("-launcherWait", 0, null) != null;
		config = new ConfigProvider(this);
	}

	public void markStarted() {
		started = true;
	}

	/**
	 * Try to shut OSGi down cleanly. Will not throw an exception (but might
	 * print error messages to {@link System#err}).
	 */
	void shutdownFramework() {
		try {
			if (framework != null) {
				framework.stop();
				framework.waitForStop(0);
			}
		} catch (Exception ex) {
			System.err.println("error stopping framework: " + ex);
		}
	}

	protected Map<String, Object> getConfigForFramework(
			BundlePackageActivator[] toAddToSystemBundle) {
		Map<String, Object> config = new HashMap<String, Object>();
		config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP,
				asList(toAddToSystemBundle));
		return config;
	}

	/**
	 * Runs the OSGi framework, installing the given activators as part of the
	 * system bundle.
	 * 
	 * @param activators
	 *            The activators to merge into the system bundle.
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	private void initOSGi(BundlePackageActivator... activators)
			throws Exception {
		boolean manualStop = true;
		try {
			Map<Integer, List<String>> bundles = getBundlesFromPackaging();
			Map<String, Object> config = getConfigForFramework(activators);
			config.put(FRAMEWORK_SYSTEMPACKAGES_EXTRA,
					getExtraPackages(activators));
			config.put(FRAMEWORK_STORAGE,
					new File(userHome, "osgi-cache").toString());
			config.put(FRAMEWORK_STORAGE_CLEAN,
					FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
			framework = getFactory().newFramework(config);
			if (!getBoolean("taverna.launcher.disableShutdownHook")) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						shutdownFramework();
					}
				};
				getRuntime().addShutdownHook(
						new Thread(r, "OSGi Shutdown Hook"));
				manualStop = false;
			}
			framework.init();
			List<Integer> levels = new ArrayList<Integer>(bundles.keySet());
			List<Bundle> toStart = new ArrayList<Bundle>();
			StartLevel sl = getService(StartLevel.class);
			Collections.sort(levels);
			int maxLevel = 0;
			for (int level : levels) {
				if (level > maxLevel)
					maxLevel = level;
				for (String bundleName : bundles.get(level)) {
					Bundle bundle = framework.getBundleContext().installBundle(
							bundleName);
					if (bundle != null
							&& bundle.getHeaders().get(FRAGMENT_HOST) == null) {
						sl.setBundleStartLevel(bundle, level);
						toStart.add(bundle);
					}
				}
			}
			if (DEBUG)
				System.err.println("DEBUG: setting start level to " + maxLevel);
			//sl.setStartLevel(maxLevel);
			framework.start();
			sl.setStartLevel(maxLevel);
			for (Bundle bundle : toStart)
				bundle.start(START_ACTIVATION_POLICY);
			if (DEBUG)
				System.err.println("DEBUG: services: "
						+ asList(framework.getRegisteredServices()));
		} catch (Exception e) {
			System.err.println("could not create framework: " + e);
			if (manualStop)
				shutdownFramework();
			throw e;
		}
		framework.start();
		if (!started) {
			clargs.printHelp();
			if (manualStop)
				shutdownFramework();
			exit(1);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getService(Class<T> api) {
		BundleContext ctxt = framework.getBundleContext();
		return (T) ctxt.getService(ctxt.getServiceReference(api.getName()));
	}

	private static final String DEFAULT_PACKAGE_VERSION = "0.0.0.0_default";

	/**
	 * Guess the list of OSGi packages to merge into the system bundle.
	 * 
	 * @param activators
	 *            The activators that we are going to install.
	 * @return OSGi magic string.
	 */
	private static String getExtraPackages(BundlePackageActivator[] activators) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		Set<Package> done = new HashSet<Package>();
		for (BundlePackageActivator bpa : activators) {
			Package p = bpa.getAPIPackage();
			if (done.contains(p))
				continue;
			sb.append(sep).append(p.getName()).append(";version=");
			if (p.getImplementationVersion() == null)
				sb.append(DEFAULT_PACKAGE_VERSION);
			else
				sb.append(p.getImplementationVersion());
			sep = ",";
			done.add(p);
		}
		return sb.toString();
	}

	public String applicationName;
	public File userHome;
	private static final String APPJAR_SUFFIX = "-app.jar";
	private static final String BUNDLEJAR_SUFFIX = "-bundles.jar";
	private static final String APPJAR_PATTERN = "^file:.*-app\\.jar$";

	/**
	 * Gets the mapping from start levels to lists of URLs of bundle JARs
	 * associated with that level. The list of bundles is discovered by
	 * discovering the associated super-package and inspecting what bundles are
	 * stored within it.
	 * 
	 * @return Bundle level to URL list map.
	 * @throws Exception
	 *             If anything goes wrong
	 */
	private Map<Integer, List<String>> getBundlesFromPackaging()
			throws Exception {
		try {
			for (URL url : getClassPathLocations())
				if (url.toString().matches(APPJAR_PATTERN))
					try {
						String appJar = url.toURI().getSchemeSpecificPart();
						Map<Integer, List<String>> bundles = getUserOverrides(appJar);
						merge(bundles, getBundleOverrides(appJar));
						merge(bundles, getBundlesFromPackaging(appJar.replace(
								APPJAR_SUFFIX, BUNDLEJAR_SUFFIX)));
						return bundles;
					} catch (FileNotFoundException fnfe) {
						// No such file...
						continue;
					}
		} catch (Exception e) {
			throw new Exception(
					"could not compute the bundle repository JAR name", e);
		}
		throw new Exception("could not compute the bundle repository JAR name");
	}

	private void merge(Map<Integer, List<String>> toUpdate,
			Map<Integer, List<String>> toAdd) {
		if (toUpdate.isEmpty()) {
			toUpdate.putAll(toAdd);
			return;
		} else if (toAdd.isEmpty()) {
			return;
		}
		for (Map.Entry<Integer, List<String>> entry : toAdd.entrySet()) {
			List<String> level = toUpdate.get(entry.getKey());
			if (level == null) {
				level = new ArrayList<String>();
				toUpdate.put(entry.getKey(), level);
			}
			level.addAll(entry.getValue());
		}
	}

	private Map<Integer, List<String>> getUserOverrides(String appJar) {
		String appname = new File(appJar).getName().replace(APPJAR_SUFFIX, "");
		applicationName = appname;
		String home = System.getProperty("user.home");
		if (home == null) {
			while (true) {
				File homeDir = new File(System.getProperty("java.io.tmpdir"),
						randomUUID().toString());
				if (!homeDir.exists() && homeDir.mkdir()) {
					home = homeDir.toString();
					System.err
							.println("WARNING: no user home directory; using "
									+ home);
					break;
				}
			}
		}
		userHome = new File(new File(home), "." + applicationName);
		if (!userHome.isDirectory())
			userHome.mkdir();
		return getBundlesFromDir(userHome);
	}

	private Map<Integer, List<String>> getBundleOverrides(String appJar) {
		return getBundlesFromDir(new File(new File(appJar).getParent(),
				"updates"));
	}

	private Map<Integer, List<String>> getBundlesFromDir(File dir) {
		Map<Integer, List<String>> toStart = new HashMap<Integer, List<String>>();
		dir = new File(dir, "bundles");
		if (DEBUG)
			System.err.println("DEBUG: checking " + dir + " for bundles...");
		if (!dir.isDirectory())
			return toStart;
		File[] dirs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (!f.isDirectory())
					return false;
				try {
					return parseInt(f.getName()) > 0;
				} catch (NumberFormatException nfe) {
					return false;
				}
			}
		});
		if (dirs == null)
			return toStart;
		for (File d : dirs) {
			List<String> toAdd = new ArrayList<String>();
			for (File jar : d.listFiles())
				if (jar.getName().endsWith(".jar"))
					toAdd.add(jar.toURI().toString());
			if (!toAdd.isEmpty())
				toStart.put(Integer.valueOf(d.getName()), toAdd);
		}
		return toStart;
	}

	/**
	 * Gets the array of locations that we load resources from.
	 * 
	 * @return Array of URLs; should <i>not</i> be retained or modified.
	 */
	private URL[] getClassPathLocations() {
		// Nasty hack; not OSGi-friendly! OK _only_ because we know we are
		// in the system class loader at this point.
		URLClassLoader classloader = (URLClassLoader) getClass()
				.getClassLoader();
		return classloader.getURLs();
	}

	/**
	 * Gets the mapping from start levels to lists of URLs of bundle JARs
	 * associated with that level.
	 * 
	 * @param bj
	 *            The name of the bundle super-package.
	 * @return Bundle level to URL list map.
	 * @throws IOException
	 *             If the bundle super-package can't be opened and read.
	 */
	private Map<Integer, List<String>> getBundlesFromPackaging(String bj)
			throws IOException {
		if (DEBUG)
			System.out.println("DEBUG: bundles being loaded from " + bj);
		Map<Integer, List<String>> toStart = new HashMap<Integer, List<String>>();
		if (bj == null)
			return toStart;

		File jarFile = new File(bj);
		Enumeration<JarEntry> e = new JarFile(jarFile).entries();
		Pattern p = compile("^resources/bundles/([0-9]+)/.*\\.jar$");
		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();

			Matcher m = p.matcher(je.getName());
			if (!m.matches())
				continue;
			Integer levelKey = new Integer(m.group(1));

			List<String> current = toStart.get(levelKey);
			if (current == null) {
				current = new ArrayList<String>();
				toStart.put(levelKey, current);
			}
			current.add("jar:" + jarFile.toURI() + "!/" + je.getName());
		}
		if (DEBUG)
			System.out.println("DEBUG: discovered bundles " + toStart);
		return toStart;
	}

	/**
	 * Helper for getting the OSGi Framework factory.
	 * 
	 * @return The factory.
	 * @throws ServiceConfigurationError
	 *             If things have gone wrong.
	 */
	protected FrameworkFactory getFactory() {
		Iterator<FrameworkFactory> it = load(FrameworkFactory.class).iterator();
		assert it.hasNext();
		return it.next();
	}

	/**
	 * Application entry point.
	 * 
	 * @param argv
	 *            The arguments to the application.
	 */
	public static void main(String[] argv) {
		try {
			Main main = new Main(argv);
			main.initOSGi(new APIActivator<CommandLineArgumentProvider>(
					CommandLineArgumentProvider.class, main.clargs),
					new APIActivator<ApplicationConfiguration>(
							ApplicationConfiguration.class, main.config));
		} catch (Exception ex) {
			ex.printStackTrace();
			exit(2);
		}
	}
}
