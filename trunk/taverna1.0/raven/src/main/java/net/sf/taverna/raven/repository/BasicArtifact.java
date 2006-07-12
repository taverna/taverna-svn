package net.sf.taverna.raven.repository;

/**
 * Simple base artifact class, a bean with three strings for
 * groupId, artifactId and version. Artifacts are considered
 * as equal if all three strings match.
 * @author Tom Oinn
 */
public class BasicArtifact implements Artifact, Comparable {

	protected String groupId;
	protected String artifactId;
	protected String version;

	/**
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
	 * @return true if the other object is also an instance
	 * of Artifact and all fields (artifactId, groupId and version)
	 * are equal according to string comparison
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof Artifact) {
			Artifact otherDep = (Artifact)other;
			if (otherDep.getArtifactId().equals(this.artifactId) &&
					otherDep.getGroupId().equals(this.groupId) &&
					otherDep.getVersion().equals(this.version)) {
				return true;
			}
		}
		return false;
	}

	/** 
	 * @return hashcode of the concatenation of the three
	 * string fields
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (this.artifactId+this.groupId+this.version).hashCode();
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getVersion() {
		return this.version;
	}

	public int compareTo(Object arg0) {
		if (arg0 instanceof Artifact) {
			Artifact other = (Artifact)arg0;
			int groupIdComp = this.groupId.compareTo(other.getGroupId());
			if (groupIdComp == 0) {
				int artifactIdComp = this.artifactId.compareTo(other.getArtifactId());
				if (artifactIdComp == 0) {
					return this.version.compareTo(other.getVersion());
				}
				else {
					return artifactIdComp;
				}
			}
			else {
				return groupIdComp;
			}
		}
		throw new ClassCastException("Cannot compare Artifact to other type");
	}

}
