package net.sf.taverna.t2.workbench.configuration;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * Handles the configuration for a {@link Configurable} object
 * 
 * @author Ian Dunlop
 * @author Stuart Owen
 * 
 */
public class ConfigurationManager {

	private static Logger logger = Logger.getLogger(ConfigurationManager.class);
	
	private File baseConfigLocation;

	private static ConfigurationManager configManager;

//	private Map<String, PropertiesConfiguration> propertiesConfigMap;

	private ConfigurationManager() {
		// TODO does this need a config file itself?
		//propertiesConfigMap = new HashMap<String, PropertiesConfiguration>();
	}

	/**
	 * Write out the properties configuration to disk based on the UUID of the
	 * {@link Configurable}
	 * 
	 * @param configurable
	 * @throws Exception 
	 */
	public void store(Configurable configurable) throws Exception {
		try {
			Map<String, Object> propertyMap = configurable.getPropertyMap();
			PropertiesConfiguration propConfig = new PropertiesConfiguration();
			for (String key : propertyMap.keySet()) {
				propConfig.addProperty(key, propertyMap.get(key));
			}
			File configFile = new File(baseConfigLocation, configurable.getUUID()
					+ ".config");
			logger.info("Storing configuration for "+configurable.getName()+" to "+configFile.getAbsolutePath());
			propConfig.save(configFile);
		} catch (ConfigurationException e) {
			throw new Exception("Failed to store the configuration: " + e);
		} catch (Exception e) {
			throw new Exception("Configuration storage failed: " + e);
		}
	}

	private void storeObject(PropertiesConfiguration propConfig, Object object)
			throws Exception {
		// TODO don't think this is needed, something similar might be needed on
		// the loading of the properties
		if (object instanceof Integer) {

		} else if (object instanceof Object[]) {

		} else if (object instanceof String) {

		} else if (object instanceof List) {

		} else if (object instanceof Long) {

		} else if (object instanceof Boolean) {

		} else if (object instanceof Byte) {

		} else if (object instanceof Short) {

		} else if (object instanceof Float) {

		} else if (object instanceof Double) {

		} else if (object instanceof BigDecimal) {

		} else {
			throw new Exception("Type of property not recognised");
		}
	}

	/**
	 * Loads the configuration details from disk or from memory
	 * 
	 * @param configurable
	 * @return
	 * @throws Exception
	 *             if there are no configuration details available
	 */
	public Map<String, Object> populate(Configurable configurable)
			throws Exception {
		try {
			PropertiesConfiguration propertiesConfig = new PropertiesConfiguration();
			propertiesConfig.load(new File(baseConfigLocation, configurable
					.getUUID()
					+ ".config"));
			configurable.getPropertyMap().clear();
			Iterator keys = propertiesConfig.getKeys();
			while (keys.hasNext()) {
				Object next = keys.next();
				Object property = propertiesConfig
						.getProperty((String) next);
				configurable.getPropertyMap().put((String) next, property);
			}
			
		} catch (Exception e) {
			logger.info("No properties found for "+configurable.getName()+", storing defailts");
			configurable.restoreDefaults();
			store(configurable);
		}
		return configurable.getPropertyMap();
	}

	private Map<String, Object> restoreObjects(
			PropertiesConfiguration propertiesConfiguration) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		Iterator keys = propertiesConfiguration.getKeys();
		while (keys.hasNext()) {
			Object next = keys.next();
			Object property = propertiesConfiguration
					.getProperty((String) next);
			propMap.put((String) next, property);
		}
		return propMap;
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
	
	public boolean isBaseLocationSet() {
		return baseConfigLocation!=null;
	}

	/**
	 * Where the config files are being stored
	 * 
	 * @return
	 * @throws Exception
	 */
	public File getBaseConfigLocation() throws Exception {
		if (isBaseLocationSet()) {
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
