package net.sf.taverna.t2.workbench.configuration;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

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
	
	private static final String DUMMY_LIST_ENTRY="DUMMY_LIST_ENTRY";

	private static Logger logger = Logger.getLogger(ConfigurationManager.class);
	
	private File baseConfigLocation;

	private static ConfigurationManager configManager;

	private ConfigurationManager() {
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File config = new File(home,"conf");
		if (!config.exists()) {
			config.mkdir();
		}
		setBaseConfigLocation(config);
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
				Object value = propertyMap.get(key);
				if (value instanceof List) {
					List<Object> list = (List<Object>)value;
					while (list.size()<2) {
						list.add(DUMMY_LIST_ENTRY);
					}
				}
				propConfig.addProperty(key, value);
			}
			File configFile = new File(baseConfigLocation,generateFilename(configurable));
			logger.info("Storing configuration for "+configurable.getName()+" to "+configFile.getAbsolutePath());
			propConfig.save(configFile);
		} catch (ConfigurationException e) {
			throw new Exception("Failed to store the configuration: " + e);
		} catch (Exception e) {
			throw new Exception("Configuration storage failed: " + e);
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
			File configFile = new File(baseConfigLocation,generateFilename(configurable));
			if (configFile.exists()) {
				propertiesConfig.load(configFile);
				configurable.getPropertyMap().clear();
				Iterator keys = propertiesConfig.getKeys();
				while (keys.hasNext()) {
					Object next = keys.next();
					Object property = propertiesConfig
							.getProperty((String) next);
					if (property instanceof List) {
						List<Object> list = (List<Object>) property;
						while(list.contains(DUMMY_LIST_ENTRY)) {
							list.remove(DUMMY_LIST_ENTRY);
						}
					}
					configurable.getPropertyMap().put((String) next, property);
				}
			}
			else {
				logger.info("Config file for "+configurable.getName()+" not yet created. Creating with default values.");
				configurable.restoreDefaults();
				store(configurable);
			}
			
		} catch (Exception e) {
			logger.error("There was a error reading the configuration file for "+configurable.getName()+", using defaults",e);
			configurable.restoreDefaults();
		}
		return configurable.getPropertyMap();
	}

	private String generateFilename(Configurable configurable) {
		return configurable.getName()+"-"+configurable.getUUID() + ".config";
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
