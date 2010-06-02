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
package net.sf.taverna.raven.launcher;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.raven.SplashScreen;
import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.appconfig.config.Log4JConfiguration;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.prelauncher.BootstrapClassLoader;
import net.sf.taverna.raven.prelauncher.PreLauncher;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.spi.SpiRegistry;

import org.apache.log4j.Logger;

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
	private SplashScreen splash;

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
		prepareClassLoaders();
		prepareLogging();
		prepareProxyConfiguration();
		prepareSplashScreen();
		String mainClass = appConfig.getMainClass();
		Launchable launchable;
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
		removeSplashScreenListener();
		try {
			return launchable.launch(args);
		} catch (Exception e) {
			System.err.println("Error while executing main() of " + mainClass);
			e.printStackTrace();
			return -4;
		}
	}

	protected void prepareProxyConfiguration() {
		LauncherHttpProxyConfiguration.getInstance();
		Authenticator.setDefault(new ProxyAuthenticator());
	}

	protected void prepareLogging() {
		Log4JConfiguration.getInstance().prepareLog4J();		
	}

	protected void prepareClassLoaders() {
		PreLauncher preLauncher = PreLauncher.getInstance();
		BootstrapClassLoader launchingClassLoader = preLauncher
				.getLaunchingClassLoader();
		if (launchingClassLoader == null) {
			// Set to a child of the real launching class loader (of us - not
			// PreLauncher) that is an BootstrapClassLoader instance - this
			// is only neccessary if we were not launched through PreLauncher
			launchingClassLoader = new BootstrapClassLoader(appRuntime
					.getClassLoader());
			preLauncher.setLaunchingClassLoader(launchingClassLoader);
		}
		if (Thread.currentThread().getContextClassLoader() == preLauncher
				.getClass().getClassLoader()) {
			// Set context class loader to the launching class loader so that
			// system artifacts can later be injected with
			// preLauncher.addURLToClassPath(url) and picked up from
			// 3rd party libraries
			Thread.currentThread().setContextClassLoader(launchingClassLoader);
		}
		
		// Add conf/ folder to classpath
		try {
			URL url = new URL(appConfig.getStartupRoot(), "conf/");
			launchingClassLoader.addURL(url);
		} catch (Exception e) {
			System.err.println("Could not add conf/ to classpath");
		}
	}

	protected void removeSplashScreenListener() {
		if (splash != null) {
			splash.removeListener();
			splash.setText("Starting application...");
		}
	}

	protected void prepareSplashScreen() {
		URL splashScreenURL = appRuntime.getSplashScreenURL();
		if (splashScreenURL != null && !GraphicsEnvironment.isHeadless()) {
			splash = SplashScreen.getSplashScreen(splashScreenURL);
			splash.listenToRepository(appRuntime.getRavenRepository());
		} else {
			System.err.println("No splash screen : " + splashScreenURL);
		}
	}

}
