package net.sf.taverna.t2.platform.raven.ui;

import java.util.Set;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;

/**
 * Utility to build a visualisation of the dependency hierarchy of a set of
 * artifact identifiers, rendering this as a string that can be used in
 * conjunction with the GraphViz toolkit to generate a visual representation of
 * the dependencies.
 * 
 * @author Tom Oinn
 * 
 */
public interface DependencyGraphBuilder {

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
	 * @param excludedArtifacts
	 *            a set of artifacts which should act as 'stop' conditions in
	 *            the dependency graph and which will not be included in the
	 *            result. These may be mandatory dependencies that are removed
	 *            from the visualisation to produce a cleaner layout,
	 *            particularly the case for logging and similar APIs where
	 *            almost all artifacts will have a dependency on them. Use null
	 *            to use the default set of excluded artifacts defined in the
	 *            application context for this bean, or an empty set to
	 *            explicitly assert that there should be no exclusions
	 * @return a dot format string which can be rendered to show dependency
	 *         information
	 */
	String getDependencyGraph(Set<ArtifactIdentifier> artifacts,
			boolean perNodeColouring, boolean showGroupsAsCluster,
			boolean compactClusters, boolean removeTransitiveDependencies,
			boolean showOptionalDependencies, boolean convergeSystemArtifacts,
			Set<ArtifactIdentifier> excludedArtifacts);

}
