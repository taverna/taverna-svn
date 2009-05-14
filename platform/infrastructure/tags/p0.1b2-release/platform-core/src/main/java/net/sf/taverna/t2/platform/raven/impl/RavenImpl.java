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

	private List<URL> defaultRepositories;

	public RavenImpl(ClassLoader parentLoader, PomParser pomParser,
			JarManager jarManager, Set<ArtifactIdentifier> systemArtifacts,
			List<URL> defaultRepositories) {
		this.loaders = new HashMap<ArtifactIdentifier, ClassLoaderPair>();
		this.parentLoader = parentLoader;
		this.pomParser = pomParser;
		this.jarManager = jarManager;
		this.systemArtifacts = systemArtifacts;
		this.defaultRepositories = defaultRepositories;
	}

	public List<URL> getDefaultRepositoryList() {
		return this.defaultRepositories;
	}

	public ClassLoader getLoader(ArtifactIdentifier id, List<URL> repositories)
			throws RavenException {
		if (repositories == null) {
			repositories = this.defaultRepositories;
		}
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
			// Check whether this dependency or a different version of it is
			// already present, exclude it if so
			boolean systemDependency = false;
			for (ArtifactIdentifier systemArtifact : systemArtifacts) {
				if (systemArtifact.equalsIgnoreVersion(dependency)) {
					systemDependency = true;
					break;
				}
			}
			if (!systemDependency) {
				// Track back along the breadcrumb path and check whether this
				// is an excluded artifact
				boolean excluded = false;
				Set<String> exclusions = new HashSet<String>();
				for (ArtifactIdentifier a : breadCrumb) {
					exclusions.addAll(pomParser.getDescription(a, repositories)
							.getExclusions());
				}
				for (String exclusion : exclusions) {
					if (exclusion.equals(dependency.getGroupId() + ":"
							+ dependency.getArtifactId())) {
						excluded = true;
						break;
					}
				}
				// Never add system artifacts to the dependency list, they are
				// effectively available from the parent class loader.
				if (!excluded) {
					// System.out.println("Adding " + dependency + " to " + id);
					immediateChildren.add(buildOrGetLoader(dependency,
							repositories, breadCrumb).inner);
				} else {
					// System.out.println("Excluding " + dependency + " from "
					// + id);
				}
			}
		}
		Set<ArtifactInnerClassLoader> descendantLoaders = new HashSet<ArtifactInnerClassLoader>();
		// Add each child, and each child's dependency set
		for (ArtifactInnerClassLoader childLoader : immediateChildren) {
			descendantLoaders.add(childLoader);
			descendantLoaders.addAll(childLoader.getDescendants());
		}
		// Now prune back, removing any loaders excluded by exclusion
		// definitions in the artifact description
		// This logic isn't quite right, it means we'll pick up exclusions from
		// artifacts that are themselves excluded but that's a pretty unpleasant
		// edge case anyway so I hope we won't encounter it. It's not clear
		// whether there's a clean way to handle it anyway
		Set<String> currentExclusions = new HashSet<String>();
		currentExclusions.addAll(description.getExclusions());
		for (ArtifactInnerClassLoader loader : descendantLoaders) {
			currentExclusions.addAll(pomParser.getDescription(
					loader.getArtifactIdentifier(), repositories)
					.getExclusions());
		}
		for (ArtifactInnerClassLoader loader : new ArrayList<ArtifactInnerClassLoader>(
				descendantLoaders)) {
			// Remove immediate exclusions
			String exclusionPart = loader.getArtifactIdentifier().getGroupId()
					+ ":" + loader.getArtifactIdentifier().getArtifactId();
			if (currentExclusions.contains(exclusionPart)) {
				descendantLoaders.remove(loader);
			}
		}
		try {
			ArtifactInnerClassLoader newInnerLoader = new ArtifactInnerClassLoader(
					jarManager.getArtifactJar(id, repositories),
					new ArrayList<ArtifactInnerClassLoader>(descendantLoaders),
					id, parentLoader);
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

	public ArtifactIdentifier definingArtifact(Object o) {
		ClassLoader cl = o.getClass().getClassLoader();
		if (cl instanceof ArtifactInnerClassLoader) {
			return ((ArtifactInnerClassLoader) cl).getArtifactIdentifier();
		}
		return null;
	}

	public PomParser getPomParser() {
		return this.pomParser;
	}

	/**
	 * Given a list of artifact identifiers return a map of artifact identifier
	 * to artifact description containing all descendants of the list of
	 * supplied artifacts, an artifact is included if it is the descendant of
	 * any artifact in the input list. The exception to this rule is that system
	 * to non-system dependencies are not followed, this reflects the behaviour
	 * of raven in that a system artifact will never link to a non system
	 * artifact because of the classloading strategy used
	 * 
	 * @param artifacts
	 *            a set of artifacts to use as starting points when building the
	 *            graph
	 * @param convergeSystemArtifacts
	 *            set to true to force any dependencies on an artifact with a
	 *            corresponding system artifact to use the system artifact
	 *            instead
	 * @return
	 */
	public Map<ArtifactIdentifier, ArtifactDescription> resolve(
			Set<ArtifactIdentifier> artifacts,
			Set<ArtifactIdentifier> ignoredArtifacts,
			boolean convergeSystemArtifacts) {
		Map<ArtifactIdentifier, ArtifactDescription> result;
		result = new HashMap<ArtifactIdentifier, ArtifactDescription>();

		// Populate the map initially, set 'finished' to false
		boolean finished = false;
		Set<String> currentExclusions = new HashSet<String>();
		for (ArtifactIdentifier initialArtifact : artifacts) {
			ArtifactDescription description = pomParser.getDescription(
					initialArtifact, defaultRepositories);
			result.put(initialArtifact, description);
			currentExclusions.addAll(description.getExclusions());
		}

		// Build the entire descendant set
		while (!finished) {
			finished = true;
			for (ArtifactDescription description : new ArrayList<ArtifactDescription>(
					result.values())) {
				boolean systemSource = (convergeSystemArtifacts && getSystemArtifactSet()
						.contains(description.getId()));
				for (ArtifactIdentifier dependency : description
						.getMandatoryDependencies()) {
					boolean systemDep = false;
					if (convergeSystemArtifacts) {
						for (ArtifactIdentifier systemArtifact : getSystemArtifactSet()) {
							if (systemArtifact.equalsIgnoreVersion(dependency)) {
								dependency = systemArtifact;
								systemDep = true;
								break;
							}
						}
					}
					if (result.containsKey(dependency)) {
						// Ignore, we've already done this one
					} else {
						// Check that this isn't excluded by something higher up
						if (!currentExclusions.contains(dependency.getGroupId()
								+ ":" + dependency.getArtifactId())
								&& !ignoredArtifacts.contains(dependency
										.getGroupId()
										+ ":" + dependency.getArtifactId())) {
							ArtifactDescription dependencyDescription = pomParser
									.getDescription(dependency,
											defaultRepositories);
							currentExclusions.addAll(dependencyDescription
									.getExclusions());
							if (!systemSource || (systemSource && systemDep)) {
								result.put(dependency, dependencyDescription);
								finished = false;
							}
							break;
						}
					}
				}
			}
		}
		return result;
	}
}
