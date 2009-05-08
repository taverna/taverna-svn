package net.sf.taverna.t2.platform.pom;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * A simple interface handling downloads of jar files for artifacts
 * 
 * @author Tom Oinn
 * 
 */
public interface JarManager {

	/**
	 * Fetch (if necessary) and return the file corresponding to the jar for a
	 * Maven 2 artifact
	 * 
	 * @param id
	 *            an ArtifactIdentifier defining the artifact to fetch
	 * @param repositories
	 *            a list of Maven 2 repositories which can contain the specified
	 *            artifact
	 * @return a File pointing to a downloaded and verified java code archive
	 *         for the specified artifact
	 */
	public File getArtifactJar(ArtifactIdentifier id, List<URL> repositories);

}
