package net.sf.taverna.raven.repository;

import net.sf.taverna.raven.RavenException;

/**
 * Thrown when an attempt to locate a remote module in a repository fails for
 * some reason or when a reference is made to an Artifact that cannot be
 * resolved within the current local repository.
 * 
 * @author Tom
 * 
 */
public class ArtifactNotFoundException extends RavenException {

	private static final long serialVersionUID = 1L;

	public ArtifactNotFoundException() {
		//
	}

	public ArtifactNotFoundException(String message) {
		super(message);
	}

}
