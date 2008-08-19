package net.sf.taverna.raven.spi;

import java.util.Set;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.Artifact;

public class ProfileTest extends TestCase {

	public void testBadProfile1() {
		try {
			new Profile(getClass().getResourceAsStream("badProfile1.xml"), true);
			fail("Did not fail on badProfile1.xml");
		} catch (InvalidProfileException e) {
			// expected
		}
	}

	public void testBadProfile2() {
		try {
			new Profile(getClass().getResourceAsStream("badProfile2.xml"), true);
			fail("Did not fail on badProfile2.xml");
		} catch (InvalidProfileException e) {
			// expected
		}
	}

	/*
	 * Test method for 'net.sf.taverna.raven.spi.Profile.filter(Set<Artifact>)'
	 */
	public void testFilter() throws InvalidProfileException {
		Profile p1 = new Profile(getClass().getResourceAsStream(
				"goodProfile.xml"), true);
		Profile p2 = new Profile(getClass().getResourceAsStream(
				"versionedProfile.xml"), true);
		Set<Artifact> p1Artifacts = p1.getArtifacts();
		Set<Artifact> p2Artifacts = p2.getArtifacts();
		assertEquals(2, p1Artifacts.size());
		assertEquals(3, p2Artifacts.size());
		// Should remove artifact2 as they have different versions
		Set<Artifact> intersection = p1.filter(p2Artifacts);
		// And not include unknown artifact3
		assertEquals(1, intersection.size());
		// But didn't touch it's own set
		assertEquals(2, p1.getArtifacts().size());
		// Or the other set
		assertEquals(3, p2Artifacts.size());
		// The remaining element should then be artifact1
		assertEquals("artifact1", intersection.iterator().next()
				.getArtifactId());
		// And of course it should be an equivalent set the other way around
		assertEquals(intersection, p2.filter(p1Artifacts));
		// And against its own set it should not remove anything
		assertEquals(p2Artifacts, p2.filter(p2Artifacts));
	}

	public void testFilterNonStrict() throws InvalidProfileException {
		// Test the not-so-obvious non-strict version of filter()

		Profile p1 = new Profile(getClass().getResourceAsStream(
				"goodProfile.xml"), false);
		Profile p2 = new Profile(getClass().getResourceAsStream(
				"versionedProfile.xml"), true);
		Set<Artifact> p1Artifacts = p1.getArtifacts();
		assertEquals(2, p1Artifacts.size());
		// The non-strict p1 should
		// include the unknown artifact3 in p1Artifacts
		Set<Artifact> filtered = p1.filter(p2.getArtifacts());
		assertEquals(2, filtered.size());
		// but should not include artifact2 (WHY NOT? Well, this is
		// what our javadoc says it should not, even though this is
		// probably not very useful behaviour)
		boolean found1 = false;
		boolean found3 = false;
		for (Artifact artifact : filtered) {
			if (artifact.getGroupId().equals("group1")) {
				assertFalse("Found two group1 artifacts", found1);
				found1 = true;
				assertEquals("artifact1", artifact.getArtifactId());
				assertEquals("1.0.0", artifact.getVersion());
			} else {
				assertEquals("group3", artifact.getGroupId());
				assertFalse("Found two group3 artifacts", found3);
				found3 = true;
				assertEquals("artifact3", artifact.getArtifactId());
				assertEquals("3.11", artifact.getVersion());
			}
		}
		assertTrue("Did not find artifact1", found1);
		assertTrue("Did not find artifact3", found3);
	}

	/*
	 * Test method for 'net.sf.taverna.raven.spi.Profile.getArtifacts()'
	 */
	public void testGetArtifacts() throws InvalidProfileException {
		Profile p = new Profile(getClass().getResourceAsStream(
				"goodProfile.xml"), true);
		Set<Artifact> artifacts = p.getArtifacts();
		assertEquals(2, artifacts.size());
		boolean found1 = false;
		boolean found2 = false;
		for (Artifact artifact : artifacts) {
			if (artifact.getGroupId().equals("group1")) {
				assertFalse("Found two group1 artifacts", found1);
				found1 = true;
				assertEquals("artifact1", artifact.getArtifactId());
				assertEquals("1.0.0", artifact.getVersion());
			} else {
				assertEquals("group2", artifact.getGroupId());
				assertFalse("Found two group2 artifacts", found2);
				found2 = true;
				assertEquals("artifact2", artifact.getArtifactId());
				assertEquals("1.0.2", artifact.getVersion());
			}
		}
		assertTrue("Did not find artifact1", found1);
		assertTrue("Did not find artifact2", found2);
	}

	/*
	 * Test method for 'net.sf.taverna.raven.spi.Profile.Profile(InputStream,
	 * boolean)'
	 */
	public void testGoodProfile() throws InvalidProfileException {
		Profile p = new Profile(getClass().getResourceAsStream(
				"goodProfile.xml"), true);
		assertEquals("NO VERSION", p.getVersion());
	}

	public void testVersionedProfile() throws InvalidProfileException {
		Profile p = new Profile(getClass().getResourceAsStream(
				"versionedProfile.xml"), true);
		assertEquals("0.1", p.getVersion());
	}
}
