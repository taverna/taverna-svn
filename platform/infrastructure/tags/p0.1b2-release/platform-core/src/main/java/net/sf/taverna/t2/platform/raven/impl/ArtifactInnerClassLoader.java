package net.sf.taverna.t2.platform.raven.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.raven.ArtifactClassLoader;

/**
 * A class loader that has no parent class loader but delegates to other class
 * loaders of this type to resolve child dependencies. We have no parent here
 * because we use an ArtifactOuterClassLoader to link from a parent class loader
 * to the inner loader used for a given artifact - the outer class loader is the
 * one returned from the RavenImpl methods.
 * 
 * @author Tom Oinn
 * 
 */
public class ArtifactInnerClassLoader extends URLClassLoader implements
		ArtifactClassLoader {

	/**
	 * We keep a fully resolved list of all descendants of this class loader to
	 * avoid duplicate resource and class loads due to multiple paths to the
	 * same dependency.
	 */
	private List<ArtifactInnerClassLoader> descendants;

	private ArtifactIdentifier artifact;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("   ArtifactInnerClassLoader(" + artifact + ")  {\n");
		for (ArtifactInnerClassLoader child : descendants) {
			sb.append("      " + child.artifact + "\n");
		}
		sb.append("   }\n");
		return sb.toString();
	}

	ArtifactInnerClassLoader(File artifactJar,
			List<ArtifactInnerClassLoader> descendants,
			ArtifactIdentifier artifact, ClassLoader parent)
			throws MalformedURLException {
		super(new URL[] { artifactJar.toURI().toURL() }, parent);
		this.descendants = descendants;
		this.artifact = artifact;
	}

	List<ArtifactInnerClassLoader> getDescendants() {
		return this.descendants;
	}

	private Class<?> findClassNoRecurse(String className)
			throws ClassNotFoundException {
		synchronized (this) {
			Class<?> c = findLoadedClass(className);
			if (c != null) {
				return c;
			}
			return super.findClass(className);
		}
	}

	private URL findResourceNoRecurse(String resourceName) {
		return super.findResource(resourceName);
	}

	private Enumeration<URL> findResourcesNoRecurse(String resourceName)
			throws IOException {
		return super.findResources(resourceName);
	}

	@Override
	public URL findResource(String resourceName) {
		URL result = super.findResource(resourceName);
		if (result != null) {
			return result;
		}
		for (ArtifactInnerClassLoader loader : descendants) {
			result = loader.findResourceNoRecurse(resourceName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public Enumeration<URL> findResources(String resourceName)
			throws IOException {
		// Build up results in this vector
		Vector<URL> results = new Vector<URL>();
		addAllToVector(super.findResources(resourceName), results);
		for (ArtifactInnerClassLoader loader : descendants) {
			addAllToVector(loader.findResourcesNoRecurse(resourceName), results);
		}
		return results.elements();
	}

	// Convenience method to add everything in an enumeration to a vector
	static void addAllToVector(Enumeration<URL> e, Vector<URL> v) {
		for (; e.hasMoreElements();) {
			v.add(e.nextElement());
		}
	}

	@Override
	protected Class<?> findClass(String className)
			throws ClassNotFoundException {
		try {
			if (findLoadedClass(className) != null) {
				return findLoadedClass(className);
			}
			synchronized (this) {
				return super.findClass(className);
			}
		} catch (ClassNotFoundException cnfe) {
			// Not found in this jar, so check all the descendants
			for (ArtifactInnerClassLoader loader : descendants) {
				try {
					return loader.findClassNoRecurse(className);
				} catch (ClassNotFoundException cnfe2) {
					// Do nothing
				}
			}
		}
		throw new ClassNotFoundException("Cannot locate class '" + className
				+ "'");
	}

	public ArtifactIdentifier getArtifactIdentifier() {
		return this.artifact;
	}
}
