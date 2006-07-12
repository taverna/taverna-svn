package net.sf.taverna.raven.spi;

import junit.framework.TestCase;

public class LatestVersionFilterTest extends TestCase {

	/*
	 * Test method for 'net.sf.taverna.raven.spi.LatestVersionFilter.lessThan(String, String)'
	 */
	public void testLessThan() {
		LatestVersionFilter lvf = new LatestVersionFilter();
		assertTrue(lvf.lessThan("1.3","1.3.1"));
		assertFalse(lvf.lessThan("1.3","1.3"));
		assertTrue(lvf.lessThan("1.3.2.a","1.3.2.b"));
	}

}
