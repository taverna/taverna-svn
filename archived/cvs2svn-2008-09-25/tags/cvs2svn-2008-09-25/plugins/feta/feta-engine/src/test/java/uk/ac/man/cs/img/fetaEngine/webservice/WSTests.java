package uk.ac.man.cs.img.fetaEngine.webservice;

import junit.framework.Test;
import junit.framework.TestSuite;

public class WSTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for uk.ac.man.cs.img.fetaEngine.webservice");
		// $JUnit-BEGIN$
		suite
				.addTestSuite(uk.ac.man.cs.img.fetaEngine.webservice.PublishWSTest.class);

		suite
				.addTestSuite(uk.ac.man.cs.img.fetaEngine.webservice.InquiryWSTest.class);

		// $JUnit-END$
		return suite;
	}
}
