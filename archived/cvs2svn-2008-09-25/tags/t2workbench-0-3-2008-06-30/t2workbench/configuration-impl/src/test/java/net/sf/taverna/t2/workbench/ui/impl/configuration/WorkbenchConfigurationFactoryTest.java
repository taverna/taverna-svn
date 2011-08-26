package net.sf.taverna.t2.workbench.ui.impl.configuration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIRegistry;

import org.junit.Before;
import org.junit.Test;
public class WorkbenchConfigurationFactoryTest {
	
	@Before
	public void setup() {
		ConfigurationManager manager = ConfigurationManager.getInstance();
		manager.setBaseConfigLocation(new File(System.getProperty("java.io.tmpdir")));
	}
	
	@Test
	public void testFoundByRegistry() {
		
		List<ConfigurationUIFactory> list = ConfigurationUIRegistry.getInstance().getConfigurationUIFactories();
		assertTrue("There should be at least 1 item in the list",list.size()>=1);
		
		boolean found=false;
		for (ConfigurationUIFactory f : list) {
			if (f instanceof WorkbenchConfigurationUIFactory) {
				found=true;
				break;
			}
		}
		assertTrue("The WorkbenchConfigurationUIFactory was not found",found);
	} 
}
