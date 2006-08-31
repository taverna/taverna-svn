/**
 * 
 */
package net.sf.taverna.raven.repository.impl;

import net.sf.taverna.raven.repository.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;

/**
 * Represents the state of a local Maven2 repository
 * on disk. Manages the queue of pending fetches.
 * Create an instance of this class using the static
 * getRepository(File base) method passing it the location
 * of the on disk maven2 repository to access or create.
 * @author Tom Oinn
 */
public class LocalRepository implements Repository {

	/**
	 * Implementation of ClassLoader that uses the artifact
	 * metadata to manage any dependencies of the artifact
	 */
	class ArtifactClassLoader extends URLClassLoader {

		private List<ArtifactClassLoader> childLoaders = 
			new ArrayList<ArtifactClassLoader>();
		private Map<String, Class> classMap =
			new HashMap<String, Class>();
		private String name;
		
		protected ArtifactClassLoader(ArtifactImpl a) throws MalformedURLException, ArtifactStateException {
      // fixme: use jarFile(a).toURI().toURL()?
      super(new URL[]{LocalRepository.this.jarFile(a).toURL()});
			init(a);
			synchronized(loaderMap) {
				loaderMap.put(a, this);
			}
		}
		
		protected ArtifactClassLoader(ArtifactImpl a, ClassLoader parent) throws MalformedURLException, ArtifactStateException {
      // fixme: use jarFile(a).toURI().toURL()?
      super(new URL[]{LocalRepository.this.jarFile(a).toURL()}, parent);
			init(a);
			synchronized(loaderMap) {
				loaderMap.put(a, this);
			}
		}
		
		private void init(ArtifactImpl a) throws ArtifactStateException {
			List<ArtifactImpl> deps = a.getDependencies();
			this.name = a.toString();
			for (ArtifactImpl dep : deps) {
				synchronized(loaderMap) {
					ArtifactClassLoader ac = loaderMap.get(dep);
					if (ac == null) {
						try {
							ac = new ArtifactClassLoader(dep);
						} catch (MalformedURLException e) {
							// Never happens
							e.printStackTrace();
						}
						loaderMap.put(a, ac);
					}						
					childLoaders.add(ac);
				}
			}
		}
		
		@Override
    public String toString() {
			return "loader{"+this.name+"}";
		}
		
		@Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
			//System.out.println("Searching for '"+name+"' - "+this.toString());
			if (classMap.containsKey(name)) {
				//System.out.println("Returning cached '"+name+"' - "+this.toString());
				return classMap.get(name);
			}
			try {
				Class c = super.findClass(name);
				classMap.put(name, c);
				//System.out.println("Returning found '"+name+"' - "+this.toString());
				return c;
			} catch (ClassNotFoundException e) {
				//System.out.println("Trying children of "+this.toString());
				//for (ArtifactClassLoader ac : childLoaders) {
					//System.out.println("    "+ac.toString());
				//}
				for (ArtifactClassLoader ac : childLoaders) {
					try {
						return ac.findClass(name);
					}
					catch (ClassNotFoundException cnfe) {
						//System.out.println("No '"+name+"' in "+this.toString());
					}
				}
			}
			throw new ClassNotFoundException();
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
		this.base = base;
		initialize();
	}
	
