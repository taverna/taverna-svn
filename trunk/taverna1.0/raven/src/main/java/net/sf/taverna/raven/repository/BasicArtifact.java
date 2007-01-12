package net.sf.taverna.raven.repository;

/**
 * Simple base artifact class, a bean with three strings for groupId, artifactId
 * and version. Artifacts are considered as equal if all three strings match.
 * 
 * @author Tom Oinn
 * @author Stian Soiland
 */
public class BasicArtifact implements Artifact {

	protected String groupId;

	protected String artifactId;

	protected String version;

	/**
	 * Construct a BasicArtifact as a copy of another artifact
	 * 
	 * @param other
	 *            Artifact whose group/artifact/version values to copy
	 */
	public BasicArtifact(Artifact other) {
		this(other.getGroupId(), other.getArtifactId(), other.getVersion());
	}

	/**
	 * Construct a BasicArtifact from the given values
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 */
	public BasicArtifact(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	/**
	 * Compare with another object.
	 * 
	 * @return true if the other object is also an instance of Artifact and all
	 *         fields (artifactId, groupId and version) are equal according to
	 *         string comparison
	 * @see Artifact#equals(Object)
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof Artifact)) {
			return false;
		}
		Artifact otherDep = (Artifact) other;
		return (otherDep.getArtifactId().equals(getArtifactId())
			&& otherDep.getGroupId().equals(getGroupId()) 
			&& otherDep.getVersion().equals(getVersion()));
	}

	/**
	 * Calculate the hash code of the artifact.
	 * 
	 * @return hashcode of the concatenation of the three string fields
	 * @see Artifact#hashCode()
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (getArtifactId() + getGroupId() + getVersion()).hashCode();
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
	}

	/**
	 * Compare with another object. If the other instance is an Artifact, it
	 * will be compared by group, artifact and version in that order.
	 */
	public int compareTo(Object other) {
		if (super.equals(other)) {
			return 0;
		}
		if (other instanceof Artifact) {
			Artifact artifact = (Artifact) other;
			int groupIdComp = getGroupId().compareTo(artifact.getGroupId());
			if (groupIdComp != 0) {
				return groupIdComp;
			}
			int artifactIdComp =
				getArtifactId().compareTo(artifact.getArtifactId());
			if (artifactIdComp == 0) {
				return getVersion().compareTo(artifact.getVersion());
			} else {
				return artifactIdComp;
			}
		}
		throw new ClassCastException("Cannot compare Artifact to other type");
	}

}
