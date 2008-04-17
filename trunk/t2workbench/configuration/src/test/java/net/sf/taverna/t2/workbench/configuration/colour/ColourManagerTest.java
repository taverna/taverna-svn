package net.sf.taverna.t2.workbench.configuration.colour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import net.sf.taverna.t2.workbench.configuration.Configurable;

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
	public void testGetPreferredColour() throws Exception {
		Colourable dummy=new Colourable() {
			
		};
		
		Color c = ColourManager.getInstance().getPreferredColour(dummy);
		assertNotNull("The returned colour should not be null",c);
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
}