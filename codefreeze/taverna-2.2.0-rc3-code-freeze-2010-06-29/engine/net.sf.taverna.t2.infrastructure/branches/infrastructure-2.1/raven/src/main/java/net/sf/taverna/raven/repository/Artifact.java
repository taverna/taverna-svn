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
 * A single code artifact (jar) within a maven2 repository either remote or
 * local. The status of the artifact is not held in this interface as it is only
 * meaningful in relation to a Repository.
 * <p>
 * Normally you would subclass {@link BasicArtifact} instead of implementing
 * this interface, as Artifact implementors also must be Comparable,
 * implementing {@link Comparable#compareTo(Object)}compareTo and overriding
 * {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * <p>
 * 
 * @see BasicArtifact
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("unchecked")
public interface Artifact extends Comparable {

	/**
	 * Compare with another object. Two Artifact instanceses are considered
	 * equal if they are both Artifact instances, and their
	 * {@link #getArtifactId()}, {@link #getGroupId()} and
	 * {@link #getVersion()} all equals.
	 * 
	 * @param other
	 *            Object to compare
	 * @return true if other is an Artifact and the artifactId, groupId and
	 *         version equals
	 */
	public boolean equals(Object other);

	/**
	 * Get the artifact ID for this Artifact
	 * 
	 * @return The artifactId
	 */
	public String getArtifactId();

	/**
	 * Get the group ID for this Artifact
	 * 
	 * @return The groupId
	 */
	public String getGroupId();

	/**
	 * Get the version for this Artifact, the version is not constrained to any
	 * particular format but is generally a period separated list of integers
	 * under lexicographical ordering.
	 * 
	 * @return The version
	 */
	public String getVersion();

	/**
	 * The hashCode should be calculated as the hash of the concatination of the
	 * artifactId, groupId and version, as in {@link BasicArtifact#hashCode()}.
	 * 
	 * @return Hash of the concatination of the artifactId, groupId and version
	 */
	public int hashCode();

}
