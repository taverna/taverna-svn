/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

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
	
	protected static final String DELETED_VALUE_CODE="~~DELETED~~";
	
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
	 * <br>
	 * Default values are not stored within the file, but only those that have been changed or deleted.
	 * 
	 * @param configurable
	 * @throws Exception 
	 */
	public void store(Configurable configurable) throws Exception {
		try {

			Map<String, String> propertyMap = configurable.getInternalPropertyMap();
			Properties props = new Properties();
		    for (String key : propertyMap.keySet()) {
		    	if (!propertyMap.get(key).equals(configurable.getDefaultProperty(key))) {
		    		props.put(key, propertyMap.get(key));
		    	}
		    }
			File configFile = new File(baseConfigLocation,generateFilename(configurable));
			logger.info("Storing configuration for "+configurable.getName()+" to "+configFile.getAbsolutePath());
			props.store(new FileOutputStream(configFile), "");
		} catch (Exception e) {
			throw new Exception("Configuration storage failed: " + e);
		}
	}
	
	

	/**
	 * Loads the configuration details from disk or from memory and populates the provided Configurable
	 * 
	 * @param configurable
	 * @return
	 * @throws Exception
	 *             if there are no configuration details available
	 */
	public void populate(Configurable configurable)
			throws Exception {
		try {
			File configFile = new File(baseConfigLocation,generateFilename(configurable));
			if (configFile.exists()) {
				Properties props = new Properties();
				props.load(new FileInputStream(configFile));
				configurable.clear();
				for (Object key : props.keySet()) {
					configurable.setProperty(key.toString(), props.getProperty(key.toString()));
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
	}

	protected String generateFilename(Configurable configurable) {
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
