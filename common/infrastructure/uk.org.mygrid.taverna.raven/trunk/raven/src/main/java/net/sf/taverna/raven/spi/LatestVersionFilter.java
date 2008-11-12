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
package net.sf.taverna.raven.spi;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.taverna.raven.repository.Artifact;

/**
 * Filters the set of artifacts, removing all but the latest version of each
 * groupId,artifactId pair. Version numbers are sorted as in VersionComparator.
 * 
 * @see VersionComparator
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public class LatestVersionFilter extends AbstractArtifactFilter {

	public Set<Artifact> filter(Set<Artifact> artifacts) {
		SortedSet<Artifact> sortedArtifacts = new TreeSet<Artifact>(
				VersionComparator.getInstance());
		sortedArtifacts.addAll(artifacts);
		Set<Artifact> results = new HashSet<Artifact>();
		Artifact previous = null;
		for (Artifact artifact : sortedArtifacts) {
			if (previous != null
					&& (previous.getGroupId() != artifact.getGroupId() || previous
							.getArtifactId() != artifact.getArtifactId())) {
				// Different artifact than before, so latest
				// is the latest version of the previous artifact
				results.add(previous);
			}
			previous = artifact;
		}
		// And add the last artifact (unless our set was empty)
		if (previous != null) {
			results.add(previous);
		}
		return results;
	}

}
