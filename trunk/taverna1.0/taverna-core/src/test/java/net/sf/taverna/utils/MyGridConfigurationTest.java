package net.sf.taverna.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

public class MyGridConfigurationTest extends TestCase {
	
	private String realTavHome;
	private File tempHome;
	private String realOS;
	
	// Property that we can expect to be there
	final static String property = "taverna.lsid.providerclass";
	final static String propValue = "org.embl.ebi.escience.baclava.lsid.UUIDLSIDProvider";

	public void setUp() throws IOException {
		realTavHome = System.getProperty("taverna.home");
		realOS = System.getProperty("os.name");
		tempHome = File.createTempFile("taverna", ".tmp");
		assertTrue(tempHome.delete());
		assertTrue(tempHome.mkdir());
		assertTrue(tempHome.isDirectory());
		assertEquals(0, tempHome.listFiles().length); //empty
		System.setProperty("taverna.home", tempHome.getAbsolutePath());
		MyGridConfiguration.flushProperties();
		assertNull(MyGridConfiguration.properties);
	}
	
	public void tearDown() throws IOException {
		if (realTavHome == null) {
			System.clearProperty("taverna.home");
		} else {
			System.setProperty("taverna.home", realTavHome);
		}
		System.setProperty("os.name", realOS);
		//FileUtils.deleteDirectory(tempHome);
		// Might have been messed up by getProperties calls
		MyGridConfiguration.flushProperties();
	}
	
	public void testMadeTempHome() {
		String home = System.getProperty("taverna.home");
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
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		assertTrue(propertiesDist.isFile());
		String content = FileUtils.readFileToString(propertiesDist, "utf8");
		// global header
		assertTrue(content.startsWith("# Default values are shown"));
		// local header
		assertTrue(content.contains("\n# Default properties from file:/"));
		// Should include the distinal config file, we'll test for parts of that
		assertTrue(content.contains("\n# Taverna configuration file"));
		// The defaults should have been included only once (ie. the split will make two parts)
		// NOTE: This might fail in Eclipse!	 
		assertEquals(2, content.split("Taverna configuration file").length);
		
		// Blank lines should be preserved as-is
		assertTrue(content.contains("\n\n\n# LSID Configuration"));
		// This default should be there (including any tabs), but double-commented out
		assertTrue(content.contains("\n##\t" + property + " = " +
				propValue + "\n"));
		// And the end should be as in the distinal
		assertTrue(content.endsWith("\n#--------------------------------------------------------------------\n"));
	}
	
	/**
	 * Test that both mygrid.properties and log4j.properties are written out,
	 * but seperately.
	 * @throws IOException 
	 */
	public void testWriteDefaultConfDistTwoFiles() throws IOException {
		File mygridPropertiesDist =
			new File(MyGridConfiguration.getUserDir("conf"),
				"mygrid.properties.dist");
		File log4jPropertiesDist =
			new File(MyGridConfiguration.getUserDir("conf"),
				"log4j.properties.dist");
		assertFalse(mygridPropertiesDist.isFile());
		assertFalse(log4jPropertiesDist.isFile());
		

		// Didn't touch log4j.properties
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		assertTrue(mygridPropertiesDist.isFile());
		assertFalse(log4jPropertiesDist.isFile());
		mygridPropertiesDist.delete();
		assertFalse(mygridPropertiesDist.isFile());
		
		// Didn't touch mygrid.properties
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.LOG4J_PROPERTIES);
		assertFalse(mygridPropertiesDist.isFile());
		assertTrue(log4jPropertiesDist.isFile());
		
