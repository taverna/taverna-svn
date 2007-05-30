package net.sf.taverna.utils;

import java.io.File;
import java.io.IOException;

import net.sf.taverna.utils.MyGridConfiguration;

import junit.framework.TestCase;

public class MyGridConfigurationTest extends TestCase {
	
	private String realTavHome;
	
	// Property that we can expect to be there
	final static String property = "taverna.lsid.providerclass";
	final static String propValue = "org.embl.ebi.escience.baclava.lsid.UUIDLSIDProvider";

	public void setUp() throws IOException {
		realTavHome=System.getProperty("taverna.home");
		String resourcePath = MyGridConfigurationTest.class.getResource("/conf/mygrid.properties").toExternalForm();
		resourcePath=resourcePath.replaceAll("file:","");
		resourcePath=resourcePath.replaceAll("conf/mygrid.properties", "");
		System.out.println("Looking for conf/mygrid.properties in: "+resourcePath);
		System.setProperty("taverna.home", resourcePath);
	}
	
	public void tearDown() throws IOException {
		MyGridConfiguration.flushProperties();
		if (realTavHome == null) {
			System.clearProperty("taverna.home");
		} else {
			System.setProperty("taverna.home", realTavHome);
		}
	}
	
	public void testMadeTempHome() {
		String home = System.getProperty("taverna.home");
		assertNotNull(home);
	}
	
	public void testPropertiesArePutOnSystem() {
		assertNotNull(System.getProperty("taverna.ontology.location"));
	}
	
	public void testGetUserDir() {
		File userDir = MyGridConfiguration.getUserDir();
		assertTrue(userDir.isDirectory());
	}
	
	
	public void testGetUserDirModule() {
		File moduleDir = MyGridConfiguration.getUserDir("conf");
		assertTrue(moduleDir.isDirectory());
		assertEquals("conf", moduleDir.getName());
	}
	
	public void testGetSystemProperty() {	
		System.setProperty("bob.monkhouse", "bob monkhouse");
		String value = MyGridConfiguration.getProperty("bob.monkhouse");
		assertEquals("System property not found",value,"bob monkhouse");
	}
}
