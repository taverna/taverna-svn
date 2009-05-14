package net.sf.taverna.t2.platform.raven.ui.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.platform.pom.ArtifactDescription;
import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.raven.Raven;
import net.sf.taverna.t2.platform.raven.ui.DependencyGraphBuilder;

/**
 * Tool to derive the set of dependencies from the t2 artifacts and render it as
 * a vaguely sane graph in graphviz dot format. It can remove any dependencies
 * implied by transitivity and has options to determine whether optional
 * dependencies are displayed. It can also be configured to converge system
 * artifact versions, this will force all artifacts to adopt the same version as
 * the system artifact matching them in both artifact and group identifier.
 * 
 * @author Tom Oinn
 * 
 */
public class DependencyGraphBuilderImpl implements DependencyGraphBuilder {

	// Colour properties for the generated graph
	static final String[] colours = new String[] { "cornsilk1", "ghostwhite" };
	static final String[] nodeColours = new String[] { "burlywood2",
			"cadetblue2", "chartreuse2", "chocolate2", "coral2",
			"darkgoldenrod1", "darkorchid1", "deeppink2", "gold1",
			"firebrick1", "darkseagreen2", "darkslategray1" };
	static final String defaultNodeColour = "darkseagreen2";

	private Set<ArtifactIdentifier> ignoredArtifacts;
	private Raven raven;

	/**
	 * Create a new dependency graph builder
	 */
	public DependencyGraphBuilderImpl() {
		this.ignoredArtifacts = new HashSet<ArtifactIdentifier>();
	}

	/**
	 * Set the ignored artifacts set
	 */
	public void setIgnoredArtifactSet(Set<String> ignore) {
		ignoredArtifacts.clear();
		for (String id : ignore) {
			ignoredArtifacts.add(new ArtifactIdentifier(id));
		}
	}

	/**
	 * Set a raven instance used by this dependency graph builder
	 */
	public void setRaven(Raven raven) {
		this.raven = raven;
	}

