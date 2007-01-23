/**
 * 
 */
package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.DownloadStatus;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;
import net.sf.taverna.raven.spi.ProfileFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Represents the state of a local Maven2 repository on disk. Manages the queue
 * of pending fetches. Create an instance of this class using the static
 * getRepository(File base) method passing it the location of the on disk maven2
 * repository to access or create.
 * 
 * @author Tom Oinn
 */
public class LocalRepository implements Repository {

	private static Log logger = Log.getLogger(LocalRepository.class);

	private static FileFilter isDirectory = new AcceptDirectoryFilter();

	/**
	 * Create a new Repository object with the specified base.
	 * 
	 * @param base
	 *            The root of the local repository, must be a directory
	 *            containing a valid Maven2 compliant repository structure
	 */
	protected LocalRepository(File base) {
		try {
			this.base = base.getCanonicalFile();
		} catch (IOException e) {
			logger.error("Could not make canonical file " + base, e);
			this.base = base;
		}
		// Fake in our own classloader
		synchronized (loaderMap) {
			loaderMap.put(new BasicArtifact("uk.org.mygrid.taverna.raven",
				"raven", "1.5.1-SNAPSHOT"), new LocalArtifactClassLoader(this,
				this.getClass().getClassLoader()));
		}
		initialize();
	}

	static Map<File, Repository> repositoryCache =
		new HashMap<File, Repository>();

