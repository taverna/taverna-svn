package net.sf.taverna.raven.repository;

/**
 * Listener to respond to status change events within a Repository
 * 
 * @author Tom Oinn
 */
public interface RepositoryListener {

	public abstract void statusChanged(Artifact a, ArtifactStatus oldStatus,
			ArtifactStatus newStatus);

}
