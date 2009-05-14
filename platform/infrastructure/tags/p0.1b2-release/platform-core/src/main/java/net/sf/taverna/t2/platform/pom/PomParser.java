package net.sf.taverna.t2.platform.pom;

import java.net.URL;
import java.util.List;

/**
 * Used to build an ArtifactDescription from an ArtifactIdentifier and a list of
 * maven 2 repository locations.
 * 
 * @author Tom Oinn
 */
public interface PomParser {

	/**
	 * Obtain a populated ArtifactDescription for the specified
	 * ArtifactIdentifier. This may return a cached copy of the description or
	 * may use the specified repositories to fetch and parse pom.xml files,
	 * aggregating any parent files into a single description.
	 * 
	 * @param id
	 *            the artifact you're trying to get a description of specified
	 *            as an ArtifactIdentifier (which is really just a triple of
	 *            groupId, artifactId and version properties)
	 * @param repositories
	 *            a list of Maven 2 repository locations to be used in order
	 *            when fetching pom files. URLs must use either 'file' or
	 *            'http[s]' protocols. If a file URL is specified the repository
	 *            is interpreted as a Maven 2 repository structure accessible on
	 *            the local filesystem, otherwise as one one a remote HTTP
	 *            server. The difference is that in the latter case the download
	 *            manager is used to retrieve the pom files.
	 * @return a populated artifact description derived from the referenced pom
	 *         file and any direct or transitive parent poms
	 * @throws ArtifactParseException
	 *             if any problems occur either fetching or parsing the pom
	 *             files
	 */
	public ArtifactDescription getDescription(ArtifactIdentifier id,
			List<URL> repositories) throws ArtifactParseException;

}