	private static Map<File,LocalRepository> repositoryCache = new HashMap<File,LocalRepository>();
	/**
	 * Get a new or cached instance of LocalRepository for the supplied base directory,
	 * this is the method to use when you want to get hold of a LocalRepository.
	 * @param base The base directory for the m2 repository on disk
	 * @return LocalRepository instance for the base directory
	 */
	public static synchronized LocalRepository getRepository(File base) {
		if (repositoryCache.containsKey(base) == false) {
			repositoryCache.put(base, new LocalRepository(base));
		}
		return repositoryCache.get(base);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.raven.repository.impl.Repository#addArtifact(net.sf.taverna.raven.repository.impl.ArtifactImpl)
	 */
	public synchronized void addArtifact(Artifact a1) {
		ArtifactImpl a = new ArtifactImpl(a1, this);
		if (this.status.containsKey(a) == false) {
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
		if (status.containsKey(a) == false) {
			// No such artifact
			throw new ArtifactNotFoundException();
		}
		if (status.get(a).equals(ArtifactStatus.Ready) == false) {
			// Can't get a classloader yet, the artifact isn't ready
			throw new ArtifactStateException(status.get(a), new ArtifactStatus[]{ArtifactStatus.Ready});
		}
		if (loaderMap.containsKey(a)) {
			return loaderMap.get(a);
		}
		else {
			ClassLoader loader;
			try {
				if (parent == null) {
					loader = new ArtifactClassLoader(a, null);
				}
				else {
					loader = new ArtifactClassLoader(a, parent);
				}
				return loader;
			} catch (MalformedURLException e) {
				// Never happens
				e.printStackTrace();
			}
			return null;
		}
	}
	
	/**
	 * Given a Class object return the Artifact whose ArtifactClassLoader created it. If the classloader was
	 * not an instance of ArtifactClassLoader then return null
	 */
	public Artifact artifactForClass(Class c) throws ArtifactNotFoundException {
		synchronized(loaderMap) {
			for (Artifact a : loaderMap.keySet()) {
				if (loaderMap.get(a) == c.getClassLoader()) {
					return a;
				}
			}
		}
		throw new ArtifactNotFoundException("No artifact for Class : "+c.getName());
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.raven.repository.impl.Repository#update()
	 */
	public synchronized void update() {
		while (act())
    {
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
		synchronized(this.listeners) {
			if (listeners.contains(l) == false) {
				listeners.add(l);
			}
		}
	}
	
	public void removeRepositoryListener(RepositoryListener l) {
		synchronized(this.listeners) {
			listeners.remove(l);
		}
	}
	
	private synchronized void setStatus(ArtifactImpl a, ArtifactStatus newStatus) {
		if (status.containsKey(a) && status.get(a) != newStatus) {
			synchronized(listeners) {
				for (RepositoryListener l : listeners) {
					l.statusChanged(a, status.get(a), newStatus);
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
		if (status.containsKey(a) == false) {
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
				//System.out.println(a.toString());
				if ("jar".equals(a.getPackageType()) == false) {
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
					}
				}
			}
			else if (s.equals(ArtifactStatus.Queued)) {
				try {
					fetch(repositories, a, "pom");
					moreToDo = true;
				} catch (ArtifactNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (s.equals(ArtifactStatus.Analyzed)) {
				try {
					fetch(repositories, a, "jar");
					moreToDo = true;
				} catch (ArtifactNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (s.equals(ArtifactStatus.Jar)) {
				boolean fullyResolved = true;
				boolean resolutionError = false;
				try {
					List<ArtifactImpl> deps = a.getDependencies();
					for (ArtifactImpl dep : deps) {
						if (status.containsKey(dep) == false) {
							addArtifact(dep);
						}
						if (status.get(dep).equals(ArtifactStatus.Ready) == false) {
							fullyResolved = false;
						}
						if (status.get(dep).isError()) {
							resolutionError = true;
						}
					}
				}
				catch (ArtifactStateException ase) {
					// Never happens, status must be Jar to enter this block
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
		if (versionDir.exists() == false) {
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
		while ((bytesRead = is.read(buffer)) != -1) {
			totalbytes += bytesRead;
			os.write(buffer, 0, bytesRead);
			dlstatus.get(a).setReadBytes(totalbytes);
		}
		os.flush();
		os.close();
	}
	
	synchronized void forcePom(ArtifactImpl a) throws ArtifactNotFoundException {
		fetch(this.repositories, a, "pom");
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
				if (toFile.exists() == false) {
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
				// Invalid repository URL?
			} catch (IOException e) {
				if (e instanceof FileNotFoundException) {
					System.out.println(a.toString()+" not found in "+repository);
				}
				else {
					e.printStackTrace();
				}
				// Ignore the exception, probably means we couldn't find the POM
				// in the repository. If there are more repositories in the list this
				// isn't neccessarily an issue.
			}
		}
		// No appropriate POM found in any of the repositories so throw an exception
		setStatus(a, "pom".equals(suffix) ?ArtifactStatus.PomFailed:ArtifactStatus.JarFailed);
		dlstatus.remove(a);
		throw new ArtifactNotFoundException();
	}
	
	private void enumerateDirs(File current, Set<File> groupDirs) {
		File[] subdirs = current.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		if (subdirs == null || subdirs.length == 0) {
			if (current.equals(base) == false) {
				groupDirs.add(current.getParentFile().getParentFile());
			}
		}
		else {
			for (File subDir : subdirs) {
				enumerateDirs(subDir, groupDirs);
			}
		}
	}
	
	/**
	 * Scan the local repository for artifacts and populate
	 * the status map accordingly
	 */
	private synchronized void initialize() {
		if (base.exists() == false) {
			// No base directory so create it
			base.mkdirs();
			return;
		}
		// Fetch all subdirectories, assuming that each
		// subdirectory corresponds to a groupId
		Set<File> groupDirs = new HashSet<File>();
		enumerateDirs(base, groupDirs);
		
		List<String> groupIds = new ArrayList<String>();
		for (File f : groupDirs) {
			File temp = f;
			String groupName = "";

			while (temp.equals(base) == false) {
				if ("".equals(groupName) == false) {
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
			String[] artifactIds = groupDirectory.list(new FilenameFilter() {
				public boolean accept(File d, String name) {
					return d.isDirectory();
				}			
			});
			for (String artifactId : artifactIds) {
				File artifactDirectory = new File(groupDirectory, artifactId);
				String[] versions = artifactDirectory.list(new FilenameFilter() {
						public boolean accept(File d, String name) {
								return d.isDirectory();
						}
				});
				for (String version : versions) {
					ArtifactImpl artifact = new ArtifactImpl(groupId, artifactId, version, this);
					File versionDirectory = new File(artifactDirectory, version);
					// If there are any directories inside here we're not in a valid
					// artifact and have accidentally wandered down the wrong branch
					// of the file system. Blame the idiotic maven2 directory structure
					// with groups split into subdirectories!
					boolean foundSubDir = false;
					//System.out.println("Version directory : "+versionDirectory.toString());
					if (versionDirectory.isDirectory()) {
						for (File sub : versionDirectory.listFiles()) {
							if (sub.isDirectory()) {
								foundSubDir = true;
							}
						}
						if (foundSubDir == false) {
							status.put(artifact, ArtifactStatus.Queued);
							File pomFile = new File(versionDirectory, artifactId+"-"+version+".pom");
							if (pomFile.exists()) {
								status.put(artifact, ArtifactStatus.Pom);
							}
							File jarFile = new File(versionDirectory, artifactId+"-"+version+".jar");
							if (jarFile.exists()) {
								status.put(artifact, ArtifactStatus.Jar);
							}
						}
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
		throw new ArtifactNotFoundException();
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
