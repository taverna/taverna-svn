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
		if (! (other instanceof Artifact)) {
			return false;
		}
		Artifact otherDep = (Artifact)other;
		return (otherDep.getArtifactId().equals(getArtifactId()) &&
					otherDep.getGroupId().equals(getGroupId()) &&
					otherDep.getVersion().equals(getVersion()));
	}

	/** 
	 * @return hashcode of the concatenation of the three
	 * string fields
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (getArtifactId()+getGroupId()+getVersion()).hashCode();
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
		return getGroupId()+":"+getArtifactId()+":"+getVersion();
	}
	
	public int compareTo(Object arg0) {
		if (arg0 instanceof Artifact) {
			Artifact other = (Artifact)arg0;
			int groupIdComp = getGroupId().compareTo(other.getGroupId());
			if (groupIdComp == 0) {
				int artifactIdComp = getArtifactId().compareTo(other.getArtifactId());
				if (artifactIdComp == 0) {
					return getVersion().compareTo(other.getVersion());
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
