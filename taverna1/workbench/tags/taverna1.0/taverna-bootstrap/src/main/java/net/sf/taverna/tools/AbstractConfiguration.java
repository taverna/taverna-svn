package net.sf.taverna.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Base class for configuration files.
 * Subclass specifies the configuration file. This class handled checking the $taverna.home/conf and $taverna.startup/conf
 * locations for the configuration file in that order.
 * 
 * @author Stuart Owen
 *
 */
public abstract class AbstractConfiguration {

	protected String getTavernaStartup() {
		return System.getProperty("taverna.startup");
	}
	
	protected String getTavernaHome() {
		return System.getProperty("taverna.home");
	}
	
	protected Properties properties;
	
	/**
	 * Returns a File object to the configuration file or null if it cannot be found.
	 * 
	 * @return
	 */
	protected File getFile() {
		String home=getTavernaHome();
		String startup=getTavernaStartup();
		String conf = getConfFolder();
		File result=null;
		if (home!=null) {
			File file = new File(new File(home, conf), getConfigurationFilename());
			if (file.exists()) {
				result=file;
			}
		}
		if (result==null && startup!=null) {
			File file = new File(new File(startup, conf), getConfigurationFilename());
			if (file.exists()) {
				result=file;
			}
		}
		
		return result;
	}
	
	/**
	 * Initialises and provides access to the list of Properties.
	 * @return
	 */
	public Properties getProperties() {
		if (properties==null) initialiseProperties();
		return properties;
	}
	
	protected void initialiseProperties() {
		InputStream is = getInputStream();
		if (is!=null) {
			try {
				properties=new Properties();
				properties.load(is);
				if (isSystemOverrided()) properties.putAll(System.getProperties());
			}  catch (IOException e) {
				errorLog("An error occurred trying to load the " + getConfigurationFilename() + " file",e);
			}
		}
	}
	
	/**
	 * Return an input stream to the configuration file, or null if it can't be found
	 * @return
	 */
	protected InputStream getInputStream() {
		InputStream result = null;
		File propertiesFile = getFile();
		if (propertiesFile!=null) {
			try {
				result=new FileInputStream(propertiesFile);
			} catch (FileNotFoundException e) {
				errorLog("Unable to find "+getConfigurationFilename(),e);
			}
		}
		else {
			errorLog("Unable to determine file for "+getConfigurationFilename(),null);
		}
		return result;
	}
	
	protected String getConfFolder() {
		return "conf";
	}
	
	/**
	 * Indicates whether the properties should be overridden with system properties. Defaults to false.
	 * @return
	 */
	protected boolean isSystemOverrided() {
		return false;
	}
	
	protected void errorLog(String message, Throwable exception) {
		System.out.println(message);
		if (exception!=null) {
			exception.printStackTrace();
		}
		
	}
	
	/**
	 * Clears the properties causing them to reinitialise when next accessed.
	 *
	 */
	public void flush() {
		properties=null;
	}
	
	protected abstract String getConfigurationFilename();
}
