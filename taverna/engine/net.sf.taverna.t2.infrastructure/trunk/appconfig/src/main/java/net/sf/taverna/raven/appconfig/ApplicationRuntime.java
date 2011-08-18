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
package net.sf.taverna.raven.appconfig;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

public class ApplicationRuntime {

	private static final String PLUGINS = "plugins";

	private static final String LAUNCHER_SPLASHSCREEN_PNG = "/launcher_splashscreen.png";

	private static final String REPOSITORY = "repository";

	private static Logger logger = Logger.getLogger(ApplicationRuntime.class);

	private ApplicationConfig appConfig = ApplicationConfig.getInstance();

	private File localRepositoryDir;

	private File applicationHomeDir;

	private ApplicationUserHome appUserHome;

	private static class ApplicationRuntimeHolder {
		private static ApplicationRuntime instance = new ApplicationRuntime();
	}

	/**
	 * Protected constructor, use {@link #getInstance()}.
	 */
	protected ApplicationRuntime() {
		String name = appConfig.getName();
		if (name.equals(ApplicationConfig.UNKNOWN_APPLICATION)) {
			name = null;
		}
		appUserHome = new ApplicationUserHome(name, appConfig
				.getApplicationHome());
	}

	/**
	 * Get the application runtime settings.
	 *
	 * @return
	 */
	public static ApplicationRuntime getInstance() {
		return ApplicationRuntimeHolder.instance;
	}

	/**
	 * Get the local repository directory.
	 *
	 * @return
	 */
	public synchronized File getLocalRepositoryDir() {
		if (localRepositoryDir != null) {
			return localRepositoryDir;
		}

		// Let's see if the application config says where it should be
		String appRepository = appConfig.getLocalRavenRepository();
		if (appRepository != null && !appRepository.equals("")) {
			localRepositoryDir = new File(appRepository);
		} else {
			// We'll make it under the application home directory then
			File appHome = getApplicationHomeDir();
			localRepositoryDir = new File(appHome, REPOSITORY);
		}

		localRepositoryDir.mkdirs();
		if (!localRepositoryDir.isDirectory()) {
			throw new IllegalStateException("Could not make local repository "
					+ localRepositoryDir);
		}
		return localRepositoryDir;

	}

	public synchronized File getApplicationHomeDir() {
		if (applicationHomeDir != null) {
			return applicationHomeDir;
		}
		File homeDir = appUserHome.getAppUserHome();
		if (homeDir == null) {
			try {
				// Make a temporary home directory as a backup
				homeDir = File.createTempFile(appConfig.getName(), "home");
				homeDir.delete();
				homeDir.mkdirs();
			} catch (IOException e) {
				throw new IllegalStateException(
						"Can't create temporary application home", e);
			}
			logger.warn("Could not determine application's user home,"
					+ " using temporary dir " + homeDir);

		}
		if (!homeDir.isDirectory()) {
			throw new IllegalStateException(
					"Could not create application home directory " + homeDir);
		}
		applicationHomeDir = homeDir;
		return applicationHomeDir;
	}

	/**
	 * Set (and if necessary create) the local repository directory to be
	 * returned by {@link #getLocalRepositoryDir()}.
	 *
	 * @param localRepositoryDir
	 *            Directory that is to be the the new local repository
	 * @throws IOException
	 *             If the localRepositoryDir could not be created or accessed as
	 *             a directory.
	 */
	public synchronized void setLocalRepositoryDir(File localRepositoryDir)
			throws IOException {
		localRepositoryDir.mkdirs();
		if (localRepositoryDir.isDirectory()) {
			this.localRepositoryDir = localRepositoryDir;
		} else {
			throw new IOException("Invalid directory " + localRepositoryDir);
		}
	}

	public ClassLoader getClassLoader() {
		ClassLoader ourClassLoader = getClass().getClassLoader();
		if (ourClassLoader == null) {
			ourClassLoader = ClassLoader.getSystemClassLoader();
		}
		return ourClassLoader;
	}

	public synchronized void setApplicationHomeDir(File applicationHomeDir) {
		this.applicationHomeDir = applicationHomeDir;
	}

	public File getPluginsDir() {
		File pluginsDir = new File(getApplicationHomeDir(), PLUGINS);
		pluginsDir.mkdirs();
		if (!pluginsDir.isDirectory()) {
			throw new IllegalStateException(
					"Could not create plugins directory " + pluginsDir);
		}
		return pluginsDir;
	}

	public URL getDefaultPluginsDir() {
		URL startupDir;
		try {
			startupDir = appConfig.getStartupRoot();
		} catch (IOException e) {
			logger.warn("Could not find startup directory", e);
			return null;
		}
		URL pluginsURI;
		try {
			pluginsURI = new URL(startupDir, PLUGINS+"/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return pluginsURI;
	}

	public URL getDefaultRepositoryDir() {
		URL startupDir;
		try {
			startupDir = appConfig.getStartupRoot();
		} catch (IOException e) {
			logger.warn("Could not find startup directory", e);
			return null;
		}
		URL repositoryURI;
		try {
			repositoryURI = new URL(startupDir, REPOSITORY+"/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return repositoryURI;

	}

	public URL getSplashScreenURL() {
		if (!appConfig.isShowingSplashscreen()) {
			return null;
		}
		return getClass().getResource(LAUNCHER_SPLASHSCREEN_PNG);
	}

}
