package net.sf.taverna.t2.workbench.ui.impl.configuration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.UUID;

import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIRegistry;

import org.junit.Before;
import org.junit.Test;


public class WorkbenchConfigurationFactoryTest {
	
	@Before
	public void setup() throws Exception {
		ConfigurationManager manager = ConfigurationManager.getInstance();
		File f = new File(System.getProperty("java.io.tmpdir"));
		File configTestsDir = new File(f,"configTests");
		if (!configTestsDir.exists()) configTestsDir.mkdir();
		File d = new File(configTestsDir,UUID.randomUUID().toString());
		d.mkdir();
		manager.setBaseConfigLocation(d);
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
