package net.sf.taverna.tools;


import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class BootstrapTest extends TestCase {

	private String realHome;
	private File tempHome;
	private String realOS;
	private String realTavHome;


	@Override
    public void setUp() throws IOException {
		realHome = System.getProperty("user.home");
		realOS = System.getProperty("os.name");
		realTavHome = System.getProperty("taverna.home");
		System.clearProperty("taverna.home");
		tempHome = File.createTempFile("taverna", ".tmp");
		assertTrue(tempHome.delete());
		assertTrue(tempHome.mkdir());
		assertTrue(tempHome.isDirectory());
		assertEquals(0, tempHome.listFiles().length); //empty
		System.setProperty("user.home", tempHome.getAbsolutePath());
		// Needs to be blank to be able to auto-detect
	}

	@Override
    public void tearDown() throws IOException {
		System.setProperty("user.home", realHome);
		System.setProperty("os.name", realOS);
		if (realTavHome == null) {
			System.clearProperty("taverna.home");
		} else {
			System.setProperty("taverna.home", realTavHome);
		}
		//FileUtils.deleteDirectory(tempHome);
	}

	/**
	 * Test that findUserDir() works on the actual OS
	 *
	 */
	public void testFindUserDir() {
		Bootstrap.findUserDir();
		File userDir = new File(System.getProperty("taverna.home"));
		assertTrue(userDir.isDirectory());
		// Can't check getParent(), would fail on Windows
		assertTrue(userDir.getAbsolutePath().startsWith(tempHome.getAbsolutePath()));
		// Ignore T/t
		assertTrue(userDir.getName().contains("averna"));
	}

	/**
	 * Test that findUserDir() works as expected on OS X
	 */
	public void testFindUserDirMac() {
		System.setProperty("os.name", "Mac OS X");
		Bootstrap.findUserDir();
		File dir = new File(System.getProperty("taverna.home"));
		File shouldBe = new File(tempHome, "Library/Application Support/Taverna-1.6.1");
		assertEquals(shouldBe, dir);
		assertTrue(dir.isDirectory());

	}

	/**
	 * Test that findUserDir() works as expected on Unix
	 */
	public void testFindUserDirUnix() {
		// Anything else is UNIX style
		System.setProperty("os.name", "Linn0x");
		Bootstrap.findUserDir();
		File dir = new File(System.getProperty("taverna.home"));
		File shouldBe = new File(tempHome, ".taverna-1.6.1");
		assertEquals(shouldBe, dir);
		assertTrue(dir.isDirectory());
	}

	/**
	 * Test that findUserDir() works as expected on Windows
	 */
	public void testFindUserDirWindows() {
		System.setProperty("os.name", "Windows XP");
		Bootstrap.findUserDir();
		File dir = new File(System.getProperty("taverna.home"));
		File shouldBe;
		String APPDATA = System.getenv("APPDATA");
		if (APPDATA == null) {
			// Likely on Non-Windows platform
			shouldBe = new File(tempHome, "Taverna-1.6.1");
		} else {
			shouldBe = new File(APPDATA, "Taverna-1.6.1");
		}
		assertEquals(shouldBe, dir);
		assertTrue(dir.isDirectory());
	}

	public void testSetUpMadeTempHome() {
		String home = System.getProperty("user.home");
		assertTrue(home.contains(".tmp"));
	}

}
