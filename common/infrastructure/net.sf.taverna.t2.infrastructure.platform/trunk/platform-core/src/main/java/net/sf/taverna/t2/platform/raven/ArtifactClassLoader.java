package net.sf.taverna.t2.platform.raven;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;

/**
 * Simple interface which can be implemented by class loaders bound to artifact
 * identifiers. Note that due to the existance of system artifacts there is no
 * guarantee that any given artifact is associated with an artifact class
 * loader, you must always check before casting the loaders returne from Raven
 * instances to this interface type, and in general you shouldn't rely on this
 * metadata being available in all cases.
 * 
 * @author Tom Oinn
 * 
 */
public interface ArtifactClassLoader {

	/**
	 * Return the artifact identifier to which this class loader is bound
	 */
	public ArtifactIdentifier getArtifactIdentifier();

}
