package net.sf.taverna.t2.workbench.configuration;

import java.io.File;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractConfigurableTest {

	private File directory;
	
	@Before
	public void setup() throws Exception {
		ConfigurationManager manager = ConfigurationManager.getInstance();
		File f = new File(System.getProperty("java.io.tmpdir"));
		File d = new File(f,UUID.randomUUID().toString());
		d.mkdir();
		directory=d;
		manager.setBaseConfigLocation(d);
		DummyConfigurable.getInstance().restoreDefaults();
	}
	
	@Test
	public void testName() {
		assertEquals("Wrong name","dummy",DummyConfigurable.getInstance().getName());
	}
	
	@Test
	public void testCategory() {
		assertEquals("Wrong category","test",DummyConfigurable.getInstance().getCategory());
	}
	
	@Test
	public void testUUID() {
		assertEquals("Wrong uuid","cheese",DummyConfigurable.getInstance().getUUID());
	}
	
	@Test
	public void testGetProperty() {
		assertEquals("Should be john","john",DummyConfigurable.getInstance().getProperty("name"));
	}
	
	@Test
	public void testSetProperty() {
		assertEquals("Should be blue","blue",DummyConfigurable.getInstance().getProperty("colour"));
		assertNull("Should be null",DummyConfigurable.getInstance().getProperty("new"));
		
		DummyConfigurable.getInstance().setProperty("colour", "red");
		DummyConfigurable.getInstance().setProperty("new", "new value");
		
		assertEquals("Should be red","red",DummyConfigurable.getInstance().getProperty("colour"));
		assertEquals("Should be new value","new value",DummyConfigurable.getInstance().getProperty("new"));
	}
	
	@Test
	public void testDeleteValue() {
		assertEquals("Should be blue","blue",DummyConfigurable.getInstance().getProperty("colour"));
		assertNull("Should be null",DummyConfigurable.getInstance().getProperty("new"));
		
		DummyConfigurable.getInstance().setProperty("new", "new value");
		
		assertEquals("Should be new value","new value",DummyConfigurable.getInstance().getProperty("new"));
		
		DummyConfigurable.getInstance().deleteProperty("new");
		DummyConfigurable.getInstance().deleteProperty("colour");
		
		assertNull("Should be null",DummyConfigurable.getInstance().getProperty("new"));
		assertNull("Should be null",DummyConfigurable.getInstance().getProperty("colour"));
	}
	
	@Test
	public void testDeleteValueBySettingNull() {
		assertEquals("Should be blue","blue",DummyConfigurable.getInstance().getProperty("colour"));
		assertNull("Should be null",DummyConfigurable.getInstance().getProperty("new"));
		
		DummyConfigurable.getInstance().setProperty("new", "new value");
		
		assertEquals("Should be new value","new value",DummyConfigurable.getInstance().getProperty("new"));
		
		DummyConfigurable.getInstance().setProperty("new",null);
		DummyConfigurable.getInstance().setProperty("colour",null);
		
		assertNull("Should be null",DummyConfigurable.getInstance().getProperty("new"));
		assertNull("Should be null",DummyConfigurable.getInstance().getProperty("colour"));
	}
	
	@Test
	public void testRestoreDefaults() {
		assertEquals("There should be 2 values",2,DummyConfigurable.getInstance().getPropertyMap().size());
		
		DummyConfigurable.getInstance().setProperty("colour", "red");
		DummyConfigurable.getInstance().setProperty("new", "new value");
		
		assertEquals("There should be 3 values",3,DummyConfigurable.getInstance().getPropertyMap().size());
		
		DummyConfigurable.getInstance().restoreDefaults();
		
		assertEquals("There should be 2 values",2,DummyConfigurable.getInstance().getPropertyMap().size());
		
		assertEquals("Should be john","john",DummyConfigurable.getInstance().getProperty("name"));
		assertEquals("Should be john","blue",DummyConfigurable.getInstance().getProperty("colour"));
	}
	
}
