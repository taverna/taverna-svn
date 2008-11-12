package net.sf.taverna.t2.platform.raven.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.platform.pom.ArtifactDescription;
import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.pom.JarManager;
import net.sf.taverna.t2.platform.pom.PomParser;
import net.sf.taverna.t2.platform.raven.Raven;
import net.sf.taverna.t2.platform.raven.RavenException;

public class RavenImpl implements Raven {

	// Maintain a map of artifact identifier to class loader for non system
	// artifacts
	private Map<ArtifactIdentifier, ClassLoaderPair> loaders;

	private Set<ArtifactIdentifier> systemArtifacts;

	private ClassLoader parentLoader;

	private PomParser pomParser;

	private JarManager jarManager;

	public RavenImpl(ClassLoader parentLoader, PomParser pomParser,
			JarManager jarManager, Set<ArtifactIdentifier> systemArtifacts) {
		this.loaders = new HashMap<ArtifactIdentifier, ClassLoaderPair>();
		this.parentLoader = parentLoader;
		this.pomParser = pomParser;
		this.jarManager = jarManager;
		this.systemArtifacts = systemArtifacts;
	}

	public ClassLoader getLoader(ArtifactIdentifier id, List<URL> repositories)
			throws RavenException {
		try {
			// If this is a system artifact just return the parent class loader
			// immediately
			if (systemArtifacts.contains(id)) {
				return this.parentLoader;
			}
			if (loaders.containsKey(id.toString())) {
				// Return cached loader
				return loaders.get(id.toString()).outer;
			} else {
				// Build and return new loader, has the side effect of
				// populating
				// the cache
				return buildOrGetLoader(id, repositories).outer;
			}
		} catch (Exception e) {
			if (e instanceof RavenException) {
				throw (RavenException) e;
			} else {
				throw new RavenException(e);
			}
		}
	}

	private synchronized ClassLoaderPair buildOrGetLoader(
			ArtifactIdentifier id, List<URL> repositories) {
		return buildOrGetLoader(id, repositories,
				new ArrayList<ArtifactIdentifier>());
	}

	private synchronized ClassLoaderPair buildOrGetLoader(
			ArtifactIdentifier id, List<URL> repositories,
			List<ArtifactIdentifier> currentPath) {
		List<ArtifactIdentifier> breadCrumb = new ArrayList<ArtifactIdentifier>(
				currentPath);
		if (currentPath.contains(id)) {
			breadCrumb.add(id);
			StringBuffer sb = new StringBuffer();
			sb.append("\n");
			sb.append("### Circular dependency!");
			for (ArtifactIdentifier part : breadCrumb) {
				sb.append("\n###    " + part);
			}
			throw new RavenException(sb.toString());
		} else {
			breadCrumb.add(id);
		}
		if (loaders.containsKey(id)) {
			// Check the cache to prevent duplicates
			return loaders.get(id);
		}
		// First need to get the ArtifactDescription from the ArtifactIdentifier
		// so we can determine any child artifacts to link to.
		ArtifactDescription description = pomParser.getDescription(id,
				repositories);
		// Get the loaders for immediate children, from this we can generate the
		// set of descendants which will be the union of all the sets of
		// descendants from all immediate children
		List<ArtifactInnerClassLoader> immediateChildren = new ArrayList<ArtifactInnerClassLoader>();
		for (ArtifactIdentifier dependency : description
				.getMandatoryDependencies()) {
			if (systemArtifacts.contains(dependency) == false) {
				// Never add system artifacts to the dependency list, they are
				// effectively available from the parent class loader.
				immediateChildren.add(buildOrGetLoader(dependency,
						repositories, breadCrumb).inner);
			}
		}
		Set<ArtifactInnerClassLoader> descendantLoaders = new HashSet<ArtifactInnerClassLoader>();
		// Add each child, and each child's dependency set
		for (ArtifactInnerClassLoader childLoader : immediateChildren) {
			descendantLoaders.add(childLoader);
			descendantLoaders.addAll(childLoader.getDescendants());
		}
		try {
			ArtifactInnerClassLoader newInnerLoader = new ArtifactInnerClassLoader(
					jarManager.getArtifactJar(id, repositories),
					new ArrayList<ArtifactInnerClassLoader>(descendantLoaders),
					id);
			ClassLoader newOuterLoader = new ArtifactOuterClassLoader(
					parentLoader, newInnerLoader);
			ClassLoaderPair pair = new ClassLoaderPair(newInnerLoader,
					newOuterLoader);

			loaders.put(id, pair);
			// System.out.println("Created loader for " + id);
			return pair;
		} catch (Exception e) {
			throw new RavenException(e);
		}
	}

	public ClassLoader getParentClassLoader() {
		return this.parentLoader;
	}

	public Set<ArtifactIdentifier> getSystemArtifactSet() {
		return this.systemArtifacts;
	}

	/**
	 * Container class for a pair of inner and outer class loaders, one pair per
	 * non-system artifact
	 */
	class ClassLoaderPair {
		ArtifactInnerClassLoader inner;
		ClassLoader outer;

		ClassLoaderPair(ArtifactInnerClassLoader inner, ClassLoader outer) {
			this.inner = inner;
			this.outer = outer;
		}
	}

}
