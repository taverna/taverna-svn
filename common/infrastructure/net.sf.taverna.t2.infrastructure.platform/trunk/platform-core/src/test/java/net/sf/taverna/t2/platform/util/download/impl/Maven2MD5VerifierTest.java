package net.sf.taverna.t2.platform.util.download.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.taverna.t2.platform.util.download.DownloadVerificationException;
import net.sf.taverna.t2.platform.util.download.DownloadVerifier;

import junit.framework.TestCase;

/**
 * Tests for the Maven2MD5Verifier download verifier module. These tests requires
 * HTTP access to the maven 2 repository mirror at ibiblio.org
 * 
 * @author Tom Oinn
 * 
 */
public class Maven2MD5VerifierTest extends TestCase {

	/**
	 * Check that we can verify a correct MD5 sum, in this case using a pom file
	 * from the axis project from the resources folder and its online md5 sum.
	 */
	public void testMD5Verification() throws MalformedURLException {
		URL pomSourceURL = new URL(
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/axis2/axis2-adb/1.4/axis2-adb-1.4.pom");
		File pomFile = fileFromResource("axis2-adb-1.4.pom");
		DownloadVerifier v = new Maven2MD5Verifier();
		assertTrue(v.verify(pomFile, pomSourceURL));
	}

	/**
	 * Check that the verifier returns false if it can't find the appropriate
	 * MD5 sum, in this case by deliberately giving it the wrong source URL
	 * 
	 * @throws MalformedURLException
	 */
	public void testMD5VerificationFailure1() throws MalformedURLException {
		URL pomSourceURL = new URL(
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/axis2/axis2-adb/1.4/WRONG-URL-HERE");
		File pomFile = fileFromResource("axis2-adb-1.4.pom");
		DownloadVerifier v = new Maven2MD5Verifier();
		assertFalse(v.verify(pomFile, pomSourceURL));
	}

	/**
	 * Check that the verifier fails, that is to say throws a
	 * VerificationException, when given a valid remote MD5 hash and a local
	 * file but where the hashes don't match
	 * 
	 * @throws MalformedURLException
	 */
	public void testMD5VerificationFailure2() throws MalformedURLException {
		URL pomSourceURL = new URL(
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/commons-codec/commons-codec/1.3/commons-codec-1.3.pom");
		File pomFile = fileFromResource("axis2-adb-1.4.pom");
		DownloadVerifier v = new Maven2MD5Verifier();
		try {
			v.verify(pomFile, pomSourceURL);
		} catch (DownloadVerificationException e) {
			return;
		}
		fail();
	}

	private static File fileFromResource(String resourceName) {
		URL resourceURL = Thread.currentThread().getContextClassLoader()
				.getResource(resourceName);
		if (resourceURL == null) {
			throw new RuntimeException("Unable to locate resource : "
					+ resourceName);
		}
		try {
			return new File(resourceURL.toURI());
		} catch (URISyntaxException e) {
			return new File(resourceURL.getPath());
		}
	}

}
