/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.raven.repository;

/**
 * Simple base artifact class, a bean with three strings for groupId, artifactId
 * and version. Artifacts are considered as equal if all three strings match.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
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
		if (groupId == null || artifactId == null || version == null) {
			throw new NullPointerException();
		}
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
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
			int artifactIdComp = getArtifactId().compareTo(
					artifact.getArtifactId());
			if (artifactIdComp == 0) {
				return getVersion().compareTo(artifact.getVersion());
			} else {
				return artifactIdComp;
			}
		}
		throw new ClassCastException("Cannot compare Artifact to other type");
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
		return (getArtifactId().equals(otherDep.getArtifactId())
				&& getGroupId().equals(otherDep.getGroupId()) && getVersion()
				.equals(otherDep.getVersion()));
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

	@Override
	public String toString() {
		return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
	}

}
