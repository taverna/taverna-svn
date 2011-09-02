package org.taverna.launcher;

import static java.util.Arrays.asList;
import static java.util.ServiceLoader.load;
import static org.apache.felix.framework.util.FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
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
	static final boolean DEBUG = true;

	class Activator implements BundleActivator {
		Activator(CommandLineArgumentProvider clap) {
			this.clap = clap;
		}

		private CommandLineArgumentProvider clap;
		private ServiceRegistration registration;

		@Override
		public void start(BundleContext context) {
			Main.this.context = context;
			registration = context.registerService(
					CommandLineArgumentProvider.class.getName(), clap,
					new Properties());
			if (DEBUG)
				System.out.println("DEBUG: Started main bundle");
		}

		@Override
		public void stop(BundleContext context) {
			if (DEBUG)
				System.out.println("DEBUG: Stopping main bundle");
			registration.unregister();
			Main.this.context = null;
		}
	}

	BundleContext context;
	boolean started;
	private Framework framework;
	private Activator activator;
	private CommandLineArgumentProvider clap;

	private Main(String[] argv) {
		clap = new CLA(this, asList(argv), "java " + Main.class + " ?args...?");
		activator = new Activator(clap);
		started = clap.consumeArgumentOnce("-launcherWait", 0, null) != null;
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

	private static final String CLAP_API = "org.taverna.launcher.environment"
			+ ";version=0.1";

	private void runFelix(BundleActivator... activators) throws Exception {
		Map<String, Object> config = new HashMap<String, Object>();
		boolean manualStop = true;
		try {
			config.put(SYSTEMBUNDLE_ACTIVATORS_PROP, asList(activators));
			config.put(FRAMEWORK_SYSTEMPACKAGES_EXTRA, CLAP_API);
			Map<Integer, List<String>> bundles = getBundlesFromPackaging();
			framework = getFactory().newFramework(config);
			if (!Boolean.getBoolean("taverna.launcher.disableShutdownHook")) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						shutdownFramework();
					}
				};
				Runtime.getRuntime().addShutdownHook(
						new Thread(r, "Felix Shutdown Hook"));
				manualStop = false;
			}
			framework.init();
			AutoProcessor.process(null, framework.getBundleContext());
			List<Integer> levels = new ArrayList<Integer>(bundles.keySet());
			Collections.sort(levels);
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
			clap.printHelp();
			if (manualStop)
				shutdownFramework();
			System.exit(1);
		}
	}

	private Map<Integer, List<String>> getBundlesFromPackaging()
			throws Exception {
		URLClassLoader classloader = (URLClassLoader) Main.class
				.getClassLoader();
		for (URL url : classloader.getURLs())
			if (url.toString().matches("^file:.*-app.jar$"))
				return getBundlesFromPackaging(url.toString().replace(
						"-app.jar", "-bundles.jar"));
		throw new Exception("could not compute the bundle repository JAR name");
	}

	private Map<Integer, List<String>> getBundlesFromPackaging(String bj)
			throws IOException {
		if (DEBUG)
			System.out.println("DEBUG: bundles being loaded from " + bj);
		Map<Integer, List<String>> toStart = new HashMap<Integer, List<String>>();
		if (bj == null)
			return toStart;
		File jarFile = new File(bj.replaceFirst("^file:", ""));
		Enumeration<JarEntry> e = new JarFile(jarFile).entries();
		Pattern p = Pattern.compile("^resources/bundles/([0-9]+)/.*\\.jar$");
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
			main.runFelix(main.activator);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(2);
		}
	}
}
