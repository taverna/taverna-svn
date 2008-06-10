package net.sf.taverna.t2.workbench.configuration;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

public class ConfigurationUIRegistry extends SPIRegistry<ConfigurationUIFactory>{

	private static ConfigurationUIRegistry instance = new ConfigurationUIRegistry();

	public static ConfigurationUIRegistry getInstance() {
		return instance;
	}
	
	private ConfigurationUIRegistry() {
		super(ConfigurationUIFactory.class);
	}
	
	public List<ConfigurationUIFactory> getConfigurationUIFactoriesForConfigurable(Configurable configurable) {
		List<ConfigurationUIFactory> result = new ArrayList<ConfigurationUIFactory>();
		for (ConfigurationUIFactory factory : getConfigurationUIFactories()) {
			if (factory.canHandle(configurable.getUUID())) {
				result.add(factory);
			}
		}
		return result;
	}
	
	public List<ConfigurationUIFactory> getConfigurationUIFactories() {
		return getInstances();
	}
	
	

}
