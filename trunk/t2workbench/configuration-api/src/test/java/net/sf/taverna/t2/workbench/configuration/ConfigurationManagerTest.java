package net.sf.taverna.t2.workbench.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.colour.DummyColour;

import org.junit.Ignore;
import org.junit.Test;

public class ConfigurationManagerTest {
	
	@Test
	public void createConfigManager() {
		ConfigurationManager instance = ConfigurationManager.getInstance();
		assertNotNull("Config Manager should not be null", instance);
	}
	
	@Ignore("Hardcoded /Users/Ian") //FIXME: update test to work using File.createTempFile(...)
	@Test
	public void populateConfigOfColourmanager() {
		ColourManager manager=ColourManager.getInstance();
		
		manager.getPropertyMap().put("colour.first", 25);
		manager.getPropertyMap().put("colour.second", 223);
		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		instance.setBaseConfigLocation(new File("/Users/Ian/scratch"));
		try {
			instance.store(manager);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		ColourManager manager2 = ColourManager.getInstance();
		
		try {
			instance.populate(manager2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Map<String, Object> propertyMap = manager2.getPropertyMap();
		
		assertEquals("Properties do not match", propertyMap.get("colour.first"), manager.getPropertyMap().get("colour.first"));
		assertEquals("Properties do not match", propertyMap.get("colour.second"), manager.getPropertyMap().get("colour.second"));
		
		
	}
	
	@Test
	public void saveColoursForDummyColourable() {
		DummyColour dummy = new DummyColour();
		ColourManager manager=ColourManager.getInstance();
		manager.getPropertyMap().put(dummy.getClass().getCanonicalName(), new String[] {"10", "20", "56"});
		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		instance.setBaseConfigLocation(new File("/tmp/scratch"));
		try {
			instance.store(manager);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			instance.populate(manager);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
