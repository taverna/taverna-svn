package eu.medsea.mimeutil.detector;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import eu.medsea.mimeutil.MimeUtil2;

import eu.medsea.util.EncodingGuesser;

import junit.framework.TestCase;

public class OpendesktopMimeDetectorTest extends TestCase {

	MimeUtil2 mimeUtil = new MimeUtil2();

	public void setUp() {
		mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
	}

	public void tearDown() {
		mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
	}


	public void testGetDescription() {
		MimeDetector mimeDetector = mimeUtil.getMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
		// Ignore version number
		assertTrue(mimeDetector.getDescription().contains("Resolve mime types for files and streams using the Opendesktop shared mime.cache file. Version ["));
	}

	public void testGetMimeTypesFileGlob() {

		assertContains("text/plain", mimeUtil.getMimeTypes(new File("abc.txt")));
		assertContains("text/x-makefile", mimeUtil.getMimeTypes(new File("makefile")));
		assertContains("text/x-makefile", mimeUtil.getMimeTypes(new File("Makefile")));
		assertContains("image/x-win-bitmap", mimeUtil.getMimeTypes(new File("x.cur")));
		assertContains("application/vnd.ms-tnef", mimeUtil.getMimeTypes(new File("winmail.dat")));
		assertContains("text/x-troff-mm", mimeUtil.getMimeTypes(new File("abc.mm")));
		assertContains("text/x-readme", mimeUtil.getMimeTypes(new File("README")));
		assertContains("video/x-anim", mimeUtil.getMimeTypes(new File("abc.anim5")));
		assertContains("video/x-anim", mimeUtil.getMimeTypes(new File("abc.animj")));
		assertContains("text/x-readme", mimeUtil.getMimeTypes(new File("READMEFILE")));
		assertContains("text/x-readme", mimeUtil.getMimeTypes(new File("READMEanim3")));
		assertContains("text/x-log", mimeUtil.getMimeTypes(new File("README.log")));
		assertContains("text/x-readme", mimeUtil.getMimeTypes(new File("README.file")));
		assertContains("application/x-compress", mimeUtil.getMimeTypes(new File("README.Z")));
		assertContains(MimeUtil2.UNKNOWN_MIME_TYPE, mimeUtil.getMimeTypes(new File("READanim3")));

		
		// Try multi extensions
		String jarExpected = "application/x-java-archive";
		String jarMimeType = mimeUtil.getMimeTypes(new File("e.1.3.jar"))
				.toString();
		
		
		assertTrue("jar mime type did not contain " + jarExpected + " but "
				+ jarMimeType, jarMimeType.contains(jarExpected));
	}

	public void testGetMimeTypesFile() {
		// Globbing won't work so lets try magic sniffing
		assertContains("application/xml", mimeUtil.getMimeTypes(new File("src/test/resources/e[xml]")));

		// This is a text file so the text file detector should be used but first verify it's not matched
		assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/plaintext")).contains("text/plain"));

		// Now set the supported encodings to all encodings supported by the JVM
		EncodingGuesser.setSupportedEncodings(EncodingGuesser.getCanonicalEncodingNamesSupportedByJVM());
		assertContains("text/plain", mimeUtil.getMimeTypes(new File("src/test/resources/plaintext")));
		// Clean out the encodings using an empty collection
		EncodingGuesser.setSupportedEncodings(new ArrayList());


	}

	public void testGetMimeTypesURL() {
		try {
			assertContains("application/x-java", mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/MimeDetector.class")));
			assertContains("application/x-java", mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/MimeDetector.class")));
			assertContains("text/x-java", mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/MimeDetector.java")));
			assertContains("text/html", mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/a.html")));
			assertContains("image/gif", mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/c-gif.img")));
			assertContains("image/svg+xml", mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/e.svg")));
			assertContains("application/x-compressed-tar", mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/f.tar.gz")));
			assertContains("application/xml", mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/e[xml]")));
		}catch(Exception e) {
			fail("Should not get here " + e.getLocalizedMessage());
		}
	}

	/**
	 * Check if the expected object is in the collection
	 * 
	 * Needed as OpenDesktop mime descriptions might otherwise make these tests
	 * fail on certain operating systems (such as Linux)
	 * 
	 * @param expected
	 * @param collection
	 */
	public static void assertContains(Object expected, Collection collection) {
		assertTrue("Did not contain " + expected + ", but: " + collection, collection
				.contains(expected));
	}

}
