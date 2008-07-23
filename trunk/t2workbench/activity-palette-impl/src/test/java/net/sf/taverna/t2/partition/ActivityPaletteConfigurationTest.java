package net.sf.taverna.t2.partition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteConfiguration;

import org.junit.Before;
import org.junit.Test;

public class ActivityPaletteConfigurationTest {

	private Configurable conf;
	@Before
	public void setup() {
		ConfigurationManager manager = ConfigurationManager.getInstance();
		File f = new File(System.getProperty("java.io.tmpdir"));
		File d = new File(f,UUID.randomUUID().toString());
		d.mkdir();
		manager.setBaseConfigLocation(d);
		conf=ActivityPaletteConfiguration.getInstance();
		conf.restoreDefaults();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEmptyList() throws Exception {
		conf.setProperty("list", new ArrayList<String>());
		assertTrue("Result was not a list but was:"+conf.getProperty("list"),conf.getProperty("list") instanceof ArrayList);
		ConfigurationManager.getInstance().store(conf);
		ConfigurationManager.getInstance().populate(conf);
		assertTrue("Result was not a list but was:"+conf.getProperty("list"),conf.getProperty("list") instanceof ArrayList);
		ArrayList list = (ArrayList)conf.getProperty("list");
		assertEquals("There should be 0 elements",0,list.size());
		assertEquals("The list should be in the propertyMap now",list,conf.getPropertyMap().get("list"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSingleItem() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("fred");
		conf.setProperty("single", list);
		ConfigurationManager.getInstance().store(conf);
		ConfigurationManager.getInstance().populate(conf);
		
		assertTrue("should be an ArrayList",conf.getProperty("single") instanceof ArrayList);
		ArrayList l = (ArrayList)conf.getProperty("single");
		assertEquals("There should be 1 element",1,l.size());
		assertEquals("Its value should be fred","fred",l.get(0));
	}
	
	@Test
	public void testList() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("fred");
		list.add("bloggs");
		conf.setProperty("list", list);
		ConfigurationManager.getInstance().store(conf);
		ConfigurationManager.getInstance().populate(conf);
		
		assertTrue("should be an ArrayList",conf.getProperty("list") instanceof ArrayList);
		ArrayList l = (ArrayList)conf.getProperty("list");
		assertEquals("There should be 1 element",2,l.size());
		assertEquals("Its value should be fred","fred",l.get(0));
		assertEquals("Its value should be bloggs","bloggs",l.get(1));
	}
	
	@Test
	public void testNull() throws Exception {
		assertNull("Should return null",conf.getProperty("blah blah blah"));
	}
}
