package net.sf.taverna.t2.platform.pom;

/**
 * Identifier for a Maven 2 artifact
 * 
 * @author Tom Oinn
 */
public final class ArtifactIdentifier implements Comparable<ArtifactIdentifier> {

	private String groupId, artifactId, version;

	/**
	 * Create a new artifact identifier, two identifiers are considered equal if
	 * all fields on both artifact identifiers match.
	 * 
	 * @param groupId
	 *            the group ID for this artifact identifier
	 * @param artifactId
	 *            the artifact ID for this artifact identifier
	 * @param version
	 *            the version for this artifact identifier
	 * @throws RuntimeException
	 *             if any property is null
	 */
	public ArtifactIdentifier(String groupId, String artifactId, String version) {
		if (groupId == null) {
			throw new RuntimeException(
					"Cannot create an artifact identifier with a null groupId");
		}
		if (artifactId == null) {
			throw new RuntimeException(
					"Cannot create an artifact identifier with a null artifactId");
		}
		if (version == null) {
			throw new RuntimeException(
					"Cannot create an artifact identifier with a null version");
		}
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	/**
	 * Create a new artifact identifier from a single colon separated string of
	 * the form <code>groupId:artifactId:version</code>
	 * 
	 * @throws RuntimeException
	 *             if the specification string is null, or does not match the
	 *             pattern above
	 */
	public ArtifactIdentifier(String compactSpecification) {
		compactSpecification = compactSpecification.trim();
		if (compactSpecification == null) {
			throw new RuntimeException(
					"Cannot create an artifact identifier from a null compact specification");
		}
		String[] parts = compactSpecification.split(":");
		if (parts.length != 3) {
			throw new RuntimeException(
					"Cannot create an artifact identifier from invalid compact specification '"
							+ compactSpecification + "'");
		}
		this.groupId = parts[0];
		this.artifactId = parts[1];
		this.version = parts[2];
	}

	/**
	 * Construct a prototype artifact identifier, used by bean based editor
	 * builders
	 */
	public ArtifactIdentifier() {
		this("groupID:artifactID:version");
	}

	/**
	 * @return the group ID part of this artifact identifer
	 */
	public final String getGroupId() {
		return groupId;
	}

	/**
	 * @return the artifact ID part of this artifact identifier
	 */
	public final String getArtifactId() {
		return artifactId;
	}

	/**
	 * Versions in maven 2 have no formal syntax and are unconstrained strings,
	 * the exception to this is that versions ending in 'snapshot' (normally
	 * upper case) are interpreted as snapshot versions and may be handled
	 * differently by the download and linkage process.
	 * 
	 * @return the version part of this artifact identifier.
	 */
	public final String getVersion() {
		return version;
	}

	/**
	 * An artifact identifier specified a snapshot if its version ends with the
	 * literal string 'snapshot' ignoring case.
	 * 
	 * @return whether the identifier points to a shapshot version
	 */
	public boolean isSnapshot() {
		return version.toLowerCase().endsWith("snapshot");
	}

	/**
	 * Two identifiers are equal if both are instances of ArtifactIdentifier,
	 * neither are null and all of artifactId, groupId and version fields match
	 * under string comparison including by case.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof ArtifactIdentifier) {
			ArtifactIdentifier ai = (ArtifactIdentifier) other;
			return (ai.getArtifactId().equals(artifactId)
					&& ai.getGroupId().equals(groupId) && ai.getVersion()
					.equals(version));
		} else {
			return false;
		}
	}

	/**
	 * Compares two artifact identifiers ignoring the version property
	 * 
	 * @return true if the other identifier is not null and matches the groupId
	 *         and artifactId properties of this artifact identifier
	 */
	public boolean equalsIgnoreVersion(ArtifactIdentifier other) {
		if (other == null) {
			return false;
		}
		return (other.getGroupId().equals(groupId) && other.getArtifactId()
				.equals(artifactId));
	}

	/**
	 * Returns the compact form of this artifact identifier as a single string
	 * of the form <code>groupId:artifactId:version</code>
	 */
	@Override
	public String toString() {
		return groupId + ":" + artifactId + ":" + version;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public int compareTo(ArtifactIdentifier o) {
		if (groupId.compareTo(o.getGroupId()) != 0) {
			return groupId.compareTo(o.getGroupId());
		} else if (artifactId.compareTo(o.getArtifactId()) != 0) {
			return artifactId.compareTo(o.getArtifactId());
		} else if (version.compareTo(o.getVersion()) != 0) {
			return version.compareTo(o.getVersion());
		}
		return 0;
	}

}
