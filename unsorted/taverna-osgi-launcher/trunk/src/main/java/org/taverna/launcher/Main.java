package org.taverna.launcher;

import static java.lang.Boolean.getBoolean;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.exit;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.ServiceLoader.load;
import static java.util.regex.Pattern.compile;
import static org.apache.felix.framework.util.FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP;
import static org.apache.felix.main.AutoProcessor.process;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.taverna.launcher.environment.CommandLineArgumentProvider;
import org.taverna.launcher.environment.impl.CLA;

/**
 * Implementation of the application boot processor and command line argument
 * handler.
 * 
 * @author Donal Fellows
 */
public class Main {
	static final boolean DEBUG = false;

	BundleContext context;
	boolean started;
	private Framework framework;
	private CommandLineArgumentProvider clargs;

	private Main(String[] argv) {
		clargs = new CLA(this, asList(argv), "java " + Main.class
				+ " ?args...?");
		started = clargs.consumeArgumentOnce("-launcherWait", 0, null) != null;
	}

	public void markStarted() {
		started = true;
	}

	/**
	 * Try to shut Felix down cleanly. Will not throw an exception (but might
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

	/**
	 * Runs the OSGi framework, installing the given activators as part of the
	 * system bundle.
	 * 
	 * @param activators
	 *            The activators to merge into the system bundle.
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	private void runFelix(BundlePackageActivator... activators)
			throws Exception {
		Map<String, Object> config = new HashMap<String, Object>();
		boolean manualStop = true;
		try {
			config.put(SYSTEMBUNDLE_ACTIVATORS_PROP, asList(activators));
			config.put(FRAMEWORK_SYSTEMPACKAGES_EXTRA,
					getExtraPackages(activators));
			Map<Integer, List<String>> bundles = getBundlesFromPackaging();
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
			process(null, framework.getBundleContext());
			List<Integer> levels = new ArrayList<Integer>(bundles.keySet());
			sort(levels);
			for (int level : levels)
				for (String bundleName : bundles.get(level)) {
					Bundle bundle = framework.getBundleContext().installBundle(
							bundleName);
					bundle.start();
				}
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
			URLClassLoader classloader = (URLClassLoader) getClass()
					.getClassLoader();
			for (URL url : classloader.getURLs())
				if (url.toString().matches(APPJAR_PATTERN))
					try {
						return getBundlesFromPackaging(url.toString().replace(
								APPJAR_SUFFIX, BUNDLEJAR_SUFFIX));
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

		File jarFile = new File(bj.replaceFirst("^file:", ""));
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
	FrameworkFactory getFactory() {
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
			main.runFelix(new APIActivator<CommandLineArgumentProvider>(
					CommandLineArgumentProvider.class, main.clargs));
		} catch (Exception ex) {
			ex.printStackTrace();
			exit(2);
		}
	}
}
