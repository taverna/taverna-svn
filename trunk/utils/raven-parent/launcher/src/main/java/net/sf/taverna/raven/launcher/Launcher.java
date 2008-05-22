package net.sf.taverna.raven.launcher;

import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.launcher.Launchable;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.prelauncher.PreLauncher;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.SpiRegistry;

/**
 * Launcher called by the {@link PreLauncher} after making sure Raven etc. is on
 * the classpath.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class Launcher {

	/**
	 * Calls the "real" application
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

	public Launchable findMainClass(String className) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
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
			System.err.println("Could not access main() in class: "
					+ mainClass);
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
