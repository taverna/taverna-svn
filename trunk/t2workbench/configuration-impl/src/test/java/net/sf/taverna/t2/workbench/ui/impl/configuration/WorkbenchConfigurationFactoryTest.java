package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.util.List;

import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIRegistry;
import net.sf.taverna.t2.workbench.ui.impl.configuration.WorkbenchConfigurationUIFactory;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
public class WorkbenchConfigurationFactoryTest {
	
	@Before
	public void setup() {
		ConfigurationManager manager = ConfigurationManager.getInstance();
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
