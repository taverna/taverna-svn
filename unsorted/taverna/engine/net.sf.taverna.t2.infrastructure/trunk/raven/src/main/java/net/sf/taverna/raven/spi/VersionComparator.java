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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.taverna.raven.repository.Artifact;

/**
 * Compare artifact by groupID, artifactID and version.
 * <p>
 * If two artifacts share groupID and artifactID, the version will be compared
 * numerically, example:
 * 
 * <pre>
 * &quot;1.2.10&quot; &gt; &quot;1.2.8&quot;
 * &quot;1.5&quot; &gt; &quot;1.4.1&quot;
 * &quot;1.3&quot; &lt; &quot;1.3.0&quot;
 * &quot;1.01.2&quot; == &quot;1.1.2&quot;
 * </pre>
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
class VersionComparator implements Comparator<Artifact> {

	// Singleton pattern
	private static VersionComparator instance = null;

	public static VersionComparator getInstance() {
		if (instance == null) {
			instance = new VersionComparator();
		}
		return instance;
	}

	/**
	 * Sort the list of artifacts by groupID, artifactID and version.
	 * 
	 * @param artifacts
	 */
	public static void sort(List<Artifact> artifacts) {
		Collections.sort(artifacts, new VersionComparator());
	}

	protected VersionComparator() {
	}

	public int compare(Artifact a, Artifact b) {
		int diff = a.getGroupId().compareTo(b.getGroupId());
		if (diff != 0) {
			return diff;
		}
		diff = a.getArtifactId().compareTo(b.getArtifactId());
		if (diff != 0) {
			return diff;
		}
		// Same artifact, check version. By string first.
		if (a.getVersion().equals(b.getVersion())) {
			return 0;
		}
		// Numerically check the versions
		return compareVersions(a.getVersion(), b.getVersion());
	}

	int compareVersions(String a, String b) {
		String[] va = a.split("\\.");
		String[] vb = b.split("\\.");
		for (int i = 0; i < va.length || i < vb.length; i++) {
			if (i == va.length) {
				// vb is more specific, ie a < b
				return -1;
			} else if (i == vb.length) {
				// va is more specific, ie a > b
				return 1;
			}
			String ca = va[i];
			String cb = vb[i];
			if (ca.equals(cb)) {
				continue; // Test next digit
			}
			try {
				int ia = Integer.parseInt(ca);
				int ib = Integer.parseInt(cb);
				if (ia == ib) {
					continue; // Test next digit
				}
				return ia - ib;
			} catch (NumberFormatException ex) {
				return ca.compareTo(cb);
			}
		}
		// No digits returned a difference, they are equal
		return 0;
	}
}
