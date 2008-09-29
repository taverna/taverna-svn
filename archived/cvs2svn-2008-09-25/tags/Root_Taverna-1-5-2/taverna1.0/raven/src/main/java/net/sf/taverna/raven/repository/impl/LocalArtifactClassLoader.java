package net.sf.taverna.raven.repository.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.Repository;
import sun.misc.CompoundEnumeration;

/**
 * Implementation of ClassLoader that uses the artifact metadata to manage any
 * dependencies of the artifact
 */
public class LocalArtifactClassLoader extends URLClassLoader {

	private static Log logger = Log.getLogger(LocalArtifactClassLoader.class);

	private List<LocalArtifactClassLoader> childLoaders = new ArrayList<LocalArtifactClassLoader>();

	private Map<String, Class> classMap = new HashMap<String, Class>();

	private String name;

	private LocalRepository repository;

	private Set<String> unknownClasses = new HashSet<String>();
	
	public Repository getRepository() {
		return repository;
	}

	public Artifact getArtifact() {
		return this.artifact;
	}
	
	private Artifact artifact;
	
	protected LocalArtifactClassLoader(LocalRepository r, ArtifactImpl a)
			throws MalformedURLException, ArtifactStateException {
		super(new URL[] { r.jarFile(a).toURI().toURL() });
		repository = r;
		this.artifact = a;
		synchronized (LocalRepository.loaderMap) {
			LocalRepository.loaderMap.put(a, this);
			init(a); // Avoid people getting non-initialized instances
		}
	}

	protected LocalArtifactClassLoader(LocalRepository r, ArtifactImpl a,
			ClassLoader parent) throws MalformedURLException,
			ArtifactStateException {
		super(new URL[] { r.jarFile(a).toURI().toURL() }, parent);
		repository = r;
		this.artifact = a;
		synchronized (LocalRepository.loaderMap) {
			LocalRepository.loaderMap.put(a, this);
			init(a); // Avoid people getting non-initialized instances
		}
	}

	protected LocalArtifactClassLoader(LocalRepository r, ClassLoader selfLoader, Artifact ravenArtifact) {
		super(new URL[0], selfLoader);				
		repository = r;
		this.artifact = ravenArtifact;
	}

	private void init(ArtifactImpl a) throws ArtifactStateException {
		List<ArtifactImpl> deps = a.getDependencies();
		name = a.toString();
		for (ArtifactImpl dep : deps) {
			synchronized (LocalRepository.loaderMap) {
				LocalArtifactClassLoader ac = LocalRepository.loaderMap
						.get(dep);
				if (ac == null) {
					try {
						ac = new LocalArtifactClassLoader(repository, dep);
					} catch (MalformedURLException e) {
						logger.error("Malformed URL when loading " + dep, e);
					}
					// LocalRepository.loaderMap.put(a, ac);
				}
				childLoaders.add(ac);
			}
		}
	}

	@Override
	public URL findResource(String name) {
		return findFirstInstanceOfResource(
				new HashSet<LocalArtifactClassLoader>(), name);
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		Set<URL> resourceLocations = new HashSet<URL>();
		enumerateResources(new HashSet<LocalArtifactClassLoader>(),
				resourceLocations, name);
		return Collections.enumeration(resourceLocations);
	}

	private URL findFirstInstanceOfResource(
			Set<LocalArtifactClassLoader> alreadySeen, String name) {
		URL resourceURL = super.findResource(name);
		if (resourceURL != null) {
			return resourceURL;
		}
		alreadySeen.add(this);
		for (LocalArtifactClassLoader cl : childLoaders) {
			if (!alreadySeen.contains(cl)) {
				resourceURL = cl.findFirstInstanceOfResource(alreadySeen, name);
				if (resourceURL != null) {
					return resourceURL;
				}
			}
		}
		return null;
	}

	private void enumerateResources(Set<LocalArtifactClassLoader> alreadySeen,
			Set<URL> resourceLocations, String name) throws IOException {
		alreadySeen.add(this);
		URL resourceURL = super.findResource(name);
		if (resourceURL != null) {
			resourceLocations.add(resourceURL);
		}
		for (LocalArtifactClassLoader cl : childLoaders) {
			if (!alreadySeen.contains(cl)) {
				cl.enumerateResources(alreadySeen, resourceLocations, name);
			}
		}
	}