	/**
	 * Get a new or cached instance of LocalRepository for the supplied base
	 * directory, this is the method to use when you want to get hold of a
	 * Repository.
	 * 
	 * @param base
	 *            The base directory for the m2 repository on disk
	 * @return Repository instance for the base directory
	 */
	public static synchronized Repository getRepository(File base) {
		if (!repositoryCache.containsKey(base)) {
			if (System.getProperty("raven.eclipse") == null) {
				repositoryCache.put(base, new LocalRepository(base));
			} else {
				repositoryCache.put(base, new EclipseRepository());
			}
		}
		return repositoryCache.get(base);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.raven.repository.impl.Repository#addArtifact(net.sf.taverna.raven.repository.impl.ArtifactImpl)
	 */
	public synchronized void addArtifact(Artifact a1) {
		ArtifactImpl a = new ArtifactImpl(a1, this);
		if (!status.containsKey(a)) {
			status.put(a, ArtifactStatus.Unknown);
			setStatus(a, ArtifactStatus.Queued);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.raven.repository.impl.Repository#addRemoteRepository(java.net.URL)
	 */
	public void addRemoteRepository(URL repositoryURL) {
		repositories.add(repositoryURL);
		if (repositoryURL.getProtocol().equals("file")) {
			fileRepositories.add(repositoryURL);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.raven.repository.impl.Repository#getLoader(net.sf.taverna.raven.repository.impl.ArtifactImpl,
	 *      java.lang.ClassLoader)
	 */
	public ClassLoader getLoader(Artifact a1, ClassLoader parent)
		throws ArtifactNotFoundException, ArtifactStateException {
		ArtifactImpl a = new ArtifactImpl(a1, this);
		if (!status.containsKey(a)) {
			// No such artifact
			throw new ArtifactNotFoundException();
		}
		if (!status.get(a).equals(ArtifactStatus.Ready)) {
			// Can't get a classloader yet, the artifact isn't ready
			logger.debug(a + " :: " + status.get(a));
			throw new ArtifactStateException(status.get(a),
				new ArtifactStatus[] { ArtifactStatus.Ready });
		}
		synchronized (loaderMap) {
			if (loaderMap.containsKey(a)) {
				return loaderMap.get(a);
			}
		}
		try {
			// Even if parent is null
			return new LocalArtifactClassLoader(this, a, parent);
		} catch (MalformedURLException e) {
			logger.error("Malformed URL for artifact " + a, e);
			return null;
		}

	}

	/**
	 * Given a Class object return the Artifact whose LocalArtifactClassLoader
	 * created it. If the classloader was not an instance of
	 * LocalArtifactClassLoader then return null
	 */
	public Artifact artifactForClass(Class c) throws ArtifactNotFoundException {
		synchronized (loaderMap) {
			for (Entry<Artifact, LocalArtifactClassLoader> entry : loaderMap.entrySet()) {
				if (entry.getValue() == c.getClassLoader()) {
					return entry.getKey();
				}
			}
		}
		throw new ArtifactNotFoundException("No artifact for Class : "
			+ c.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.raven.repository.impl.Repository#update()
	 */
	public synchronized void update() {
		while (act()) {
			// nothing
		}
	}

	/**
	 * Return all Artifacts within this repository
	 */
	public synchronized List<Artifact> getArtifacts() {
		return new ArrayList<Artifact>(status.keySet());
	}

	/**
	 * Return all artifacts with the specified ArtifactStatus
	 */
	public synchronized List<Artifact> getArtifacts(ArtifactStatus s) {
		List<Artifact> result = new ArrayList<Artifact>();
		for (Artifact a : status.keySet()) {
			if (status.get(a).equals(s)) {
				result.add(a);
			}
		}
		return result;
	}

	/**
	 * Add a new repository listener to be notified of status changes within
	 * this Repository implementation
	 */
	public void addRepositoryListener(RepositoryListener l) {
		synchronized (listeners) {
			if (!listeners.contains(l)) {
				listeners.add(l);
			}
		}
	}

	public void removeRepositoryListener(RepositoryListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}

	private synchronized void setStatus(ArtifactImpl a, ArtifactStatus newStatus) {
		if (status.containsKey(a) && status.get(a) != newStatus) {
			synchronized (listeners) {
				for (RepositoryListener l : listeners) {
					ArtifactStatus old = status.get(a);
					status.put(a, newStatus);
					l.statusChanged(a, old, newStatus);
				}
			}
			status.put(a, newStatus);
		}
	}

	/**
	 * Status for a given Artifact
	 * 
	 * @param a
	 *            Artifact to get status for
	 * @return ArtifactStatus representing the state of the artifact within this
	 *         repository, or ArtifactStatus.Unknown if there is no such
	 *         artifact
	 */
	public synchronized ArtifactStatus getStatus(Artifact a) {
		if (!status.containsKey(a)) {
			return ArtifactStatus.Unknown;
		}
		return status.get(a);
	}

	static final Map<Artifact, LocalArtifactClassLoader> loaderMap =
		new HashMap<Artifact, LocalArtifactClassLoader>();

	private final List<RepositoryListener> listeners =
		new ArrayList<RepositoryListener>();

	/**
	 * Scan the status table, perform actions on each item based on the status.
	 * If an item is Queued then start downloading the POM If the item has a
	 * downloaded POM then parse it, add any unknown dependencies to the status
	 * with Queued state and start downloading the jar file If the item has a
	 * jar file then traverse dependencies and determine whether all transitive
	 * dependencies are satisfied in which case mark as Ready state
	 */
	private boolean act() {
		boolean moreToDo = false;
		Set<ArtifactImpl> temp = new HashSet<ArtifactImpl>(status.keySet());
		for (ArtifactImpl a : temp) {
			ArtifactStatus s = status.get(a);
			if (s.equals(ArtifactStatus.Pom)) {
				// logger.debug(a.toString());
				if (!"jar".equals(a.getPackageType())) {
					setStatus(a, ArtifactStatus.PomNonJar);
				} else {
					try {
						List<ArtifactImpl> deps = a.getDependencies();
						synchronized (this) {
							for (Artifact dep : deps) {
								addArtifact(dep);
							}
							setStatus(a, ArtifactStatus.Analyzed);
							moreToDo = true;
						}
					} catch (ArtifactStateException ase) {
						// Never happens, state must be Pom to enter this block
						logger.error("Artifact state for " + a
							+ "should have been Pom", ase);
					}
				}
			} else if (s.equals(ArtifactStatus.Queued)) {
				try {
					fetch(a, "pom");
					moreToDo = true;
				} catch (ArtifactNotFoundException e) {
					logger.warn("Could not find artifact " + a, e);
				}
			} else if (s.equals(ArtifactStatus.Analyzed)) {
				try {
					fetch(a, "jar");
					moreToDo = true;
				} catch (ArtifactNotFoundException e) {
					logger.warn("Could not find artifact " + a, e);
				}
			} else if (s.equals(ArtifactStatus.Jar)) {
				boolean fullyResolved = true;
				boolean resolutionError = false;
				try {
					List<ArtifactImpl> deps = a.getDependencies();
					Set<Artifact> seenArtifacts = new HashSet<Artifact>();
					seenArtifacts.add(a);
					for (ArtifactImpl dep : deps) {
						if (!status.containsKey(dep)) {
							addArtifact(dep);
						}
						// if (! status.get(dep).equals(ArtifactStatus.Ready)) {
						// fullyResolved = false;
						// }
						if (!fullyResolved(dep, seenArtifacts)) {
							fullyResolved = false;
						}
						if (status.get(dep).isError()) {
							resolutionError = true;
						}
					}
				} catch (ArtifactStateException ase) {
					logger.error("Artifact state for " + a
						+ "should have been Jar", ase);
				}
				if (fullyResolved) {
					setStatus(a, ArtifactStatus.Ready);
					moreToDo = true;
				}
				if (resolutionError) {
					setStatus(a, ArtifactStatus.DependencyFailure);
				}
			}
		}
		return moreToDo;
	}

	/**
	 * Returns true if the artifact is fully resolved. A fully resolved jar has:
	 * <ul>
	 * <li>a status of 'Ready', or</li>
	 * <li>a status of 'Jar' and all its dependencies are 'Ready' or have
	 * already been seen.</li>
	 * </ul>
	 * 
	 * @param artifact
	 * @param seenArtifacts
	 * @return true if the artifact is fully resolved
	 */
	private boolean fullyResolved(ArtifactImpl artifact,
		Set<Artifact> seenArtifacts) {
		if (status.get(artifact).equals(ArtifactStatus.Ready)) {
			return true;
		}
		if (status.get(artifact).equals(ArtifactStatus.Jar)) {
			if (seenArtifacts.contains(artifact)) {
				return true;
			}
			seenArtifacts.add(artifact);
			try {
				List<ArtifactImpl> deps = artifact.getDependencies();
				for (ArtifactImpl dep : deps) {
					if (!fullyResolved(dep, seenArtifacts)) {
						return false;
					}
				}
				return true;
			} catch (ArtifactStateException e) {
				logger.error("Artifact state for " + artifact
					+ " should have been Jar", e);
			}
		}
		return false;
	}

	/**
	 * Base directory for the local repository
	 */
	File base;

	/**
	 * URL list of remote repository base URLs. Modified by
	 * {@link #addRemoteRepository(URL)}
	 */
	private LinkedHashSet<URL> repositories = new LinkedHashSet<URL>();

	/**
	 * Subset of repositories that are hosted locally, ie. which URLs start with
	 * 
	 * <pre>
	 * file:
	 * </pre>. Modified by {@link #addRemoteRepository(URL)}.
	 */
	private LinkedHashSet<URL> fileRepositories = new LinkedHashSet<URL>();

	/**
	 * Map of artifact to artifact status
	 */
	private Map<ArtifactImpl, ArtifactStatus> status =
		new HashMap<ArtifactImpl, ArtifactStatus>();

	/**
	 * Map of Artifact to a two element array of total size in current download
	 * and bytes downloaded. If the artifact has no pending downloads it will
	 * not appear as a key in this map.
	 */
	private Map<ArtifactImpl, DownloadStatusImpl> dlstatus =
		new HashMap<ArtifactImpl, DownloadStatusImpl>();

	/**
	 * Cache of pom files for given artifacts, supports pomFile(). File entries
	 * can be both within the base repository, or from a local file://
	 * repository
	 */
	private Map<Artifact, File> pomFiles = new HashMap<Artifact, File>();

	/**
	 * Cache of jar files for given artifacts, supports pomFile(). File entries
	 * can be both within the base repository, or from a local file://
	 * repository
	 */
	private Map<Artifact, File> jarFiles = new HashMap<Artifact, File>();

	/**
	 * Local base directory for the specified artifact in the {@link #base}
	 * repository. The directory will be created if createDirs is true and it
	 * doesn't exist.
	 * 
	 * @param a
	 *            {@link Artifact} which directory is to be located.
	 * @param createDirs
	 *            Create directories if missing
	 * @return {@link File} of the directory where {@link Artifact}
	 */
	private File artifactDir(Artifact a, boolean createDirs) {
		String[] groupParts = a.getGroupId().split("\\.");
		File groupDir = base;
		for (String part : groupParts) {
			groupDir = new File(groupDir, part);
		}
		File artifactDir = new File(groupDir, a.getArtifactId());
		File versionDir = new File(artifactDir, a.getVersion());
		if (createDirs) {
			versionDir.mkdirs();
		}
		return versionDir;
	}


	/**
	 * Generate abstract {@link File} representation of where the artifact file
	 * would be stored in the local base directory as calculated by
	 * {@link #artifactDir(Artifact)}
	 * 
	 * @param a
	 *            {@link Artifact} which file is to be located
	 * @param extension
	 *            Extension of file, such as ".pom" or ".jar"
	 * @param createDirs
	 *            Create directories if missing
	 * @return Abstract {@link File} for artifact file
	 */
	private File artifactFile(Artifact a, String extension, boolean createDirs) {
		return new File(artifactDir(a, createDirs), 
			a.getArtifactId() + "-"	+ a.getVersion() + extension);
	}
	
	/**
	 * File object for POM for the specified artifact. Note that this file might 
	 * be outside {@link #artifactDir(Artifact)} as it could have been found in
	 * a local repository by {@link #fetchLocal(ArtifactImpl, String)}.
	 */
	File pomFile(Artifact a) {
		return file(a, ".pom", pomFiles);
	}

	/**
	 * File object for JAR for the specified artifact. Note that this file might 
	 * be outside {@link #artifactDir(Artifact)} as it could have been found in
	 * a local repository by {@link #fetchLocal(ArtifactImpl, String)}.
	 */
	public File jarFile(Artifact a) {
		return file(a, ".jar", jarFiles);
	}
	
	/**
	 * Utillity function for {@link #pomFile(Artifact)}) and
	 * {@link #jarFile(Artifact)}. Return {@link File} object from cache, or
	 * construct a new using {@link #artifactFile(Artifact, String)}
	 * 
	 * @param a {@link Artifact} which file is referenced
	 * @param extension of file, ".pom" or ".jar"
	 * @param cache to check/put file in, {@link #pomFiles} or {@link #jarFiles}
	 * @return
	 */
	private File file(Artifact a, String extension, Map<Artifact, File> cache) {
		File file = cache.get(a);
		if (file == null) {
			file = artifactFile(a, extension, true);
			cache.put(a, file);
		}
		return file;
	}
	

	/**
	 * Copy the input stream to the output stream. Why doesn't java include this
	 * as a utility method somewhere?
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	void copyStream(InputStream is, OutputStream os, Artifact a)
		throws IOException {
		int totalbytes = 0;
		byte[] buffer = new byte[1024];
		int bytesRead;
		try {
			while ((bytesRead = is.read(buffer)) != -1) {
				totalbytes += bytesRead;
				os.write(buffer, 0, bytesRead);
				dlstatus.get(a).setReadBytes(totalbytes);
			}
		} finally {
			dlstatus.get(a).setFinished();
			os.flush();
			os.close();
		}
	}

	synchronized void forcePom(ArtifactImpl a) throws ArtifactNotFoundException {
		fetch(a, "pom");
	}

	/**
	 * Fetch a remote POM or Jar and store it in the local repository
	 * 
	 * @param a
	 *            The artifact to resolve the POM for
	 * @param suffix
	 *            The suffix of the file to fetch, either 'pom' or 'jar'
	 * @throws ArtifactNotFoundException
	 */
	private void fetch(ArtifactImpl a, String suffix)
		throws ArtifactNotFoundException {
		if (artifactFile(a, "."+suffix, false).isFile()) {
			// Already exists, no need to refetch
			//dlstatus.remove(a);
			setStatus(a,
				"pom".equals(suffix) ? ArtifactStatus.Pom
					: ArtifactStatus.Jar);
			
			return;
		}
		if (fetchLocal(a, suffix)) {
			logger.debug("Found " + a + "." + suffix + " locally cached");
			// No need to copy a file already in file://something repository
			//dlstatus.remove(a);
			setStatus(a,
				"pom".equals(suffix) ? ArtifactStatus.Pom
					: ArtifactStatus.Jar);
			return;
		}
		String repositoryPath = repositoryPath(a, suffix);
		for (URL repository : repositories) {
			try {
				URL pomLocation = new URL(repository, repositoryPath);
				InputStream is = null;
				try {
					URLConnection connection = pomLocation.openConnection();
					connection.setConnectTimeout(10000);
					connection.connect();
					int length = connection.getContentLength();
					dlstatus.put(a, new DownloadStatusImpl(length));
					is = connection.getInputStream();
				} catch (FileNotFoundException e) {
					if (a.getVersion().endsWith("-SNAPSHOT")) {
						is = getSnapshotArtifactStream(a, suffix, repository);
					} else {
						logger.info(a + " not found in " + repository);
					}
				} catch (SocketTimeoutException e) {
					logger.warn("Connection timed out while looking for" + a
						+ " in " + repository);
				}
				if (is != null) {
					// Where to write it?
					File toFile = artifactFile(a, "."+suffix, true);
					// Sanity check, we shouldn't have climbed outside base
					File parent = toFile.getParentFile().getCanonicalFile();
					if (!parent.toString().startsWith(base.toString())) {
						logger.error("Could not write outside repository "
							+ toFile);
						break; // pomFailed
					}
					// Opened the stream so presumably the thing exists
					// Create the appropriate directory structure within the
					// local repository
					if (toFile.exists()) {
						logger.warn("Already existed, overwriting " + toFile);
						toFile.delete();
					}
					toFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(toFile);
					setStatus(a,
						"pom".equals(suffix) ? ArtifactStatus.PomFetching
							: ArtifactStatus.JarFetching);
					copyStream(is, fos, a);
					dlstatus.remove(a);
					setStatus(a, "pom".equals(suffix) ? ArtifactStatus.Pom
						: ArtifactStatus.Jar);
					return;
				}

			} catch (MalformedURLException e) {
				logger.error("Malformed repository URL: " + repository, e);
			} catch (FileNotFoundException e) {
				logger.info(a + " not found in " + repository);
			} catch (IOException e) {
				logger.warn("Could not read " + a, e);
				// Ignore the exception, probably means we couldn't find the POM
				// in the repository. If there are more repositories in the list
				// this isn't neccessarily an issue.
			}
		}
		// No appropriate POM found in any of the repositories so throw an
		// exception
		setStatus(a, "pom".equals(suffix) ? ArtifactStatus.PomFailed
			: ArtifactStatus.JarFailed);
		dlstatus.remove(a);
		throw new ArtifactNotFoundException("Can't find artifact for: " + a);
	}

	private String repositoryPath(ArtifactImpl a, String suffix) {
		String fname = a.getArtifactId() + "-" + a.getVersion() + "." + suffix;
		String repositoryDir =
			a.getGroupId().replaceAll("\\.", "/") + "/" + a.getArtifactId()
				+ "/" + a.getVersion();
		String repositoryPath = repositoryDir + "/" + fname;
		return repositoryPath;
	}

	/**
	 * Try to fetch artifact file from one of the local <code>file:/</code>
	 * repositories. If the artifact exists in one of the local repositories, it
	 * is <strong>not</strong> copied to our <code>base</code> repository,
	 * and the repository location will be put into the map of {@link #pomFiles}
	 * or {@link #jarFiles} corresponding to the suffix, as retrievable by
	 * {@link #pomFile(Artifact)} or {@link #jarFile(Artifact)}.ß
	 * 
	 * @param artifact
	 *            {@link Artifact} which file to download
	 * @param suffix
	 *            Suffix of file, either "pom" or "jar"
	 * @return true if the artifact file was cached from a local repository
	 */
	private boolean fetchLocal(ArtifactImpl artifact, String suffix) {
		String repositoryPath = repositoryPath(artifact, suffix);
		// Let's see if any of our file:// repositories have it
		for (URL repository : fileRepositories) {
			File file = new File(repository.getPath(), repositoryPath);
			if (!file.canRead()) {
				continue; // Not found in this repository
			}
			// Just to be sure, let's try to really read it
			try {
				InputStream stream = file.toURI().toURL().openStream();
				stream.read();
				stream.close();
			} catch (IOException e) {
				logger.warn("Could not read " + file, e);
				continue; // try next repository
			}
			if (suffix.equals("pom")) {
				pomFiles.put(artifact, file);
			} else if (suffix.equals("jar")) {
				jarFiles.put(artifact, file);
				// FIXME: Avoid always making this from scratch	
			} else { // unexpected suffix
				logger.error("Unknown suffix " + suffix + " for " + artifact);
				return false;
			}
			// Note how we also copy the .pom, just so that the .jar we
			// have to copy anyway is accompanied with the .pom
			Set<Artifact> systemArtifacts = ProfileFactory.getInstance().getProfile().getSystemArtifacts();
			if (systemArtifacts.contains(artifact)) {
				// Need to copy it so that the system classloader can
				// find it (We can't interact with it here as Raven shouldn't
				// depend on Taverna's BootstrapClassLoader)
				// TODO: something like SystemClassLoaderSPI.addURL(file)
				return false; 
			}
			return true; // OK to use cache
		}
		return false; // Didn't find it
	}

	/**
	 * checks for a maven-metadata.xml, and if present opens it to find the
	 * current build and timestamp. From this information it then generates a
	 * stream to the file, or return null if no such file exists.
	 * 
	 * @param a
	 * @param suffix
	 * @param repository
	 * @return
	 */
	private InputStream getSnapshotArtifactStream(ArtifactImpl a,
		String suffix, URL repository) {
		String repositoryDir =
			a.getGroupId().replaceAll("\\.", "/") + "/" + a.getArtifactId()
				+ "/" + a.getVersion();

		InputStream result = null;
		try {
			URL metadata =
				new URL(repository, repositoryDir + "/" + "maven-metadata.xml");
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			try {
				Document doc =
					fac.newDocumentBuilder().parse(metadata.openStream());
				String filename =
					getSnapshotFilenameFromMetaData(a, doc, suffix);

				// test for the file, with a short timeout
				URLConnection con =
					new URL(repository, repositoryDir + "/" + filename).openConnection();
				con.setConnectTimeout(10000);
				result = con.getInputStream();

				logger.info("Returning snapshot stream to "
					+ new URL(repository, repositoryDir + "/" + filename));

			} catch (SAXException e) {
				logger.error("Error parsing maven-metadata.xml for artifact "
					+ a + " at " + metadata);
			} catch (IOException e) {
				// metadata not found, so not a snapshot
			} catch (ParserConfigurationException e) {
				logger.error("Error parsing maven-metadata.xml for artifact "
					+ a + " at " + metadata);
			}

		} catch (MalformedURLException e) {
			logger.error("Malformed URL", e);
		}

		return result;
	}

	/**
	 * Queries the xml, and generates the filename which is artifact-<version>-<timestamp>-<buildnumber>.suffix
	 * 
	 * @param a
	 * @param doc
	 * @param suffix
	 * @return
	 */
	private String getSnapshotFilenameFromMetaData(Artifact a, Document doc,
		String suffix) {
		NodeList ts = doc.getElementsByTagName("timestamp");
		NodeList bn = doc.getElementsByTagName("buildNumber");
		String timestamp = "";
		String buildnumber = "";

		if (ts.getLength() > 0) {
			if (ts.getLength() != 1)
				logger.warn("metadata for snapshot for " + a
					+ " contains multiple timestamp entries");
			timestamp = ts.item(0).getTextContent();
		} else {
			logger.warn("metadata for snapshot for " + a
				+ " doesn't describe a timestamp");
		}

		if (bn.getLength() > 0) {
			if (bn.getLength() != 1)
				logger.warn("metadata for snapshot for " + a
					+ " contains multiple buildnumber entries");
			buildnumber = bn.item(0).getTextContent();
		} else {
			logger.warn("metadata for snapshot for " + a
				+ " doesn't describe a buildnumber");
		}

		String filename =
			a.getArtifactId() + "-" + a.getVersion().replace("-SNAPSHOT", "")
				+ "-" + timestamp + "-" + buildnumber + "." + suffix;
		logger.info("SNAPSHOT filename=" + filename);
		return filename;
	}

	private static class AcceptDirectoryFilter implements FileFilter {
		public boolean accept(File f) {
			return f.isDirectory();
		}
	}

	private Set<File> enumerateDirs(File current) {
		Set<File> groupDirs = new HashSet<File>();
		enumerateDirs(current, groupDirs);
		return groupDirs;
	}

	private void enumerateDirs(File current, Set<File> groupDirs) {
		try {
			current = current.getCanonicalFile();
		} catch (IOException e) {
			logger.warn("Could not make canonical path " + current, e);
			return;
		}
		// assumes base is canonical path (as by constructor)
		if (!current.getPath().startsWith(base.getPath())) {
			logger.warn(current + " is outside base root " + base);
			return;
		}
		File[] subdirs = current.listFiles(isDirectory);
		if (subdirs == null || subdirs.length == 0) {
			if (!current.equals(base)) {
				groupDirs.add(current.getParentFile().getParentFile());
			}
		} else {
			for (File subDir : subdirs) {
				enumerateDirs(subDir, groupDirs);
			}
		}
	}

	/**
	 * Clean the local repository by removing invalid artifacts and directories.
	 * <p>
	 * Empty directories will always be removed.
	 * <p>
	 * <!-- removeFailing/removeUnknown not implemented If
	 * <code>removeFailing</code> is true, all artifacts will be attempted
	 * re-downloaded, if this fails, the artifact will be removed. This option
	 * should only be used after verifying network access to the repositories.
	 * <p>
	 * If <code>removeUnknown</code> is true, files and directories not native
	 * to the Raven repositories will be removed. Be sure that the local
	 * repository directory is only used as a Raven repository before enabling
	 * this option.
	 * 
	 * @param removeFailing
	 *            Remove artifacts that can no longer be downloaded from
	 *            repositories
	 * @param removeUnknown
	 *            Remove unknown (non-Raven) files and directories -->
	 */
	public synchronized void clean() {
		if (!base.isDirectory()) {
			logger.warn("Could not clean non-directory " + base);
			return;
		}
		Set<File> groupDirs = enumerateDirs(base);
		for (File groupDir : groupDirs) {
			deleteEmptyDirs(groupDir);
		}
	}

	private void deleteEmptyDirs(File dir) {
		try {
			dir = dir.getCanonicalFile();
		} catch (IOException e) {
			logger.warn("Could not check: " + dir, e);
			// bad sign.. Let's stay away
			return;
		}
		File[] subdirs = dir.listFiles(isDirectory);
		for (File child : subdirs) {
			try {
				// Make sure we don't climb out following a symlink or something
				if (!child.getCanonicalFile().getParentFile().equals(dir)) {
					logger.warn("Skipping not a real child: " + child);
					continue;
				}
			} catch (IOException e) {
				// bad sign.. Let's stay away
				logger.warn("Could not check child: " + child, e);
				continue;
			}
			deleteEmptyDirs(child);
		}
		// OK.. we've checked our subdirs.. they might have all be
		// gone now, so let's see if we can disappear as well
		File[] content = dir.listFiles();
		if (content == null || content.length == 0) {
			// logger.debug("Deleting " + dir);
			dir.delete();
		}
	}

	/**
	 * Scan the local repository for artifacts and populate the status map
	 * accordingly
	 */
	private synchronized void initialize() {
		if (!base.exists()) {
			// No base directory so create it
			base.mkdirs();
			// Don't need to check previous content, finished
			return;
		}
		
		/*  // NO, do NOT load up all the existing crap
		// Fetch all subdirectories, assuming that each
		// subdirectory corresponds to a groupId
		Set<File> groupDirs = enumerateDirs(base);

		List<String> groupIds = new ArrayList<String>();
		for (File f : groupDirs) {
			File temp = f;
			String groupName = "";
			while (!temp.equals(base)) {
				if (!"".equals(groupName)) {
					groupName = "." + groupName;
				}
				groupName = temp.getName() + groupName;
				temp = temp.getParentFile();
			}
			groupIds.add(groupName);
		}
		// For each subdirectory collect all artifacts
		for (String groupId : groupIds) {
			File groupDirectory = base;
			for (String part : groupId.split("\\.")) {
				groupDirectory = new File(groupDirectory, part);
			}
			File[] artifacts = groupDirectory.listFiles(isDirectory);
			for (File artifactDir : artifacts) {
				String artifactId = artifactDir.getName();
				File[] versions = artifactDir.listFiles(isDirectory);
				if (versions == null) {
					logger.debug("Null version list at " + artifactDir);
					continue;
				}
				for (File versionDir : versions) {
					String version = versionDir.getName();
					ArtifactImpl artifact =
						new ArtifactImpl(groupId, artifactId, version, this);
					// If there are any directories inside here we're not in a
					// valid
					// artifact and have accidentally wandered down the wrong
					// branch
					// of the file system. Blame the idiotic maven2 directory
					// structure
					// with groups split into subdirectories!
					File[] subDirs = versionDir.listFiles(isDirectory);
					if (subDirs != null && subDirs.length > 0) {
						// Not a valid version directory!
						continue;
					}
					File[] files = versionDir.listFiles();
					if (files != null && files.length == 0) {
						// No POM or JAR - ignore directory, should be deleted
						continue;
					}
					status.put(artifact, ArtifactStatus.Queued);
					File pomFile =
						new File(versionDir, artifactId + "-" + version
							+ ".pom");
					if (pomFile.exists()) {
						status.put(artifact, ArtifactStatus.Pom);
					}
					File jarFile =
						new File(versionDir, artifactId + "-" + version
							+ ".jar");
					if (jarFile.exists()) {
						status.put(artifact, ArtifactStatus.Jar);
					}
				}
			}
		}
		*/
	}

	/**
	 * If the artifact specified is in either PomFetching or JarFetching state
	 * this returns a DownloadStatus object which provides a non updating
	 * snapshot of the file size (if known) and total bytes downloaded. The
	 * intent is to use this for progress bars within any client GUI code.
	 * 
	 * @return DownloadStatus object representing the state of the current
	 *         download for this artifact
	 * @param a
	 *            Artifact to get status for
	 * @throws ArtifactNotFoundException
	 *             if this repository doesn't contain the specified artifact.
	 * @throws ArtifactStateException
	 *             if the artifact is found but isn't involved in a download at
	 *             the present time.
	 */
	public DownloadStatus getDownloadStatus(Artifact a)
		throws ArtifactStateException, ArtifactNotFoundException {
		if (status.containsKey(a)) {
			ArtifactStatus astatus = status.get(a);
			if (dlstatus.containsKey(a)) {
				return dlstatus.get(a);
			} else {
				throw new ArtifactStateException(astatus,
					new ArtifactStatus[] { ArtifactStatus.PomFetching,
							ArtifactStatus.JarFetching });
			}
		}
		throw new ArtifactNotFoundException("Cant find artifact for: " + a);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Artifact a : status.keySet()) {
			sb.append(getStatus(a)).append("\t").append(a.toString()).append(
				"\n");
		}
		return sb.toString();
	}
}
