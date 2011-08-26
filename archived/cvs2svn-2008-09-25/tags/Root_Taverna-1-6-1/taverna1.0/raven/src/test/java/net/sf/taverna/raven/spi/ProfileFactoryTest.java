package net.sf.taverna.raven.spi;

import junit.framework.TestCase;

public class ProfileFactoryTest extends TestCase {

	public void testProfileDefined() {
		System.setProperty("raven.profile", "http://somewhere");
		assertTrue(ProfileFactory.getInstance().isProfileDefined());
	}
	
	public void testNoProfileDefined() {
		if (System.getProperty("raven.profile")!=null) System.getProperties().remove("raven.profile");
		assertFalse(ProfileFactory.getInstance().isProfileDefined());
	}
}