		String content = FileUtils.readFileToString(log4jPropertiesDist, "utf8");
		// global header
		assertTrue(content.startsWith("# Default values are shown"));
		assertFalse(content.contains("\n# Taverna configuration file"));
		assertTrue(content.contains("\n##log4j.rootLogger"));
	}
	
	
	/**
	 * Test the upgrade mechanism for mygrid.properties and mygrid.properties.dist
	 * <p>
	 * In short, if mygrid.properties exist, it will be upgraded only if it matches completely with the old mygrid.properties.dist. 
	 * mygrid.properties.dist should always be upgraded. 
	 * 
	 * @throws IOExceptqion
	 */
	public void testWriteDefaultConf() throws IOException {
		File propertiesDist = new File(MyGridConfiguration.getUserDir("conf"), 
			"mygrid.properties.dist");
		File properties = new File(MyGridConfiguration.getUserDir("conf"), 
			"mygrid.properties");
		assertFalse(properties.isFile());
		assertFalse(propertiesDist.isFile());
		
		// Create both files when they don't exist
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		assertTrue(properties.isFile());
		assertTrue(propertiesDist.isFile());
		assertTrue(FileUtils.contentEquals(propertiesDist, properties));
		
		// rewrite mygrid.properties
		properties.delete();
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		assertTrue(FileUtils.contentEquals(propertiesDist, properties));

		// rewrite mygrid.properties.dist
		propertiesDist.delete();
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		assertTrue(FileUtils.contentEquals(propertiesDist, properties));
		
		// We'll change the mygrid.properties so that it won't be upgraded
		FileUtils.writeStringToFile(properties, "Changed something", "latin1");
		String content = FileUtils.readFileToString(properties, "latin1");
		assertEquals("Changed something", content);
		// They should now be different
		assertFalse(FileUtils.contentEquals(propertiesDist, properties));
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		// And it should not have been changed
		content = FileUtils.readFileToString(properties, "latin1");
		assertEquals("Changed something", content);
		assertFalse(FileUtils.contentEquals(propertiesDist, properties));

		// Unless it is equal to the .dist, then it is probably
		// just an old version
		FileUtils.writeStringToFile(propertiesDist, "Changed something", "latin1");
		String distContent = FileUtils.readFileToString(propertiesDist, "latin1");
		assertEquals("Changed something", distContent);
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		assertTrue(FileUtils.contentEquals(propertiesDist, properties));
		// And that crap we added should now be gone
		distContent = FileUtils.readFileToString(propertiesDist, "latin1");
		assertTrue(distContent.contains("Taverna"));
		assertFalse(distContent.contains("Changed something"));
	}
	
	public void testloadUserProperties() throws IOException {
		Properties empty = new Properties();
		// Should be empty as the file doesn't even exist yet
		assertEquals(empty, MyGridConfiguration.loadUserProperties(MyGridConfiguration.MYGRID_PROPERTIES));
		// Did not touch the global cache, that is only touched by
		// getProperties();
		assertNull(MyGridConfiguration.properties);

		// OK, let's get something to work with
		MyGridConfiguration.writeDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		
		// the default config should be all ## commented out
		assertEquals(empty, MyGridConfiguration.loadUserProperties(MyGridConfiguration.MYGRID_PROPERTIES));
		File properties = new File(MyGridConfiguration.getUserDir("conf"), 
			"mygrid.properties");
		String content = FileUtils.readFileToString(properties, "latin1");
		// uncomment those defaults
		content = content.replace("\n##", "\n");
		FileUtils.writeStringToFile(properties, content, "latin1");
		
		// Now it should no longer be empty
		assertFalse(empty.equals(MyGridConfiguration.loadUserProperties(MyGridConfiguration.MYGRID_PROPERTIES)));
		// but it should be the same as the default properties
		assertEquals(MyGridConfiguration.loadDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES),
					MyGridConfiguration.loadUserProperties(MyGridConfiguration.MYGRID_PROPERTIES));
		
		// OK, let's try to override some of those things instead. 
		// We'll first check that we have a good property to test
		assertEquals(propValue, 
				MyGridConfiguration.loadDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES).get(property));
		// We'll change that to "fish" and add a new property
		String testProp = "taverna.test.property";
		FileUtils.writeStringToFile(properties, 
				property + " = fish\n" +
				testProp + " = test\n", 
				"latin1");
		Properties user = MyGridConfiguration.loadUserProperties(MyGridConfiguration.MYGRID_PROPERTIES);
		Properties defaults = MyGridConfiguration.loadDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES);
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
		for (URL url : MyGridConfiguration.findResources("fish.properties", null)) {
			fail("Should not have found " + url);
		}
		boolean foundMygrid = false;
		for (URL url : MyGridConfiguration.findResources("mygrid.properties", null)) {
			if (foundMygrid) {
				// NOTE: This might happen in Eclipse. Run tests from maven.
				fail("Found more than one mygrid.properties: " + url);
			}
			foundMygrid = true;
			assertTrue(url.getPath().contains("mygrid.properties"));
		}
		assertTrue(foundMygrid);
	}
	
	public void testloadDefaultProperties() {
		Properties props = MyGridConfiguration.loadDefaultProperties(MyGridConfiguration.MYGRID_PROPERTIES, null);
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
	
	public void testGetPropertiesLog4j() {
		Properties props = MyGridConfiguration.getProperties(MyGridConfiguration.LOG4J_PROPERTIES);
		assertTrue(props.containsKey("log4j.rootLogger"));
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
