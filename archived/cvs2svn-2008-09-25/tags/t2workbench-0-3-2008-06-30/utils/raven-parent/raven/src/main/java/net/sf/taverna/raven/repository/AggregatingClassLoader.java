package net.sf.taverna.raven.repository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * ClassLoader implementation as a convenience to allow searching of multiple
 * artifact based classloaders. This can be used for cases such as the Beanshell
 * where only a single ClassLoader can be specified, should such cases require
 * classes from multiple independant artifacts this class can aggregate a set of
 * classloaders, delegating to each in turn until an appropriate match is found
 * for a class or resource.
 * 
 * @author Tom Oinn
 */
public class AggregatingClassLoader extends ClassLoader {

	List<ClassLoader> loaders = new ArrayList<ClassLoader>();

	public AggregatingClassLoader(Repository rep, List<Artifact> artifacts)
			throws ArtifactStateException, ArtifactNotFoundException {
		super();
		init(rep, artifacts);
	}

	public AggregatingClassLoader(Repository rep, List<Artifact> artifacts,
			ClassLoader parent) throws ArtifactStateException,
			ArtifactNotFoundException {
		super(parent);
		init(rep, artifacts);
	}

	@Override
	public URL findResource(String name) {
		for (ClassLoader loader : loaders) {
			URL u = loader.getResource(name);
			if (u != null) {
				return u;
			}
		}
		return null;
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		List<URL> results = new ArrayList<URL>();
		for (ClassLoader loader : loaders) {
			Enumeration<URL> en = loader.getResources(name);
			for (; en.hasMoreElements();) {
				results.add(en.nextElement());
			}
		}
		final Iterator<URL> i = results.iterator();
		return new Enumeration<URL>() {
			public boolean hasMoreElements() {
				return i.hasNext();
			}

			public URL nextElement() {
				return i.next();
			}
		};

	}

	private void init(Repository rep, List<Artifact> artifacts)
			throws ArtifactStateException, ArtifactNotFoundException {
		for (Artifact a : artifacts) {
			loaders.add(rep.getLoader(a, null));
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		for (ClassLoader loader : loaders) {
			try {
				return loader.loadClass(name);
			} catch (ClassNotFoundException cnfe) {
				//
			}
		}
		throw new ClassNotFoundException("Unable to locate a loader for "
				+ name);
	}

}
