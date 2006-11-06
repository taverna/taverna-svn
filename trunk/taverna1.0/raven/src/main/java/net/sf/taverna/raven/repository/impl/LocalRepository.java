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
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.DownloadStatus;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;

/**
 * Represents the state of a local Maven2 repository
 * on disk. Manages the queue of pending fetches.
 * Create an instance of this class using the static
 * getRepository(File base) method passing it the location
 * of the on disk maven2 repository to access or create.
 * @author Tom Oinn
 */
public class LocalRepository implements Repository {
	
	private static Log logger = Log.getLogger(LocalRepository.class);
	
	private static FileFilter isDirectory = new AcceptDirectoryFilter();
	
	/**
	 * Implementation of ClassLoader that uses the artifact
	 * metadata to manage any dependencies of the artifact
	 */
	public class ArtifactClassLoader extends URLClassLoader {
		
		private List<ArtifactClassLoader> childLoaders = 
			new ArrayList<ArtifactClassLoader>();
		private Map<String, Class> classMap =
			new HashMap<String, Class>();
		private String name;		
		
		public Repository getRepository() {
			return LocalRepository.this;
		}
		
		protected ArtifactClassLoader(ArtifactImpl a) throws MalformedURLException, ArtifactStateException {
			super(new URL[]{jarFile(a).toURI().toURL()});
			synchronized(loaderMap) {
				loaderMap.put(a, this);
			}
			init(a);
		}
		
		protected ArtifactClassLoader(ArtifactImpl a, ClassLoader parent) throws MalformedURLException, ArtifactStateException {
			super(new URL[]{jarFile(a).toURI().toURL()}, parent);			
			synchronized(loaderMap) {
				loaderMap.put(a, this);
			}
			init(a);
		}
		
		protected ArtifactClassLoader(ClassLoader selfLoader) {
			super(new URL[0], selfLoader);
		}
		
		private void init(ArtifactImpl a) throws ArtifactStateException {			
			List<ArtifactImpl> deps = a.getDependencies();
			name = a.toString();
			for (ArtifactImpl dep : deps) {
				synchronized(loaderMap) {
					ArtifactClassLoader ac = loaderMap.get(dep);
					if (ac == null) {
						try {
							ac = new ArtifactClassLoader(dep);
						} catch (MalformedURLException e) {
							logger.error("Malformed URL when loading " + dep, e);
						}
//						loaderMap.put(a, ac);						
					}						
					childLoaders.add(ac);
				}
			}
		}
		
		@Override
		public URL findResource(String name) {
			return findFirstInstanceOfResource(new HashSet<ArtifactClassLoader>(), name);
		}
		
		@Override
		public Enumeration<URL> findResources(String name) throws IOException {
			Set<URL> resourceLocations = new HashSet<URL>();
			enumerateResources(new HashSet<ArtifactClassLoader>(), resourceLocations, name);
			return Collections.enumeration(resourceLocations);
		}
		
		
		private URL findFirstInstanceOfResource(Set<ArtifactClassLoader> alreadySeen, String name) {
			URL resourceURL = super.findResource(name);
			if (resourceURL != null) {
				return resourceURL;
			}
			alreadySeen.add(this);
			for (ArtifactClassLoader cl : childLoaders) {
				if (!alreadySeen.contains(cl)) {
				resourceURL = cl.findFirstInstanceOfResource(alreadySeen, name);
				if (resourceURL != null) {
					return resourceURL;
				}
			}
			}
			return null;
		}
		
		private void enumerateResources(Set<ArtifactClassLoader> alreadySeen, Set<URL> resourceLocations, String name) throws IOException {
			alreadySeen.add(this);
			URL resourceURL = super.findResource(name);
			if (resourceURL != null) {
				resourceLocations.add(resourceURL);
			}
			for (ArtifactClassLoader cl : childLoaders) {
				if (! alreadySeen.contains(cl)) {
					cl.enumerateResources(alreadySeen, resourceLocations, name);
				}
			}
		}
		
		
		@Override
		public String toString() {
			return "loader{"+name+"} from "+System.identityHashCode(LocalRepository.this);
		}
		
		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			return findClass(name, new HashSet<ArtifactClassLoader>());
		}
				
