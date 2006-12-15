package net.sf.taverna.raven.repository;

/**
 * A single code artifact (jar) within a maven2 repository
 * either remote or local. The status of the artifact is not
 * held in this interface as it is only meaningful in relation
 * to a Repository.
 * @author Tom
 *
 */
public interface Artifact {

	/**
	 * Get the artifact ID for this Artifact
	 * @return Returns the artifactId.
	 */
	public abstract String getArtifactId();

	/**
	 * Get the group ID for this Artifact
	 * @return Returns the groupId.
	 */
	public abstract String getGroupId();

	/**
	 * Get the version for this Artifact, the version
	 * is not constrained to any particular format but
	 * is generally a period separated list of integers
	 * under lexicographical ordering.
	 * @return Returns the version.
	 */
	public abstract String getVersion();

}