	@Override
	public String toString() {
		return "loader{" + name + "} from "
				+ System.identityHashCode(repository);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {		
		try {
			return findClass(name, new HashSet<ClassLoader>());						
		} catch (ClassNotFoundException ex) {
			if (! unknownClasses.contains(name)) {
				// Only log it once
				unknownClasses.add(name);
				logger.info("Could not find " + name + " in " + this.name);
				logger.debug(ex);
			}
			throw ex;
		}
	}

	protected Class<?> findClass(String name,
			Set<ClassLoader> seenLoaders)
			throws ClassNotFoundException {
		Class result = null;
		logger.debug("Searching for '" + name + "' - " + this);
		seenLoaders.add(this);		

		if (classMap.containsKey(name)) {
			logger.debug("Returning cached '" + name + "' - " + this);
			result = classMap.get(name);
		} else {
			try {
				result = super.findClass(name);
				logger.debug("Returning found '" + name + "' - " + this);
			} catch (ClassNotFoundException e) {
				for (LocalArtifactClassLoader ac : childLoaders) {
					if (!seenLoaders.contains(ac)) {
						try {
							result = ac.loadClass(name, seenLoaders);
						} catch (ClassNotFoundException cnfe) {
							logger.debug("No '" + name + "' in " + this);
						}
					}
				}
			} catch (Error e) {
				logger.error("Error finding class " + name + " ACL="+this, e);
			}
		}
		if (result == null) {
			throw new ClassNotFoundException(name);
		}

		if (!classMap.containsKey(name)) {
			classMap.put(name, result);
		}
		
		return result;
	}

	private Class<?> loadClass(String name,
			Set<ClassLoader> seenLoaders)
			throws ClassNotFoundException {
		Class result = null;
		if (classMap.get(name) != null) {
			result = classMap.get(name);
		} else {
			Class loadedClass = findLoadedClass(name);
			if (loadedClass != null) {
				result = loadedClass;
			} else {
				ClassLoader parent = getParent();
				if (parent!=null && !seenLoaders.contains(parent)) {
					try {
						if (parent instanceof LocalArtifactClassLoader) {
							result = ((LocalArtifactClassLoader) parent).loadClass(
									name, seenLoaders);
						} else if (parent != null) {
							if (validClassLoaderForName(parent,name))
								result = parent.loadClass(name);
						}
					} catch (ClassNotFoundException cnfe) {
						seenLoaders.add(parent);
					}					
				}
				if (result == null)
					result = findClass(name, seenLoaders);
			}
		}
		if (!classMap.containsKey(name)) {
			classMap.put(name, result);
		}
		return result;
	}
	
	/**
	 * Temporary patch to prevent a URLClassLoader used to bootstrap Taverna being used
	 * to find non-raven classes. Otherwise, every URL is searched for the class - that it will
	 * never find, which is very time consuming, especially as more mirror sites are included.
	 * 
	 * This is for those using the 1.5.0 bootstrap. The 1.5.1 bootstrap first tries to load Raven
	 * with local URLS only, then trying remote if that fails. This means that with this bootstrap
	 * this problem only now exists on the first run.
	 * @param parent
	 * @param name
	 * @return
	 */
	private boolean validClassLoaderForName(ClassLoader parent, String name) {
		if (!isParentRavenClassLoader()) return true;		
		
		//the only class of the package net.sf.taverna.raven that isn't part of the raven artifact is Log4jLog - which would shouldn't be found here anyway
		if (name.startsWith("net.sf.taverna.raven") && !name.endsWith("Log4jLog")) return true;
		
		return false;
	}

	@Override
	/**
	 * Overridden to prevent it checking parents if the parent is the URLClassLoader
	 * used to bootstrap raven. Otherwise it has to download and search each raven.jar
	 * for every repository, including mirror repositories, defined.
	 */
	public Enumeration<URL> getResources(String name) throws IOException {
		if (getParent()==null || !isParentRavenClassLoader()) {
			return super.getResources(name);			
		}
		Enumeration[] tmp = new Enumeration[2];
		tmp[1]=findResources(name);
		
		return new CompoundEnumeration<URL>(tmp);
	}
	
	private boolean isParentRavenClassLoader() {
		boolean result = false;
		if (getParent()!=null && getParent() instanceof URLClassLoader) {
			URLClassLoader loader = (URLClassLoader)getParent();
			for (URL url : loader.getURLs()) {
				if (url.toExternalForm().contains("uk/org/mygrid/taverna/raven/raven/")) result=true;
				break;
			}
		}
		return result;
	}
	
	
}