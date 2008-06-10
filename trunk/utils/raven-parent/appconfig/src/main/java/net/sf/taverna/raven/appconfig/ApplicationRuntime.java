package net.sf.taverna.raven.appconfig;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.DummyRepository;
import net.sf.taverna.raven.repository.impl.LocalRepository;

import org.apache.log4j.Logger;

public class ApplicationRuntime {
	private static final String REPOSITORY = "repository";

	private static Logger logger = Logger.getLogger(ApplicationRuntime.class);

	private static ApplicationRuntime instance;

	private ApplicationConfig appConfig = ApplicationConfig.getInstance();

	private File localRepositoryDir;

	private File applicationHomeDir;

	private ApplicationUserHome appUserHome;

	private Repository ravenRepository;

	/**
	 * Protected constructor, use {@link #getInstance()}.
	 */
	protected ApplicationRuntime() {
		appUserHome = new ApplicationUserHome(appConfig.getName(), appConfig
				.getApplicationHome());
	}

	/**
	 * Get the application runtime settings.
	 * 
	 * @return
	 */
	public static synchronized ApplicationRuntime getInstance() {
		if (instance == null) {
			instance = new ApplicationRuntime();
		}
		return instance;
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

	public synchronized Repository getRavenRepository() {
		if (ravenRepository != null) {
			return ravenRepository;
		}
		ravenRepository = makeRavenRepository();
		ravenRepository.update();
		return ravenRepository;
	}

	private Repository makeRavenRepository() {
		Repository repository;
		if (!appConfig.isUsingRaven()) {
			// FIXME: Avoid raven.eclipse hack
			System.setProperty("raven.eclipse", "true");
			repository = new DummyRepository();
			return repository;
		}
		repository = LocalRepository
				.getRepository(getLocalRepositoryDir(), getClassLoader(),
						getSystemArtifacts());
		return repository;
	}

	public Set<Artifact> getSystemArtifacts() {
		Set<Artifact> artifacts = new HashSet<Artifact>();
		artifacts.add(new BasicArtifact("uk.org.mygrid.taverna.raven", "raven",
				"1.7-SNAPSHOT"));
		artifacts.add(new BasicArtifact("uk.org.mygrid.taverna.raven",
				"raven-log4j", "1.7-SNAPSHOT"));
		artifacts.add(new BasicArtifact("uk.org.mygrid.taverna.raven",
				"prelauncher", "1.7-SNAPSHOT"));
		artifacts.add(new BasicArtifact("uk.org.mygrid.taverna.raven",
				"launcher", "1.7-SNAPSHOT"));
		artifacts.add(new BasicArtifact("uk.org.mygrid.taverna.raven",
				"launcher-api", "1.7-SNAPSHOT"));
		artifacts.add(new BasicArtifact("uk.org.mygrid.taverna.raven",
				"plugins-api", "1.7-SNAPSHOT"));
		artifacts.add(new BasicArtifact("uk.org.mygrid.taverna.raven",
				"appconfig", "1.7-SNAPSHOT"));
		
		artifacts.add(new BasicArtifact("log4j", "log4j", "1.2.12"));
		return artifacts;
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
		File pluginsDir = new File(getApplicationHomeDir(), "plugins");
		pluginsDir.mkdirs();
		if (!pluginsDir.isDirectory()) {
			throw new IllegalStateException(
					"Could not create plugins directory " + pluginsDir);
		}
		return pluginsDir;
	}

	public File getDefaultPluginsDir() {
		File startupDir;
		try {
			startupDir = appConfig.getStartupDir();
		} catch (IOException e) {
			logger.warn("Could not find startup directory", e);
			return null;
		}
		File pluginsDir = new File(startupDir, "plugins");
		if (!pluginsDir.isDirectory()) {
			logger.warn("Could not find plugins directory " + pluginsDir);
			return null;
		}
		return pluginsDir;
	}

}
