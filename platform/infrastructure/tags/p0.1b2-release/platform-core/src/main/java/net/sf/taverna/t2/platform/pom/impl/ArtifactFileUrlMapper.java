package net.sf.taverna.t2.platform.pom.impl;

import java.io.File;
import java.net.URL;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.util.download.URLMapper;

/**
 * A URLMapper for pom and jar files
 * 
 * @author Tom Oinn
 * 
 */
public class ArtifactFileUrlMapper implements URLMapper {

	/**
	 * Root location of the downloaded file cache
	 */
	private File baseLocation;

	/**
	 * Group ID to be used as the top level directory within the poms directory
	 */
	private ArtifactIdentifier artifact;

	/**
	 * Create a new Pom URL mapper with the specified base download manager
	 * location. Pom files are downloaded into a location defined by
	 * $baseLocation/artifacts/groupID/artifactID-version.pom
	 * 
	 * @param baseLocation
	 */
	public ArtifactFileUrlMapper(File baseLocation, ArtifactIdentifier artifact) {
		this.baseLocation = baseLocation;
		this.artifact = artifact;
		// Check that the appropriate sub-directory exists and create it if it's
		// not there already
		File pomDirectory = new File(baseLocation, "artifacts/"
				+ this.artifact.getGroupId());
		if (pomDirectory.exists() == false) {
			pomDirectory.mkdirs();
		}
	}

	/**
	 * @see net.sf.taverna.t2.platform.util.download.URLMapper#map(java.net.URL)
	 */
	public File map(URL source) {
		String[] parts = source.toExternalForm().split("\\.");
		String extension = parts[parts.length - 1];
		return new File(baseLocation, "artifacts/" + artifact.getGroupId()
				+ "/" + artifact.getArtifactId() + "-" + artifact.getVersion()
				+ "." + extension);
	}

}
