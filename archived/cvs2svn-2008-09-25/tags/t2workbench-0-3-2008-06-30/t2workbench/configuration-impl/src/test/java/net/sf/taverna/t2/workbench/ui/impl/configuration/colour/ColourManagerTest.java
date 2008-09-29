package net.sf.taverna.t2.workbench.ui.impl.configuration.colour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

import org.junit.Test;

public class ColourManagerTest {

	@Test
	public void testGetInstance() throws Exception {
		ColourManager manager=ColourManager.getInstance();
		assertNotNull(manager);
		ColourManager manager2=ColourManager.getInstance();
		assertSame("They should be the same instance",manager, manager2);
	}
	
	@Test
	public void testGetPreferredColourEqualsWhite() throws Exception {
		String dummy=new String();
		
		Color c = ColourManager.getInstance().getPreferredColour(dummy);
		assertEquals("The default colour should be WHITE", Color.WHITE,c);
	}
	
	@Test
	public void testConfigurableness() throws Exception {
		ColourManager manager=ColourManager.getInstance();
		assertTrue(manager instanceof Configurable);
		
		assertEquals("wrong category","colour", manager.getCategory());
		assertEquals("wrong name","Colour Management", manager.getName());
		assertEquals("wrong UUID","d13327f0-0c84-11dd-bd0b-0800200c9a66", manager.getUUID());
		assertNotNull("property map is missing",manager.getPropertyMap());
		assertNotNull("there is no default property map",manager.getDefaultPropertyMap());
	}
	
	@Test
	public void saveAsWrongArrayType() {
		String dummy = "";
		ColourManager manager=ColourManager.getInstance();
		manager.getPropertyMap().put(dummy.getClass().getCanonicalName(), "#ffffff");
		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		instance.setBaseConfigLocation(new File(System.getProperty("java.io.tmpdir")+File.separatorChar+"scratch"));
		try {
			instance.store(manager);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			instance.populate(manager);
		} catch (Exception e) {
			
		}
		try {
			manager.getPreferredColour(dummy);
		} catch (Exception e) {
			
		}
	}
	
}