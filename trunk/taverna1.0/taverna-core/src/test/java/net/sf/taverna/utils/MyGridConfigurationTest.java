package net.sf.taverna.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

public class MyGridConfigurationTest extends TestCase {
	
	private String realHome;
	private File tempHome;
	private String realOS;
	
	// Property that we can expect to be there
	final static String property = "taverna.lsid.providerclass";
	final static String propValue = "org.embl.ebi.escience.baclava.UUIDLSIDProvider";

	public void setUp() throws IOException {
		realHome = System.getProperty("user.home");
		realOS = System.getProperty("os.name");
		tempHome = File.createTempFile("taverna", ".tmp");
		assertTrue(tempHome.delete());
		assertTrue(tempHome.mkdir());
		assertTrue(tempHome.isDirectory());
		assertEquals(0, tempHome.listFiles().length); //empty
		System.setProperty("user.home", tempHome.getAbsolutePath());
		assertNull(MyGridConfiguration.properties);
	}
	
	public void tearDown() throws IOException {
		System.setProperty("user.home", realHome);
		System.setProperty("os.name", realOS);
		//FileUtils.deleteDirectory(tempHome);
		// Might have been messed up by getProperties calls
		MyGridConfiguration.flushProperties();
	}
	
	public void testMadeTempHome() {
		String home = System.getProperty("user.home");
		assertTrue(home.contains(".tmp"));
	}
	
	public void testGetUserDir() {
		File userDir = MyGridConfiguration.getUserDir();
		assertTrue(userDir.isDirectory());
		// Can't check getParent(), would fail on Windows
		assertTrue(userDir.getAbsolutePath().startsWith(tempHome.getAbsolutePath()));
		// Ignore T/t
		assertTrue(userDir.getName().contains("averna"));
	}
	
	/* 
	 * Test that getUserDir() works as expected on different OS-es
	 * 
	 */
	public void testGetUserDirMultiOS() {
		System.setProperty("os.name", "Mac OS X");
		File dir = MyGridConfiguration.getUserDir();
		File shouldBe = new File(tempHome, "Library/Application Support/Taverna");
		assertEquals(shouldBe, dir);
		assertTrue(dir.isDirectory());
		
		System.setProperty("os.name", "Windows XP");
		dir = MyGridConfiguration.getUserDir();
		String APPDATA = System.getenv("APPDATA");
		if (APPDATA == null) {
			// Likely on Non-Windows platform
			shouldBe = new File(tempHome, "Taverna");
		} else {
			shouldBe = new File(APPDATA, "Taverna");
		}
		assertEquals(shouldBe, dir);
		assertTrue(dir.isDirectory());
	
		// Anything else is UNIX style
		System.setProperty("os.name", "Linn0x");
		dir = MyGridConfiguration.getUserDir();
		shouldBe = new File(tempHome, ".taverna");
		assertEquals(shouldBe, dir);
		assertTrue(dir.isDirectory());
	}
	
	
	public void testGetUserDirModule() {
		File moduleDir = MyGridConfiguration.getUserDir("conf");
		assertTrue(moduleDir.isDirectory());
		assertEquals("conf", moduleDir.getName());
	}
	
	/**
	 * Test that mygrid properties are written out to the
	 * mygrid.properties.dist, and that the defaults are 
	 * double-commented out
	 * 
	 * @throws IOException
	 */
	public void testWriteDefaultConfDist() throws IOException {
		File propertiesDist = new File(MyGridConfiguration.getUserDir("conf"), 
									 "mygrid.properties.dist");
		assertFalse(propertiesDist.isFile());
		MyGridConfiguration.writeDefaultProperties();
		assertTrue(propertiesDist.isFile());
		String content = FileUtils.readFileToString(propertiesDist, "utf8");
		// global header
		assertTrue(content.startsWith("# Default values are shown"));
		// local header
		assertTrue(content.contains("\n# Default properties from file:/"));
		// Should include the distinal config file, we'll test for parts of that
		assertTrue(content.contains("\n# Taverna configuration file"));
		// The defaults should have been included only once (ie. the split will make two parts)
		assertEquals(2, content.split("Taverna configuration file").length);
		// Blank lines should be preserved as-is
		assertTrue(content.contains("\n\n\n# LSID Configuration"));
		// This default should be there (including any tabs), but double-commented out
		assertTrue(content.contains("\n##\ttaverna.lsid.providerclass = " +
				"org.embl.ebi.escience.baclava.UUIDLSIDProvider\n"));
		// And the end should be as in the distinal
		assertTrue(content.endsWith("\n#--------------------------------------------------------------------\n"));
	}
	
	
	/**
	 * Test the upgrade mechanism for mygrid.properties and mygrid.properties.dist
	 * <p>
	 * In short, if mygrid.properties exist, it will be upgraded only if it matches completely with the old mygrid.properties.dist. 
	 * mygrid.properties.dist should always be upgraded. 
	 * 
	 * @throws IOException
	 */
	public void testWriteDefaultConf() throws IOException {
		File propertiesDist = new File(MyGridConfiguration.getUserDir("conf"), 
			"mygrid.properties.dist");
		File properties = new File(MyGridConfiguration.getUserDir("conf"), 
			"mygrid.properties");
		assertFalse(properties.isFile());
		assertFalse(propertiesDist.isFile());
		
		// Create both files when they don't exist
		MyGridConfiguration.writeDefaultProperties();
		assertTrue(properties.isFile());
		assertTrue(propertiesDist.isFile());
		assertTrue(FileUtils.contentEquals(propertiesDist, properties));
		
		// rewrite mygrid.properties
		properties.delete();
		MyGridConfiguration.writeDefaultProperties();
		assertTrue(FileUtils.contentEquals(propertiesDist, properties));

		// rewrite mygrid.properties.dist
		propertiesDist.delete();
		MyGridConfiguration.writeDefaultProperties();
		assertTrue(FileUtils.contentEquals(propertiesDist, properties));
		
		// We'll change the mygrid.properties so that it won't be upgraded
		FileUtils.writeStringToFile(properties, "Changed something", "latin1");
		String content = FileUtils.readFileToString(properties, "latin1");
		assertEquals("Changed something", content);
		// They should now be different
		assertFalse(FileUtils.contentEquals(propertiesDist, properties));
		MyGridConfiguration.writeDefaultProperties();
		// And it should not have been changed
		content = FileUtils.readFileToString(properties, "latin1");
		assertEquals("Changed something", content);
		assertFalse(FileUtils.contentEquals(propertiesDist, properties));

		// Unless it is equal to the .dist, then it is probably
		// just an old version
		FileUtils.writeStringToFile(propertiesDist, "Changed something", "latin1");
		String distContent = FileUtils.readFileToString(propertiesDist, "latin1");
		assertEquals("Changed something", distContent);
		MyGridConfiguration.writeDefaultProperties();
		assertTrue(FileUtils.contentEquals(propertiesDist, properties));
		// And that crap we added should now be gone
		distContent = FileUtils.readFileToString(propertiesDist, "latin1");
		assertTrue(distContent.contains("Taverna"));
		assertFalse(distContent.contains("Changed something"));
	}
	
