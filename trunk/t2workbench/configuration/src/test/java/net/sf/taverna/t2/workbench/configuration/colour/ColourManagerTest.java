package net.sf.taverna.t2.workbench.configuration.colour;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.awt.Color;

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
}