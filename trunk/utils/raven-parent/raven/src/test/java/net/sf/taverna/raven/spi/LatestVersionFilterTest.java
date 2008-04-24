package net.sf.taverna.raven.spi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;

public class LatestVersionFilterTest extends TestCase {

	public void testLatestVersionFilter() {
		LatestVersionFilter filter = new LatestVersionFilter();
		List<Artifact> artifacts = new ArrayList<Artifact>();
		artifacts.add(new BasicArtifact("second", "artifact", "1.4.0")); // 0
		artifacts.add(new BasicArtifact("second", "third", "1.3.a")); // 1
		artifacts.add(new BasicArtifact("second", "third", "1.3.9")); // 2
		artifacts.add(new BasicArtifact("second", "artifact", "1.4")); // 3
		artifacts.add(new BasicArtifact("first", "artifact", "1.4.0")); // 4
		artifacts.add(new BasicArtifact("first", "artifact", "1.5")); // 5
		artifacts.add(new BasicArtifact("first", "artifact", "1.4.1")); // 6
		artifacts.add(new BasicArtifact("first", "artifact", "1.0")); // 7

		Set<Artifact> artifactSet = new HashSet<Artifact>(artifacts);
		assertEquals(8, artifactSet.size());
		Set<Artifact> filtered = filter.filter(artifactSet);
		assertEquals(3, filtered.size()); // One version pr artifact
		assertEquals(8, artifacts.size()); // Did not touch

		// Should only contain these three versions
		assertTrue(filtered.contains(artifacts.get(5))); // 1.5
		assertTrue(filtered.contains(artifacts.get(0))); // 1.4.0
		assertTrue(filtered.contains(artifacts.get(1))); // 1.3.a
		// And nothing else
		// (Yes, this is also asserted by filtered.size(),
		// but we test that filtered.contains() is not broken)
		assertFalse(filtered.contains(artifacts.get(4)));
	}

}
