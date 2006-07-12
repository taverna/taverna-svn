package net.sf.taverna.raven.spi;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;

/**
 * Filters the set of artifacts, removing all but the latest
 * version of each groupId,artifactId pair. Version numbers
 * are treated as being integers under lexicographic ordering
 * with the '.' character as separator. Presence of a token is
 * assumed to be an indication of a later version when compared
 * to absence, i.e. 1.3 &lt; 1.3.1. Where tokens are not
 * parsable as integer numbers String ordering is applied to both
 * tokens.
 * @author Tom Oinn
 */
public class LatestVersionFilter implements ArtifactFilter {

	public Set<Artifact> filter(Set<Artifact> artifacts) {
		Set<Artifact> results = new HashSet<Artifact>();
		Set<Artifact> work = new HashSet<Artifact>(artifacts);
		while (work.isEmpty() == false) {
			Artifact test = work.iterator().next();
			work.remove(test);
			Set<Artifact> toRemove = new HashSet<Artifact>();
			for (Artifact a : work) {
				if (test.getGroupId().equals(a.getGroupId()) && 
						test.getArtifactId().equals(a.getArtifactId())) {
					toRemove.add(a);
					test = latest(test, a);
				}
			}
			results.add(test);
			work.removeAll(toRemove);
		}
		return results;
	}

	Artifact latest(Artifact a, Artifact b) {
		return lessThan(a.getVersion(), b.getVersion())?b:a;
	}
	
	boolean lessThan(String a, String b) {
		String[] va = a.split("\\.");
		String[] vb = b.split("\\.");
		for (int i = 0; i < va.length || i < vb.length ; i++) {
			if (i == va.length) {
				return true;
			}
			else if (i == vb.length) {
				return false;
			}
			String ca = va[i];
			String cb = vb[i];
			
			try {
				int ia = Integer.parseInt(ca);
				int ib = Integer.parseInt(cb);
				if (ia < ib) {
					return true;
				}
				else if (ia > ib) {
					return false;
				}
			}
			catch (NumberFormatException nfe) {
				if (ca.compareTo(cb) > 0) {
					return false;
				}
				else if (ca.compareTo(cb) < 0) {
					return true;
				}
			}
		}
		return false;
	}
	
}