	/**
	 * Build the dependency graph
	 * 
	 * @param artifacts
	 *            a set of artifacts to use as starting points when building the
	 *            graph
	 * @param perNodeColouring
	 *            set to true to colour nodes by group from a fixed palette,
	 *            cycling if there are more groups than entries in the palette
	 * @param showGroupsAsCluster
	 *            set to true to force artifacts in the same group to be
	 *            clustered in the graph
	 * @param compactClusters
	 *            set to true to only show clusters with direct artifact
	 *            children, false to show all clusters leading to an artifact
	 * @param removeTransitiveDependencies
	 *            set to true to remove any direct dependencies which are
	 *            entirely implied by (mandatory) transitive dependencies
	 * @param showOptionalDependencies
	 *            set to true to show optional dependencies between artifacts in
	 *            the graph as dotted lines, false to not show them at all
	 * @param convergeSystemArtifacts
	 *            set to true to force any dependencies on an artifact with a
	 *            corresponding system artifact to use the system artifact
	 *            instead
	 * @return a dot format string which can be rendered to show dependency
	 *         information
	 */
	public String getDependencyGraph(Set<ArtifactIdentifier> artifacts,
			boolean perNodeColouring, boolean showGroupsAsCluster,
			boolean compactClusters, boolean removeTransitiveDependencies,
			boolean showOptionalDependencies, boolean convergeSystemArtifacts,
			Set<ArtifactIdentifier> excludedArtifacts) {
		colourIndex = 0;
		if (excludedArtifacts != null) {
			this.ignoredArtifacts = excludedArtifacts;
		}
		// Calculate node set and grouping
		Map<ArtifactIdentifier, ArtifactDescription> artifactMap = raven
				.resolve(artifacts, ignoredArtifacts, convergeSystemArtifacts);
		ArtifactGroup root = new ArtifactGroup("");
		root.buildGroups(artifactMap.keySet());

		StringBuffer sb = new StringBuffer();
		sb.append("digraph G {\n");
		sb.append("  node [style=filled, peripheries=2];\n");
		sb.append(root.toString(-1, showGroupsAsCluster, compactClusters,
				perNodeColouring, convergeSystemArtifacts, artifacts));
		sb.append(getEdges(artifactMap, removeTransitiveDependencies,
				showOptionalDependencies, convergeSystemArtifacts));
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Return a block of text containing graphviz edge definitions for the
	 * dependencies between the specified artifacts
	 * 
	 * @param artifacts
	 *            a map of artifact identifier to artifact description
	 *            containing all artifacts in the eventual graph
	 * @param removeTransitiveDependencies
	 *            set to true to remove all dependencies which are implied by
	 *            transitivity, this cleans up the graph a lot but means that
	 *            explicitly declared dependencies may not appear. The actual
	 *            dependency structure is preserved however as dependency in
	 *            maven is transitive
	 * @param showOptionalDependencies
	 *            set to true to show optional dependencies, these are shown
	 *            only if both artifacts concerned are already in the graph,
	 *            optional dependencies are not taken into account when
	 *            generating the node list. Optional dependencies are denoted by
	 *            dotted lines in the dependency graph
	 * @param convergeSystemArtifacts
	 *            set to true to force any dependencies on an artifact with a
	 *            corresponding system artifact to use the system artifact
	 *            instead. This will also prevent any optional dependency from a
	 *            system to a non system artifact from showing, system artifacts
	 *            by definition will not 'see' a non system one
	 * @return a block of text that can be inserted into a dot format string and
	 *         which contains edge definitions showing dependencies between
	 *         artifact nodes
	 */
	private String getEdges(
			Map<ArtifactIdentifier, ArtifactDescription> artifacts,
			boolean removeTransitiveDependencies,
			boolean showOptionalDependencies, boolean convergeSystemArtifacts) {
		StringBuffer sb = new StringBuffer();
		/**
		 * if (removeTransitiveDependencies) { Map<ArtifactIdentifier,
		 * Set<ArtifactIdentifier>> ancestors; ancestors = new
		 * HashMap<ArtifactIdentifier, Set<ArtifactIdentifier>>(); for
		 * (ArtifactIdentifier id : artifacts.keySet()) { ancestors.put(id,
		 * transitiveDepsFor(convergeId(id, convergeSystemArtifacts),
		 * artifacts.keySet(), convergeSystemArtifacts)); } for
		 * (ArtifactDescription desc : artifacts.values()) {
		 * List<ArtifactIdentifier> depsInScope = new
		 * ArrayList<ArtifactIdentifier>(); for (ArtifactIdentifier dep :
		 * desc.getMandatoryDependencies()) { dep = convergeId(dep,
		 * convergeSystemArtifacts); if (artifacts.keySet().contains(dep) &&
		 * !dep.equals(convergeId(desc.getId(), convergeSystemArtifacts))) {
		 * depsInScope.add(dep); } } for (ArtifactIdentifier dep : depsInScope)
		 * { dep = convergeId(dep, convergeSystemArtifacts); boolean
		 * foundTransitiveRoute = false; for (ArtifactIdentifier check :
		 * depsInScope) { if (!check.equals(dep) &&
		 * ancestors.get(check).contains(dep) // Next clause prevents removal of
		 * a dependency // if the transitive path leads back through the //
		 * original node. This will leave some // dependencies that could be
		 * removed but is the // fastest way to make this work with broken //
		 * poms with circular dependencies such as // biomoby
		 * 
		 * // was (check) && !ancestors.get(check).contains(
		 * convergeId(desc.getId(), convergeSystemArtifacts))) {
		 * foundTransitiveRoute = true; break; } if (!check.equals(dep)) { if
		 * (artifacts.get(check).getMandatoryDependencies() .contains(dep) &&
		 * artifacts.get( convergeId(desc.getId(), convergeSystemArtifacts))
		 * .getMandatoryDependencies() .contains(check) && !artifacts
		 * .get(check) .getMandatoryDependencies() .contains(
		 * convergeId(desc.getId(), convergeSystemArtifacts))) {
		 * foundTransitiveRoute = true; break; } } } if (!foundTransitiveRoute)
		 * { if (!ignoredArtifacts.contains(dep) &&
		 * !ignoredArtifacts.contains(convergeId(desc .getId(),
		 * convergeSystemArtifacts))) { if (edgeValid(convergeSystemArtifacts,
		 * desc.getId(), dep)) { sb.append("  " +
		 * buildId(convergeId(desc.getId(), convergeSystemArtifacts)) + "->" +
		 * buildId(dep) + ";\n"); } } } } } }
		 */
		if (removeTransitiveDependencies) {
			Graph g = new Graph(artifacts, convergeSystemArtifacts);
			sb.append(g.toString());
		} else {
			for (ArtifactDescription desc : artifacts.values()) {
				for (ArtifactIdentifier dep : desc.getMandatoryDependencies()) {
					dep = convergeId(dep, convergeSystemArtifacts);
					if (artifacts.keySet().contains(dep)
							&& !ignoredArtifacts.contains(dep)
							&& !ignoredArtifacts.contains(convergeId(desc
									.getId(), convergeSystemArtifacts))) {
						if (edgeValid(convergeSystemArtifacts, desc.getId(),
								dep)) {
							sb.append("  " + buildId(desc.getId()) + "->"
									+ buildId(dep) + ";\n");
						}
					}
				}
			}
		}
		if (showOptionalDependencies) {
			for (ArtifactDescription desc : artifacts.values()) {
				for (ArtifactIdentifier dep : desc.getOptionalDependencies()) {
					dep = convergeId(dep, convergeSystemArtifacts);
					if (!ignoredArtifacts.contains(dep)
							&& !ignoredArtifacts.contains(convergeId(desc
									.getId(), convergeSystemArtifacts))) {
						if (raven.getSystemArtifactSet().contains(desc.getId())
								&& !raven.getSystemArtifactSet().contains(dep)) {
							// Skip it
						} else {
							if (artifacts.keySet().contains(dep)) {
								if (edgeValid(convergeSystemArtifacts, desc
										.getId(), dep)) {
									sb.append("  " + buildId(desc.getId())
											+ "->" + buildId(dep)
											+ " [style=dotted];\n");
								}
							}
						}
					}
				}
			}
		}
		return sb.toString();
	}

	private boolean edgeValid(boolean convergeSystemArtifacts,
			ArtifactIdentifier source, ArtifactIdentifier dep) {
		if (!convergeSystemArtifacts) {
			return true;
		}
		boolean systemSource = raven.getSystemArtifactSet().contains(source);
		boolean systemDep = raven.getSystemArtifactSet().contains(dep);
		return (!systemSource || (systemSource && systemDep));
	}

	/**
	 * Calculate the set of transitive dependencies for a given artifact, using
	 * only a certain set of other artifacts (this allows for exclusions which
	 * are specified elsewhere)
	 * 
	 * @param id
	 *            the artifact for which dependencies should be calculated
	 * @param scope
	 *            a set of artifacts that can be used as dependencies *
	 * @param convergeSystemArtifacts
	 *            set to true to force any dependencies on an artifact with a
	 *            corresponding system artifact to use the system artifact
	 *            instead
	 * @return a set of artifact identifiers representing the mandatory
	 *         transitive dependencies on the specified artifact within the
	 *         specified scope
	 */
	@SuppressWarnings("unused")
	private Set<ArtifactIdentifier> transitiveDepsFor(ArtifactIdentifier id,
			Set<ArtifactIdentifier> scope, boolean convergeSystemArtifacts) {
		return transitiveDepsForInner(id,
				new HashSet<ArtifactIdentifier>(scope),
				convergeSystemArtifacts, new HashSet<ArtifactIdentifier>());
	}

	private Set<ArtifactIdentifier> transitiveDepsForInner(
			ArtifactIdentifier id, Set<ArtifactIdentifier> scope,
			boolean convergeSystemArtifacts, Set<ArtifactIdentifier> alreadySeen) {
		Set<ArtifactIdentifier> result = new HashSet<ArtifactIdentifier>();
		id = convergeId(id, convergeSystemArtifacts);
		for (ArtifactIdentifier dep : raven.getPomParser().getDescription(id,
				raven.getDefaultRepositoryList()).getMandatoryDependencies()) {
			dep = convergeId(dep, convergeSystemArtifacts);
			if (convergeSystemArtifacts) {
				boolean systemSource = raven.getSystemArtifactSet()
						.contains(id);
				boolean systemDep = raven.getSystemArtifactSet().contains(dep);
				if (!systemSource || (systemSource && systemDep)) {
					//
				} else {
					break;
				}
			}
			if (scope.contains(dep) && !alreadySeen.contains(dep)) {
				result.add(dep);
				alreadySeen.add(dep);
				result.addAll(transitiveDepsForInner(dep, scope,
						convergeSystemArtifacts,
						new HashSet<ArtifactIdentifier>(alreadySeen)));
			}
		}
		return result;
	}

	/**
	 * Generate a node identifier for the given artifact identifier to comply
	 * with the dot language restrictions on node names
	 * 
	 * @param id
	 *            an artifact identifier to transform
	 * @return a string containing a valid identifier for a node representing
	 *         the supplied artifact
	 */
	private static String buildId(ArtifactIdentifier id) {
		return (id.getArtifactId() + "_" + id.getVersion()).replaceAll("\\.",
				"_").replaceAll("-", "_");
	}

	private int colourIndex = 0;

	private String nextNodeColour() {
		synchronized (nodeColours) {
			return nodeColours[colourIndex++ % nodeColours.length];
		}
	}

	private ArtifactIdentifier convergeId(ArtifactIdentifier dependency,
			boolean converge) {
		if (converge) {
			for (ArtifactIdentifier systemArtifact : raven
					.getSystemArtifactSet()) {
				if (systemArtifact.equalsIgnoreVersion(dependency)) {
					return systemArtifact;
				}
			}
		}
		return dependency;
	}

	private final class ArtifactGroup {

		List<ArtifactGroup> childGroups;
		List<ArtifactIdentifier> members;
		String groupId;

		private ArtifactGroup(String id) {
			this.groupId = id;
			this.members = new ArrayList<ArtifactIdentifier>();
			this.childGroups = new ArrayList<ArtifactGroup>();
		}

		private String toString(int indent, boolean showClusters,
				boolean minimalClusters, boolean perGroupColours,
				boolean convergeSystemArtifacts, Set<ArtifactIdentifier> roots) {
			StringBuffer sb = new StringBuffer();
			// If the group is empty other than a single child just delegate
			// immediately to the child
			if (members.isEmpty() && childGroups.size() == 1) {
				sb.append(childGroups.get(0).toString(indent, showClusters,
						minimalClusters, perGroupColours,
						convergeSystemArtifacts, roots));
			} else {

				String indentString;
				if (indent < 0) {
					indentString = "";
				} else {
					char[] indentArray = new char[(indent + 1) * 4];
					for (int i = 0; i < indentArray.length; i++) {
						indentArray[i] = ' ';
					}
					indentString = new String(indentArray);
				}
				if (!groupId.equals("")
						&& (minimalClusters || !members.isEmpty())
						&& showClusters) {
					sb.append(indentString
							+ "subgraph cluster_"
							+ groupId.replaceAll("\\.", "_").replaceAll("-",
									"_") + "{\n");
					sb.append(indentString + "  label=\"" + groupId + "\";\n");

					sb.append(indentString + "  color=\"" +

					colours[(indent + 100) % colours.length] + "\";\n");
					sb.append(indentString + "  style=\"filled\";\n");

				}
				String colour = defaultNodeColour;
				if (!members.isEmpty() && perGroupColours) {
					colour = nextNodeColour();
				}
				for (ArtifactIdentifier member : members) {
					if (!ignoredArtifacts.contains(member)) {
						sb.append(indentString + "  \"" + buildId(member)
								+ "\" [label=\"" + member.getArtifactId()
								+ "\\n" + member.getVersion() + "\", color=\""
								+ colour + "\"");
						if (raven.getSystemArtifactSet().contains(member)
								&& convergeSystemArtifacts) {
							sb.append(" fillcolor=\"white\" peripheries=1");
						}
						if (roots.contains(member)) {
							sb.append(" shape=\"rectangle\"");
						}
						sb.append("];\n");
					}
				}
				for (ArtifactGroup child : childGroups) {
					sb.append(child.toString(indent + 1, showClusters,
							minimalClusters, perGroupColours,
							convergeSystemArtifacts, roots));
				}
				if (!groupId.equals("")
						&& (minimalClusters || !members.isEmpty())
						&& showClusters) {
					sb.append(indentString + "}\n");
				}
			}
			return sb.toString();
		}

		private void buildGroups(Set<ArtifactIdentifier> artifacts) {
			// Find members
			for (ArtifactIdentifier id : artifacts) {
				if (id.getGroupId().equals(groupId)) {
					this.members.add(id);
				}
			}
			// Find immediate sub-groups
			Set<String> subGroups = new HashSet<String>();
			int currentDepth = groupId.split("\\.").length;
			if (groupId.equals("")) {
				currentDepth = 0;
			}
			for (ArtifactIdentifier id : artifacts) {
				String[] splitId = id.getGroupId().split("\\.");
				// Find unique immediate child groups
				if (id.getGroupId().startsWith(groupId)
						&& splitId.length > currentDepth) {
					if (groupId.equals("")) {
						subGroups.add(splitId[0]);

					} else {
						subGroups.add(groupId + "." + splitId[currentDepth]);
					}
				}
			}
			for (String subGroupId : subGroups) {
				ArtifactGroup child = new ArtifactGroup(subGroupId);
				childGroups.add(child);
				child.buildGroups(artifacts);
			}
		}
	}

	class Graph {

		private Set<Edge> edges;

		public Graph(Map<ArtifactIdentifier, ArtifactDescription> artifacts,
				boolean converge) {
			edges = new HashSet<Edge>();
			for (ArtifactIdentifier from : artifacts.keySet()) {
				ArtifactDescription ad = artifacts.get(from);
				for (ArtifactIdentifier to : ad.getMandatoryDependencies()) {
					to = convergeId(to, converge);
					if (artifacts.keySet().contains(to)) {
						boolean systemFrom = raven.getSystemArtifactSet()
								.contains(from);
						boolean systemTo = raven.getSystemArtifactSet()
								.contains(to);
						if (!systemFrom || (systemFrom && systemTo)
								|| !converge) {
							Edge e = new Edge(from, to);
							edges.add(e);
						}
					}
				}
			}
			boolean done = false;
			while (!done) {
				done = true;
				Edge edgeToRemove = null;
				for (Edge e : edges) {
					if (indirectlyReachable(e.to, e.from)) {
						edgeToRemove = e;
						done = false;
						break;
					}
				}
				if (edgeToRemove != null) {
					edges.remove(edgeToRemove);
				}
			}
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (Edge e : edges) {
				sb.append(e.toString());
			}
			return sb.toString();
		}

		private boolean indirectlyReachable(ArtifactIdentifier to,
				ArtifactIdentifier from) {
			Set<ArtifactIdentifier> visited = new HashSet<ArtifactIdentifier>();
			Set<ArtifactIdentifier> edgeNodes = new HashSet<ArtifactIdentifier>();
			Set<ArtifactIdentifier> newNodes = new HashSet<ArtifactIdentifier>();
			edgeNodes.add(from);
			boolean finished = false;
			while (!finished) {
				finished = true;
				// Each iteration take every edge node and expand it
				for (ArtifactIdentifier a : edgeNodes) {
					for (Edge e : edges) {
						if (e.to.equals(to) && e.from.equals(from)) {
							// Ignore this edge
						} else {
							if (e.to.equals(to) && e.from.equals(a)) {
								return true;
							} else {
								if (e.from.equals(a) && !visited.contains(e.to)
										&& !edgeNodes.contains(e.to)
										&& !newNodes.contains(e.to)) {
									newNodes.add(e.to);
									finished = false;
								}
							}
						}
					}
				}
				if (!finished) {
					visited.addAll(edgeNodes);
					edgeNodes.clear();
					edgeNodes.addAll(newNodes);
					newNodes.clear();
				}
			}
			return false;
		}

		class Edge {
			public Edge(ArtifactIdentifier from, ArtifactIdentifier to) {
				this.from = from;
				this.to = to;
			}

			public String toString() {
				return buildId(from) + "->" + buildId(to) + ";\n";
			}

			ArtifactIdentifier from;
			ArtifactIdentifier to;

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof Edge) {
					Edge other = (Edge) obj;
					return (other.to.equals(to) && other.from.equals(from));
				}
				return false;
			}

			public int hashCode() {
				return (to.hashCode() + from.hashCode());
			}
		}

	}

}
