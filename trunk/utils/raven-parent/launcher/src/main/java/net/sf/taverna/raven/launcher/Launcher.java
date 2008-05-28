package net.sf.taverna.raven.launcher;

import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.prelauncher.PreLauncher;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.spi.SpiRegistry;

/**
 * Launcher called by the {@link PreLauncher} after making sure Raven etc. is on
 * the classpath.
 * <p>
 * The Launcher will find the Raven {@link LocalRepository} through
 * {@link ApplicationRuntime#getRavenRepository()}. It then initialises the
 * {@link PluginManager} so that it can use the {@link SpiRegistry} of
 * {@link Launchable}s to find the instance of the class named by
 * {@link ApplicationConfig#APP_MAIN} in the
 * {@link ApplicationConfig#PROPERTIES raven-launcher.properties}. The
 * {@link Launchable#launch(String[])} method is then executed.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class Launcher {

	/**
	 * Call the "real" application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		int status = launcher.launchMain(args);
		if (status != 0) {
			System.exit(status);
		}
	}

	private final ApplicationConfig appConfig;
	private final ApplicationRuntime appRuntime;

	public Launcher() {
		appConfig = ApplicationConfig.getInstance();
		appRuntime = ApplicationRuntime.getInstance();
	}

	/**
	 * Find the instance of the given class name by looking it up in the
	 * {@link SpiRegistry} of {@link Launchable}s.
	 * <p>
	 * The {@link PluginManager} is also initialised.
	 * 
	 * @param className
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Launchable findMainClass(String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Repository localRepository = appRuntime.getRavenRepository();
		PluginManager.setRepository(localRepository);

		// A getInstance() should be enough to initialise
		// the plugins
		@SuppressWarnings("unused")
		PluginManager pluginMan = PluginManager.getInstance();

		SpiRegistry launchableSpi = new SpiRegistry(localRepository,
				Launchable.class.getCanonicalName(), appRuntime
						.getClassLoader());
		for (Class<?> launchableClass : launchableSpi) {
			System.out.println(launchableClass);
			if (launchableClass.getCanonicalName().equals(className)) {
				Launchable launchable = (Launchable) launchableClass
						.newInstance();
				return launchable;
			}
		}
		throw new ClassNotFoundException("Could not find " + className);
	}

	/**
	 * Launch the main {@link Launchable} method as resolved from
	 * {@link #findMainClass(String)}.
	 * 
	 * @param args
	 *            Arguments to pass to {@link Launchable#launch(String[])}
	 * @return The status code of launching, 0 means success.
	 */
	public int launchMain(String[] args) {
		Launchable launchable;
		System.out.println(appConfig.getName());
		String mainClass = appConfig.getMainClass();
		try {
			launchable = findMainClass(mainClass);
		} catch (ClassNotFoundException e) {
			System.err.println("Could not find class: " + mainClass);
			e.printStackTrace();
			return -1;
		} catch (IllegalAccessException e) {
			System.err
					.println("Could not access main() in class: " + mainClass);
			e.printStackTrace();
			return -2;
		} catch (InstantiationException e) {
			System.err.println("Could not instantiate class: " + mainClass);
			e.printStackTrace();
			return -3;
		}

		try {
			return launchable.launch(args);
		} catch (Exception e) {
			System.err.println("Error while executing main() of " + mainClass);
			e.printStackTrace();
			return -4;
		}
	}

}