	public void testloadUserProperties() throws IOException {
		Properties empty = new Properties();
		// Should be empty as the file doesn't even exist yet
		assertEquals(empty, MyGridConfiguration.loadUserProperties());
		// Did not touch the global cache, that is only touched by
		// getProperties();
		assertNull(MyGridConfiguration.properties);

		// OK, let's get something to work with
		MyGridConfiguration.writeDefaultProperties();
		
		// the default config should be all ## commented out
		assertEquals(empty, MyGridConfiguration.loadUserProperties());
		File properties = new File(MyGridConfiguration.getUserDir("conf"), 
			"mygrid.properties");
		String content = FileUtils.readFileToString(properties, "latin1");
		// uncomment those defaults
		content = content.replace("\n##", "\n");
		FileUtils.writeStringToFile(properties, content, "latin1");
		
		// Now it should no longer be empty
		assertFalse(empty.equals(MyGridConfiguration.loadUserProperties()));
		// but it should be the same as the default properties
		assertEquals(MyGridConfiguration.loadDefaultProperties(),
					MyGridConfiguration.loadUserProperties());
		
		// OK, let's try to override some of those things instead. 
		// We'll first check that we have a good property to test
		assertEquals(propValue, 
				MyGridConfiguration.loadDefaultProperties().get(property));
		// We'll change that to "fish" and add a new property
		String testProp = "taverna.test.property";
		FileUtils.writeStringToFile(properties, 
				property + " = fish\n" +
				testProp + " = test\n", 
				"latin1");
		Properties user = MyGridConfiguration.loadUserProperties();
		Properties defaults = MyGridConfiguration.loadDefaultProperties();
		// Our new property should be there
		assertEquals("test", user.get(testProp));
		// but not in the defaults
		assertNull(defaults.get(testProp));
		// In defaults, propValue is unchanged
		assertEquals(propValue, 
				defaults.get(property));
		// but the user props should have "fish" instead
		assertEquals("fish", user.get(property));
	}
	
	public void testFindDefaultResources() {
		for (URL url : MyGridConfiguration.findResources("fish.properties")) {
			fail("Should not have found " + url);
		}
		boolean foundMygrid = false;
		for (URL url : MyGridConfiguration.findResources("mygrid.properties")) {
			if (foundMygrid) {
				fail("Found more than one mygrid.properties: " + url);
			}
			foundMygrid = true;
			assertTrue(url.getPath().contains("mygrid.properties"));
		}
		assertTrue(foundMygrid);
	}
	
	public void testLoadDefaultProperties() {
		Properties props = MyGridConfiguration.loadDefaultProperties();
		// Did not touch the global cache, that is only touched by
		// getProperties();
		assertNull(MyGridConfiguration.properties);
		// We'll assume this property to be loaded
		assertEquals(propValue, props.get(property));
	}
	
	public void testGetPropertiesLoadDefault() {
		assertNull(MyGridConfiguration.properties);
		Properties props = MyGridConfiguration.getProperties();
		assertNotNull(MyGridConfiguration.properties);
		assertEquals(propValue, props.get(property));
	}
	
	public void testGetPropertiesWithUser() throws IOException {
		File properties = new File(MyGridConfiguration.getUserDir("conf"), 
			"mygrid.properties");
		String testProp = "taverna.test.property";
		FileUtils.writeStringToFile(properties, 
				property + " = fish\n" +
				testProp + " = test\n", 
				"latin1");
		Properties props = MyGridConfiguration.getProperties();
		assertEquals("test", props.get(testProp));
		assertEquals("fish", props.get(property));
	}
	
}
