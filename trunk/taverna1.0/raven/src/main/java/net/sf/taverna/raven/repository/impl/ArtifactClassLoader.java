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
 * Implementation of ClassLoader that uses the artifact metadata to manage
 * any dependencies of the artifact
 */
public class ArtifactClassLoader extends URLClassLoader {

	private static Log logger = Log.getLogger(ArtifactClassLoader.class);
	
	private final LocalRepository repository;

	private List<ArtifactClassLoader> childLoaders = new ArrayList<ArtifactClassLoader>();

	private Map<String, Class> classMap = new HashMap<String, Class>();

	private String name;

	public Repository getRepository() {
		return repository;
	}

	protected ArtifactClassLoader(LocalRepository repository, ArtifactImpl a)
			throws MalformedURLException, ArtifactStateException {
		super(new URL[] { repository.jarFile(a).toURI().toURL() });
		this.repository = repository;
		synchronized (LocalRepository.loaderMap) {
			LocalRepository.loaderMap.put(a, this);
		}
		init(a);
	}

	protected ArtifactClassLoader(LocalRepository repository, ArtifactImpl a, ClassLoader parent)
			throws MalformedURLException, ArtifactStateException {
		super(new URL[] { repository.jarFile(a).toURI().toURL() }, parent);
		this.repository = repository;
		synchronized (LocalRepository.loaderMap) {
			LocalRepository.loaderMap.put(a, this);
		}
		init(a);
	}

	protected ArtifactClassLoader(LocalRepository repository, ClassLoader selfLoader) {
		super(new URL[0], selfLoader);
		this.repository = repository;
	}

	private void init(ArtifactImpl a) throws ArtifactStateException {
		List<ArtifactImpl> deps = a.getDependencies();
		name = a.toString();
		for (ArtifactImpl dep : deps) {
			synchronized (LocalRepository.loaderMap) {
				ArtifactClassLoader ac = LocalRepository.loaderMap.get(dep);
				if (ac == null) {
					try {
						ac = new ArtifactClassLoader(this.repository, dep);
					} catch (MalformedURLException e) {
						logger
								.error("Malformed URL when loading " + dep,
										e);
					}
					// loaderMap.put(a, ac);
				}
				childLoaders.add(ac);
			}
		}
	}

	@Override
	public URL findResource(String name) {
		return findFirstInstanceOfResource(
				new HashSet<ArtifactClassLoader>(), name);
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		Set<URL> resourceLocations = new HashSet<URL>();
		enumerateResources(new HashSet<ArtifactClassLoader>(),
				resourceLocations, name);
		return Collections.enumeration(resourceLocations);
	}

	private URL findFirstInstanceOfResource(
			Set<ArtifactClassLoader> alreadySeen, String name) {
		URL resourceURL = super.findResource(name);
		if (resourceURL != null) {
			return resourceURL;
		}
		alreadySeen.add(this);
		for (ArtifactClassLoader cl : childLoaders) {
			if (!alreadySeen.contains(cl)) {
				resourceURL = cl.findFirstInstanceOfResource(alreadySeen,
						name);
				if (resourceURL != null) {
					return resourceURL;
				}
			}
		}
		return null;
	}

	private void enumerateResources(Set<ArtifactClassLoader> alreadySeen,
			Set<URL> resourceLocations, String name) throws IOException {
		alreadySeen.add(this);
		URL resourceURL = super.findResource(name);
		if (resourceURL != null) {
			resourceLocations.add(resourceURL);
		}
		for (ArtifactClassLoader cl : childLoaders) {
			if (!alreadySeen.contains(cl)) {
				cl.enumerateResources(alreadySeen, resourceLocations, name);
			}
		}
	}

	@Override
	public String toString() {
		return "loader{" + name + "} from "
				+ System.identityHashCode(this.repository);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return findClass(name, new HashSet<ArtifactClassLoader>());
	}

	protected Class<?> findClass(String name,
			Set<ArtifactClassLoader> seenLoaders)
			throws ClassNotFoundException {
		logger.debug("Searching for '" + name + "' - " + this);
		seenLoaders.add(this);
		if (classMap.containsKey(name)) {
			logger.debug("Returning cached '" + name + "' - " + this);
			return classMap.get(name);
		}
		try {
			Class c = super.findClass(name);
			classMap.put(name, c);
			logger.debug("Returning found '" + name + "' - " + this);
			return c;
		} catch (ClassNotFoundException e) {
			// logger.debug("Trying children of "+this);
			// for (ArtifactClassLoader ac : childLoaders) {
			// logger.debug(" "+ac.toString());
			// }
			for (ArtifactClassLoader ac : childLoaders) {
				if (!seenLoaders.contains(ac)) {
					try {
						return ac.loadClass(name, seenLoaders);
					} catch (ClassNotFoundException cnfe) {
						logger.debug("No '" + name + "' in " + this);
					}
				}
			}
		}
		throw new ClassNotFoundException(name);
	}

	private Class<?> loadClass(String name,
			Set<ArtifactClassLoader> seenLoaders)
			throws ClassNotFoundException {
		Class loadedClass = findLoadedClass(name);
		if (loadedClass != null) {
			return loadedClass;
		}
		ClassLoader parent = getParent();
		try {
			if (parent instanceof ArtifactClassLoader) {
				return ((ArtifactClassLoader) parent).loadClass(name,
						seenLoaders);
			} else if (parent != null) {
				return parent.loadClass(name);
			}
		} catch (ClassNotFoundException cnfe) {
		}
		return findClass(name, seenLoaders);
	}
}