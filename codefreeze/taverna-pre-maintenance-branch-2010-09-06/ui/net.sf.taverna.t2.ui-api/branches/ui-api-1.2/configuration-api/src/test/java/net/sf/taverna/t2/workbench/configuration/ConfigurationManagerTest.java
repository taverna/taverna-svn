/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationManagerTest {

private File configFile;
	
	@Before
	public void setup() throws Exception {
		ConfigurationManager manager = ConfigurationManager.getInstance();
		File f = new File(System.getProperty("java.io.tmpdir"));
		File configTestsDir = new File(f,"configTests");
		if (!configTestsDir.exists()) configTestsDir.mkdir();
		File d = new File(configTestsDir,UUID.randomUUID().toString());
		d.mkdir();
		manager.setBaseConfigLocation(d);
		configFile = new File(d,manager.generateFilename(DummyConfigurable.getInstance()));
		DummyConfigurable.getInstance().restoreDefaults();
	}
	
	@Test
	public void testStore() throws Exception {
		Configurable conf = DummyConfigurable.getInstance();
		ConfigurationManager manager = ConfigurationManager.getInstance();
		manager.store(conf);
		assertTrue(configFile.exists());
	}
	
	@Test
	public void testDefaultValues() throws Exception {
		Configurable conf = DummyConfigurable.getInstance();
		ConfigurationManager manager = ConfigurationManager.getInstance();
		assertEquals("name should equal john","john",conf.getProperty("name"));
		manager.store(conf);
		Properties props = new Properties();
		props.load(new FileInputStream(configFile));
		assertFalse("stored properties should not contain the default value",props.containsKey("name"));
		manager.populate(conf);
		assertEquals("default property name should still exist after re-populating","john",conf.getProperty("name"));
	}
	
	@Test
	public void testRemoveNotDefaultValue() throws Exception {
		Configurable conf = DummyConfigurable.getInstance();
		ConfigurationManager manager = ConfigurationManager.getInstance();
		conf.setProperty("hhh", "iii");
		manager.store(conf);
		Properties props = new Properties();
		props.load(new FileInputStream(configFile));
		assertEquals("The stored file should contain the new entry","iii",props.get("hhh"));
		conf.deleteProperty("hhh");
		manager.store(conf);
		manager.populate(conf);
		assertNull("The removed value should no longer exist",conf.getProperty("hhh"));
		props.clear();
		props.load(new FileInputStream(configFile));
		assertNull("The stored file should no longer contain the deleted entry",props.get("hhh"));
	}
	
	@Test
	public void testNewValues() throws Exception {
		Configurable conf = DummyConfigurable.getInstance();
		conf.setProperty("country", "france");
		ConfigurationManager manager = ConfigurationManager.getInstance();
		assertEquals("country should equal france","france",conf.getProperty("country"));
		manager.store(conf);
		Properties props = new Properties();
		props.load(new FileInputStream(configFile));
		assertTrue("stored properties should contain the default value",props.containsKey("country"));
		assertEquals("stored property country should equal france","france",props.getProperty("country"));
		manager.populate(conf);
		assertEquals("default property name should still exist after re-populating","france",conf.getProperty("country"));
	}
	
	@Test
	public void testDeleteDefaultProperty() throws Exception {
		AbstractConfigurable conf = DummyConfigurable.getInstance();
		ConfigurationManager manager = ConfigurationManager.getInstance();
		assertEquals("name should equal john","john",conf.getProperty("name"));
		conf.deleteProperty("name");
		manager.store(conf);
		manager.populate(conf);
		assertNull("value for name should be null",conf.getProperty("name"));
		
		Properties props = new Properties();
		props.load(new FileInputStream(configFile));
		assertTrue("Key name should be in stored props because its a deleted default value",props.containsKey("name"));
		assertEquals("name should have the special value to indicate its been deleted",ConfigurationManager.DELETED_VALUE_CODE,props.getProperty("name"));
	}
	
	@Test
	public void testFilename() {
		assertTrue(configFile.getAbsolutePath().endsWith("dummyPrefix-cheese.config"));
	}
}