		protected Class<?> findClass(String name, Set<ArtifactClassLoader> seenLoaders) throws ClassNotFoundException {
			logger.debug("Searching for '"+name+"' - "+this);
			seenLoaders.add(this);
			if (classMap.containsKey(name)) {
				logger.debug("Returning cached '"+name+"' - "+this);
				return classMap.get(name);
			}
			try {
				Class c = super.findClass(name);
				classMap.put(name, c);
				logger.debug("Returning found '"+name+"' - "+this);
				return c;
			} catch (ClassNotFoundException e) {
				//logger.debug("Trying children of "+this);
				//for (ArtifactClassLoader ac : childLoaders) {
				//logger.debug("    "+ac.toString());
				//}
				for (ArtifactClassLoader ac : childLoaders) {
					if (! seenLoaders.contains(ac)) {
						try {
//							Class loadedClass = findLoadedClass(name);
//							if (loadedClass != null) {
//								return loadedClass;
//							}
							if (ac.getParent() instanceof ArtifactClassLoader) {
								((ArtifactClassLoader) ac.getParent()).findClass(name, seenLoaders);
							} else if (ac.getParent() != null) {
								try {
									return ac.getParent().loadClass(name);
								} catch (ClassNotFoundException cnfe) {
						}
							}
//							return ac.loadClass(name);
							return ac.findClass(name, seenLoaders);
						}
						catch (ClassNotFoundException cnfe) {
							logger.debug("No '"+name+"' in "+this);
						}
				}
			}
		}
			throw new ClassNotFoundException(name);
	}
	}
	
	/**
	 * Create a new Repository object with the specified
	 * base.
	 * @param base The root of the local repository, must
	 * be a directory containing a valid Maven2 compliant
	 * repository structure
	 */
	protected LocalRepository(File base) {
		try {
			this.base = base.getCanonicalFile();
		} catch (IOException e) {
			logger.error("Could not make canonical file " + base, e);
			this.base = base;
		}
		// Fake in our own classloader
		loaderMap.put(new BasicArtifact("uk.org.mygrid.taverna.raven","raven","1.5-SNAPSHOT"), new ArtifactClassLoader(this.getClass().getClassLoader()));
		initialize();
	}
	
	private static Map<File,Repository> repositoryCache = new HashMap<File,Repository>();
	/**
	 * Get a new or cached instance of LocalRepository for the supplied base directory,
	 * this is the method to use when you want to get hold of a Repository.
	 * @param base The base directory for the m2 repository on disk
	 * @return Repository instance for the base directory
	 */
	public static synchronized Repository getRepository(File base) {
		if (! repositoryCache.containsKey(base)) {
			if (System.getProperty("raven.eclipse") == null) {
				repositoryCache.put(base, new LocalRepository(base));
			} else {
				repositoryCache.put(base, new EclipseRepository());
			}
		}
		return repositoryCache.get(base);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.raven.repository.impl.Repository#addArtifact(net.sf.taverna.raven.repository.impl.ArtifactImpl)
	 */
	public synchronized void addArtifact(Artifact a1) {
		ArtifactImpl a = new ArtifactImpl(a1, this);
		if (! status.containsKey(a)) {
			artifactDir(a);
			status.put(a, ArtifactStatus.Unknown);
			setStatus(a, ArtifactStatus.Queued);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.raven.repository.impl.Repository#addRemoteRepository(java.net.URL)
	 */
	public void addRemoteRepository(URL repositoryURL) {
		repositories.add(repositoryURL);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.raven.repository.impl.Repository#getLoader(net.sf.taverna.raven.repository.impl.ArtifactImpl, java.lang.ClassLoader)
	 */
	public ClassLoader getLoader(Artifact a1, ClassLoader parent) 
	throws ArtifactNotFoundException, ArtifactStateException {
		ArtifactImpl a = new ArtifactImpl(a1, this);
		if (! status.containsKey(a)) {
			// No such artifact
			throw new ArtifactNotFoundException();
		}
		if (! status.get(a).equals(ArtifactStatus.Ready)) {
			// Can't get a classloader yet, the artifact isn't ready
			logger.debug(a+" :: "+status.get(a));
			throw new ArtifactStateException(status.get(a), new ArtifactStatus[]{ArtifactStatus.Ready});
		}
		if (loaderMap.containsKey(a)) {
			return loaderMap.get(a);
		}
		try {
			// Even if parent is null
			return new ArtifactClassLoader(a, parent);
		} catch (MalformedURLException e) {
			logger.error("Malformed URL for artifact " + a, e);
		}
		return null;

	}
	
	/**
	 * Given a Class object return the Artifact whose ArtifactClassLoader created it. If the classloader was
	 * not an instance of ArtifactClassLoader then return null
	 */
	public Artifact artifactForClass(Class c) throws ArtifactNotFoundException {
		for (Entry<Artifact, ArtifactClassLoader> entry : loaderMap.entrySet()) {
			if (entry.getValue() == c.getClassLoader()) { 
				return entry.getKey();
			}
		}
		throw new ArtifactNotFoundException("No artifact for Class : "+c.getName());
	}
	
	/* (non-Javadoc)
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
	 * Add a new repository listener to be notified of status changes
	 * within this Repository implementation
	 */
	public void addRepositoryListener(RepositoryListener l) {
		synchronized(listeners) {
			if (! listeners.contains(l)) {
				listeners.add(l);
			}
		}
	}
	
	public void removeRepositoryListener(RepositoryListener l) {
		synchronized(listeners) {
			listeners.remove(l);
		}
	}
	
	private synchronized void setStatus(ArtifactImpl a, ArtifactStatus newStatus) {
		if (status.containsKey(a) && status.get(a) != newStatus) {
			synchronized(listeners) {
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
	 * @param a Artifact to get status for
	 * @return ArtifactStatus representing the state of the artifact within
	 * this repository, or ArtifactStatus.Unknown if there is no such artifact
	 */
	public synchronized ArtifactStatus getStatus(Artifact a) {
		if (! status.containsKey(a)) {
			return ArtifactStatus.Unknown;
		}
		return status.get(a);
	}
	
	static final Map<Artifact, ArtifactClassLoader> loaderMap =
		new HashMap<Artifact, ArtifactClassLoader>();
	
	private final List<RepositoryListener> listeners =
		new ArrayList<RepositoryListener>();
	
	/**
	 * Scan the status table, perform actions on each item based
	 * on the status.
	 * If an item is Queued then start downloading the POM
	 * If the item has a downloaded POM then parse it, add any unknown
	 * dependencies to the status with Queued state and start downloading
	 * the jar file
	 * If the item has a jar file then traverse dependencies and determine
	 * whether all transitive dependencies are satisfied in which case mark
	 * as Ready state
	 */
	private boolean act() {
		boolean moreToDo = false;
		Set<ArtifactImpl> temp = new HashSet<ArtifactImpl>(status.keySet());
		for (ArtifactImpl a : temp) {
			ArtifactStatus s = status.get(a);
			if (s.equals(ArtifactStatus.Pom)) {
				//logger.debug(a.toString());
				if (! "jar".equals(a.getPackageType())) {
					setStatus(a, ArtifactStatus.PomNonJar);
				}
				else {
					try {
						List<ArtifactImpl> deps = a.getDependencies();
						synchronized(this) {
							for (Artifact dep : deps) {
								addArtifact(dep);
							}
							setStatus(a, ArtifactStatus.Analyzed);
							moreToDo = true;
						}
					}
					catch (ArtifactStateException ase) {
						// Never happens, state must be Pom to enter this block
						logger.error("Artifact state for " + a + "should have been Pom", ase);
					}
				}
			}
			else if (s.equals(ArtifactStatus.Queued)) {
				try {
					fetch(repositories, a, "pom");
					moreToDo = true;
				} catch (ArtifactNotFoundException e) {
					logger.warn("Could not find artifact "+ a, e);
				}
			}
			else if (s.equals(ArtifactStatus.Analyzed)) {
				try {
					fetch(repositories, a, "jar");
					moreToDo = true;
				} catch (ArtifactNotFoundException e) {
					logger.warn("Could not find artifact "+ a, e);
				}
			}
			else if (s.equals(ArtifactStatus.Jar)) {
				boolean fullyResolved = true;
				boolean resolutionError = false;
				try {
					List<ArtifactImpl> deps = a.getDependencies();
					Set<Artifact> seenArtifacts = new HashSet<Artifact>();
					seenArtifacts.add(a);
					for (ArtifactImpl dep : deps) {
						if (! status.containsKey(dep)) {
							addArtifact(dep);
						}
//						if (! status.get(dep).equals(ArtifactStatus.Ready)) {
//							fullyResolved = false;
//						}
						if (!fullyResolved(dep, seenArtifacts)) {
							fullyResolved = false;
						}
						if (status.get(dep).isError()) {
							resolutionError = true;
						}
					}
				}
				catch (ArtifactStateException ase) {
					logger.error("Artifact state for " + a + "should have been Jar", ase);
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
	 * Returns true if the artifact is fully resolved.
	 * 
	 * A fully resolved jar has:
	 * <ul>
	 * <li>a status of 'Ready', or</li>
	 * <li>a status of 'Jar' and all its dependencies are 'Ready' or have already been seen.</li>
	 * </ul>
	 * @param artifact
	 * @param seenArtifacts
	 * @return true if the artifact is fully resolved
	 */
	private boolean fullyResolved(ArtifactImpl artifact, Set<Artifact> seenArtifacts) {
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
				logger.error("Artifact state for " + artifact + "should have been Jar", e);
			}
		}
		return false;
	}
	
	/**
	 * Base directory for the local repository
	 */
	private File base;
	
	/**
	 * URL list of remote repository base URLs
	 */
	private List<URL> repositories = new ArrayList<URL>();
	
	/**
	 * Map of artifact to artifact status
	 */
	private Map<ArtifactImpl, ArtifactStatus> status =
		new HashMap<ArtifactImpl, ArtifactStatus>();
	
	/**
	 * Map of Artifact to a two element array of total
	 * size in current download and bytes downloaded. If
	 * the artifact has no pending downloads it will not
	 * appear as a key in this map.
	 */
	private Map<ArtifactImpl, DownloadStatusImpl> dlstatus = 
		new HashMap<ArtifactImpl, DownloadStatusImpl>();
	
	/**
	 * Local base directory for the specified artifact
	 */
	private File artifactDir(Artifact a) {
		String[] groupParts = a.getGroupId().split("\\.");
		File groupDir = base;
		for (String part : groupParts) {
			groupDir = new File(groupDir, part);
		}
		File artifactDir = new File(groupDir, a.getArtifactId());
		File versionDir = new File(artifactDir, a.getVersion());
		if (! versionDir.exists()) {
			versionDir.mkdirs();
		}
		return versionDir;
	}
	
	/**
	 * File object for POM for the specified artifact
	 */
	File pomFile(Artifact a) {
		return new File(artifactDir(a), a.getArtifactId()+"-"+a.getVersion()+".pom");
	}
	
	/**
	 * File object for Jar for the specified artifact
	 */
	private File jarFile(Artifact a) {
		return new File(artifactDir(a), a.getArtifactId()+"-"+a.getVersion()+".jar");
	}
	
	/**
	 * Copy the input stream to the output stream. Why doesn't java include
	 * this as a utility method somewhere?
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	void copyStream(InputStream is, OutputStream os, Artifact a) 
	throws IOException {
		int totalbytes = 0;
		byte[] buffer = new byte[1024];
		int bytesRead;
		try
		{
			while ((bytesRead = is.read(buffer)) != -1) {
				totalbytes += bytesRead;
				os.write(buffer, 0, bytesRead);
				dlstatus.get(a).setReadBytes(totalbytes);
			}
		}
		finally
		{
			dlstatus.get(a).setFinished();
			os.flush();
			os.close();
		}
	}
	
	synchronized void forcePom(ArtifactImpl a) throws ArtifactNotFoundException {
		fetch(repositories, a, "pom");
	}
	
	/**
	 * Fetch a remote POM or Jar and store it in the local repository
	 * @param repositories2 Array of URLs to maven2 repositories online
	 * @param a The artifact to resolve the POM for
	 * @param suffix The suffix of the file to fetch, either 'pom' or 'jar'
	 * @throws ArtifactNotFoundException
	 */
	//fixme: repositories2 isn't used
	private void fetch(List<URL> repositories2, ArtifactImpl a, String suffix)
	throws ArtifactNotFoundException {
		String fname = a.getArtifactId()+"-"+a.getVersion()+"."+suffix;
		String repositoryPath = a.getGroupId().replaceAll("\\.","/")+"/"+a.getArtifactId()+"/"+a.getVersion()+"/"+fname;
		for (URL repository : repositories) {
			try {
				URL pomLocation = new URL(repository, repositoryPath);
				URLConnection connection = pomLocation.openConnection();
				connection.connect();
				int length = connection.getContentLength();
				dlstatus.put(a, new DownloadStatusImpl(length));
				
				InputStream is = connection.getInputStream();
				// Opened the stream so presumably the thing exists
				// Create the appropriate directory structure within the local repository
				File toFile = ("pom".equals(suffix) ?pomFile(a):jarFile(a));
				if (! toFile.exists()) {
					toFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(toFile);
					setStatus(a, "pom".equals(suffix) ?ArtifactStatus.PomFetching:ArtifactStatus.JarFetching);
					copyStream(is, fos, a);
					dlstatus.remove(a);
					setStatus(a, "pom".equals(suffix) ?ArtifactStatus.Pom:ArtifactStatus.Jar);
					return;
				}
				else {
					// Strange, file shouldn't have been there
				}
				
			} catch (MalformedURLException e) {
				logger.error("Malformed repository URL: " + repository, e);
			} catch (IOException e) {
				if (e instanceof FileNotFoundException) {
					logger.debug(a+" not found in "+repository);
				} else {
					logger.warn("Could not read " + a, e);
				}
				// Ignore the exception, probably means we couldn't find the POM
				// in the repository. If there are more repositories in the list this
				// isn't neccessarily an issue.
			}
		}
		// No appropriate POM found in any of the repositories so throw an exception
		setStatus(a, "pom".equals(suffix) ?ArtifactStatus.PomFailed:ArtifactStatus.JarFailed);
		dlstatus.remove(a);
		throw new ArtifactNotFoundException("Can't find artifact for: "+a);
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
		if (! current.getPath().startsWith(base.getPath())) {
			logger.warn(current + " is outside base root " + base);
			return;
		}
		File[] subdirs = current.listFiles(isDirectory);
		if (subdirs == null || subdirs.length == 0) {
			if (! current.equals(base)) {
				groupDirs.add(current.getParentFile().getParentFile());
			}
		} else {
			for (File subDir : subdirs) {
				enumerateDirs(subDir, groupDirs);
			}
		}
	}
	
	/** 
	 * Clean the local repository by removing invalid 
	 * artifacts and directories. 
	 * <p>
	 * Empty directories will always be removed. 
	 * 
	 * <p><!--  removeFailing/removeUnknown not implemented
	 * If <code>removeFailing</code> is true, 
	 * all artifacts will be attempted re-downloaded, 
	 * if this fails, the artifact 
	 * will be removed. This option should only be used
	 * after verifying network access to the repositories.
	 * <p>
	 * If <code>removeUnknown</code> is true, files and
	 * directories not native to the Raven repositories will be removed.
	 * Be sure that the local repository directory is only
	 * used as a Raven repository before enabling this option.
	 * 
	 * 
	 * @param removeFailing Remove artifacts that can no longer be downloaded from repositories
	 * @param removeUnknown Remove unknown (non-Raven) files and directories
	 * -->
	 */
	public synchronized void clean() {
		if (! base.isDirectory()) {
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
				if (! child.getCanonicalFile().getParentFile().equals(dir)) {
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
			//logger.debug("Deleting " + dir);
			dir.delete();
		}
	}
	
	/**
	 * Scan the local repository for artifacts and populate
	 * the status map accordingly
	 */
	private synchronized void initialize() {
		if (! base.exists()) {
			// No base directory so create it
			base.mkdirs();
			// Don't need to check previous content, finished
			return;
		}
		// Fetch all subdirectories, assuming that each
		// subdirectory corresponds to a groupId
		Set<File> groupDirs = enumerateDirs(base);
		
		List<String> groupIds = new ArrayList<String>();
		for (File f : groupDirs) {
			File temp = f;			
			String groupName = "";
			while (! temp.equals(base)) {
				if (! "".equals(groupName)) {
					groupName = "." + groupName ;
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
					logger.debug("Null version list at "+artifactDir);
					continue;
				}
				for (File versionDir : versions) {
					String version = versionDir.getName();
					ArtifactImpl artifact = new ArtifactImpl(groupId, artifactId, version, this);
					// If there are any directories inside here we're not in a valid
					// artifact and have accidentally wandered down the wrong branch
					// of the file system. Blame the idiotic maven2 directory structure
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
					File pomFile = new File(versionDir, artifactId+"-"+version+".pom");
					if (pomFile.exists()) {
						status.put(artifact, ArtifactStatus.Pom);
					}
					File jarFile = new File(versionDir, artifactId+"-"+version+".jar");
					if (jarFile.exists()) {
						status.put(artifact, ArtifactStatus.Jar);
					}
				}
			}
		}
	}
	
	
	/**
	 * If the artifact specified is in either PomFetching or JarFetching state this returns
	 * a DownloadStatus object which provides a non updating snapshot of the file size (if known)
	 * and total bytes downloaded. The intent is to use this for progress bars within any client
	 * GUI code.
	 * @return DownloadStatus object representing the state of the current download
	 * for this artifact
	 * @param a Artifact to get status for
	 * @throws ArtifactNotFoundException if this repository doesn't contain the specified
	 * artifact.
	 * @throws ArtifactStateException if the artifact is found but isn't involved in a download
	 * at the present time.
	 */
	public DownloadStatus getDownloadStatus(Artifact a)	throws ArtifactStateException, ArtifactNotFoundException {
		if (status.containsKey(a)) {
			ArtifactStatus astatus = status.get(a);
			if (dlstatus.containsKey(a)) {
				return dlstatus.get(a);
			}
			else {
				throw new ArtifactStateException (astatus, new ArtifactStatus[]{ArtifactStatus.PomFetching, ArtifactStatus.JarFetching});	
			}
		}
		throw new ArtifactNotFoundException("Cant find artifact for: "+a);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Artifact a : status.keySet()) {
			sb
			.append(getStatus(a))
			.append("\t")
			.append(a.toString())
			.append("\n");
		}
		return sb.toString();
	}
}
