package net.sf.taverna.raven.spi;

import junit.framework.TestCase;

public class ProfileTest extends TestCase {

	/*
	 * Test method for 'net.sf.taverna.raven.spi.Profile.Profile(InputStream, boolean)'
	 */
	public void testProfile() {
		try {
			new Profile(getClass().getResourceAsStream("goodProfile.xml"),true);
		} catch (InvalidProfileException e) {
			e.printStackTrace();
			fail();
		}
		try {
			new Profile(getClass().getResourceAsStream("badProfile1.xml"),true);
			fail();
		}
		catch (InvalidProfileException e) {
			//	
		}
		try {
			new Profile(getClass().getResourceAsStream("badProfile2.xml"),true);
			fail();
		}
		catch (InvalidProfileException e) {
			//	
		}
	}

	/*
	 * Test method for 'net.sf.taverna.raven.spi.Profile.getArtifacts()'
	 */
	public void testGetArtifacts() {

	}

	/*
	 * Test method for 'net.sf.taverna.raven.spi.Profile.filter(Set<Artifact>)'
	 */
	public void testFilter() {

	}

}
