package net.sf.taverna.t2.workbench.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Hansles the configuration for a {@link Configurable} object
 * 
 * @author Ian Dunlop
 * @author Stuart Owen
 * 
 */
public class ConfigurationManager {

	private File baseConfigLocation;

	private static ConfigurationManager configManager;

	private Map<String, PropertiesConfiguration> propertiesConfigMap;

	private ConfigurationManager() {
		//TODO does this need a config file itself?
		propertiesConfigMap = new HashMap<String, PropertiesConfiguration>();
	}

	public void store(Configurable configurable) {
		// write out the propertiesConfig somewhere, ie get from the Map based
		// on the UUID and store it
	}

	/**
	 * Loads the configuration details from disk or from memory
	 * 
	 * @param configurable
	 * @return
	 * @throws Exception
	 *             if there are no configuration details available
	 */
	public PropertiesConfiguration populate(Configurable configurable)
			throws Exception {
		// get config from disk or from the Map if available
		PropertiesConfiguration propertiesConfiguration = propertiesConfigMap
				.get(configurable.getUUID());
		if (propertiesConfiguration != null) {
			return propertiesConfiguration;
		} else {
			// load from disk in the baseConfigLocation and return
			try {
				return null;
			} catch (Exception e) {
				throw new Exception("No properties file exists");
			}
		}

	}

	/**
	 * Get an instance of the {@link ConfigurationManager}
	 * 
	 * @return
	 */
	public static ConfigurationManager getInstance() {
		if (configManager == null) {
			configManager = new ConfigurationManager();
			return configManager;
		} else {
			return configManager;
		}
	}

	/**
	 * Where the config files are being stored
	 * 
	 * @return
	 * @throws Exception
	 */
	public File getBaseConfigLocation() throws Exception {
		if (baseConfigLocation != null) {
			return baseConfigLocation;
		} else {
			throw new Exception("Set location first");
		}
	}

	/**
	 * Where should the config files be stored
	 * 
	 * @return
	 * @throws Exception
	 */
	public void setBaseConfigLocation(File baseConfigLocation) {
		// TODO if this is a different place than before then copy all the
		// config files to this new place
		this.baseConfigLocation = baseConfigLocation;
	}

}
