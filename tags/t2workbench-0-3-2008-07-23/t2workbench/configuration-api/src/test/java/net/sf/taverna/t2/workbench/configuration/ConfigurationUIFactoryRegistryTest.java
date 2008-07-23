package net.sf.taverna.t2.workbench.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.swing.JPanel;

import org.junit.Test;

public class ConfigurationUIFactoryRegistryTest {

	@Test
	public void testGetAll() {
		List<ConfigurationUIFactory> list = ConfigurationUIRegistry.getInstance().getConfigurationUIFactories();
		assertNotNull(list);
		assertTrue("There should be at least 2 ConfigurationUI's  in the list",list.size()>=2);
		boolean found=false;
		for (ConfigurationUIFactory f : list) {
			if (f instanceof DummyUIFactory1) {
				found=true;
				break;
			}
		}
		assertTrue("There should be a DummyUIFactory1 in the list",found);
		
		found=false;
		for (ConfigurationUIFactory f : list) {
			if (f instanceof DummyUIFactory1) {
				found=true;
				break;
			}
		}
		assertTrue("There should be a DummyUIFactory2 in the list",found);
	}
	
	@Test
	public void testGetByConfigurable() {
		List<ConfigurationUIFactory> list=ConfigurationUIRegistry.getInstance().getConfigurationUIFactoriesForConfigurable(new DummyUIFactory1.DummyConfigurable1());
		assertEquals("There should be 1 item in the list",1,list.size());
		ConfigurationUIFactory f = list.get(0);
		assertTrue("The item should be a DummyUIFactory1",f instanceof DummyUIFactory1);
		assertTrue("The configurable should be a DummyUIFactory1.DummyConfigurable1",f.getConfigurable() instanceof DummyUIFactory1.DummyConfigurable1);
		assertTrue("The panel should be an instanceo fo a JLabel",f.getConfigurationPanel() instanceof JPanel);
	}
}
