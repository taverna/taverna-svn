package net.sf.taverna.raven.repository;

import net.sf.taverna.raven.RavenException;

/**
 * Thrown when an operation attempt to perform on an artifact
 * that isn't in the right state relative to a particular
 * repository
 * @author Tom Oinn
 */
public class ArtifactStateException extends RavenException {

	private static final long serialVersionUID = 1L;

	private ArtifactStatus state;
	private ArtifactStatus[] validStates;
	
	public ArtifactStateException(ArtifactStatus state, ArtifactStatus[] validStates) {
		this.state = state;
		this.validStates = validStates;
	}
	
	public ArtifactStatus[] getValidStates() {
		return this.validStates;
	}
	
	public ArtifactStatus getState() {
		return this.state;
	}
	
}
