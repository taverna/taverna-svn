package net.sf.taverna.raven.spi;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.taverna.raven.repository.Artifact;

/**
 * Compare artifact by groupID, artifactID and version.
 * <p>
 * If two artifacts share groupID and artifactID, the version will 
 * be compared numerically, example:
 * <pre>
 * "1.2.10" > "1.2.8"
 * "1.5" > "1.4.1"
 * "1.3" < "1.3.0"
 * "1.01.2" == "1.1.2"
 * </pre>
 * 
 * @author Tom Oinn
 * @author Stian Soiland
 *
 */
class VersionComparator implements Comparator<Artifact> {

	// Singleton pattern
	private static VersionComparator instance = null;
	protected VersionComparator() {}
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
		for (int i = 0; i < va.length || i < vb.length ; i++) {
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
			}
			catch (NumberFormatException ex) {
				return ca.compareTo(cb);
			}
		}
		// No digits returned a difference, they are equal
		return 0;
	}
}