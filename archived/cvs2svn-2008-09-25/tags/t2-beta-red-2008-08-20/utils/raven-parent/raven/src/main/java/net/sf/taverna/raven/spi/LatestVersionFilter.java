package net.sf.taverna.raven.spi;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.taverna.raven.repository.Artifact;

/**
 * Filters the set of artifacts, removing all but the latest version of each
 * groupId,artifactId pair. Version numbers are sorted as in VersionComparator.
 * 
 * @see VersionComparator
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public class LatestVersionFilter extends AbstractArtifactFilter {

	public Set<Artifact> filter(Set<Artifact> artifacts) {
		SortedSet<Artifact> sortedArtifacts = new TreeSet<Artifact>(
				VersionComparator.getInstance());
		sortedArtifacts.addAll(artifacts);
		Set<Artifact> results = new HashSet<Artifact>();
		Artifact previous = null;
		for (Artifact artifact : sortedArtifacts) {
			if (previous != null
					&& (previous.getGroupId() != artifact.getGroupId() || previous
							.getArtifactId() != artifact.getArtifactId())) {
				// Different artifact than before, so latest
				// is the latest version of the previous artifact
				results.add(previous);
			}
			previous = artifact;
		}
		// And add the last artifact (unless our set was empty)
		if (previous != null) {
			results.add(previous);
		}
		return results;
	}

}
