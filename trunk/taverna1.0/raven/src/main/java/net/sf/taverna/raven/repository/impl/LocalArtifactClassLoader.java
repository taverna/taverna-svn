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
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.Repository;

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

	public Repository getRepository() {
		return repository;
	}

	protected LocalArtifactClassLoader(LocalRepository r, ArtifactImpl a)
			throws MalformedURLException, ArtifactStateException {
		super(new URL[] { r.jarFile(a).toURI().toURL() });
		repository = r;
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
		synchronized (LocalRepository.loaderMap) {
			LocalRepository.loaderMap.put(a, this);
			init(a); // Avoid people getting non-initialized instances
		}
	}

	protected LocalArtifactClassLoader(LocalRepository r, ClassLoader selfLoader) {
		super(new URL[0], selfLoader);				
		repository = r;
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
		return findClass(name, new HashSet<LocalArtifactClassLoader>());						
	}

	protected Class<?> findClass(String name,
			Set<LocalArtifactClassLoader> seenLoaders)
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
			Set<LocalArtifactClassLoader> seenLoaders)
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
				try {
					if (parent instanceof LocalArtifactClassLoader) {
						result = ((LocalArtifactClassLoader) parent).loadClass(
								name, seenLoaders);
					} else if (parent != null) {
						result = parent.loadClass(name);
					}
				} catch (ClassNotFoundException cnfe) {
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
}