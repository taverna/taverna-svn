package net.sf.taverna.raven.spi;

import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;

/**
 * Filter a set of Artifact objects according to some criteria
 * @author Tom Oinn
 */
public interface ArtifactFilter {

	/**
	 * Given a set of Artifacts returns a subset according to
	 * the filtering criteria defined by implementations of this
	 * interface.
	 * @param artifacts List of Artifacts to filter
	 * @return filtered subset of the input
	 */
	public abstract Set<Artifact> filter(Set<Artifact> artifacts);

	public abstract void addArtifactFilterListener(ArtifactFilterListener listener);

	public abstract void removeArtifactFilterListener(ArtifactFilterListener listener);
	
}
