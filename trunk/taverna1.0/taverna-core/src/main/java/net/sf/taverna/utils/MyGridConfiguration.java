package net.sf.taverna.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * myGrid configuration, such as services to load in the workbench or 
 * LSID provider to use. The configuration is in the file mygrid.properties 
 * in the classpath.
 * <p>
 * The recommended way to get the configuration is to call 
 * the static methods getProperties() and getProperty(). 
 * <p>
 * For backwards compatability, loadMygridProperties() 
 * loads the mygrid properties into the system properties. 
 * This was previously done by a static block in 
 * org.embl.ebi.escience.scuflui.workbench.Workbench. 
 * This method should be called by the main() methods to allow 
 * legacy classes to retrieve myGrid configuration.
 * 
 * @author Stian Soiland
 *
 */
public class MyGridConfiguration {
	
	private static Properties properties = null;
	private final static String PROPERTIES = "mygrid";

	private static Logger logger = Logger.getLogger(MyGridConfiguration.class);

	
	// Can't be instanciated, static methods only
	private MyGridConfiguration() {}
	
	/**
	 * Get the myGrid properties
	 * 
	 * @return A Properties instance loaded from mygrid.properties
	 */
	synchronized public static Properties getProperties() {
		if (properties == null) {
			ResourceBundle rb;
			try {
				rb = ResourceBundle.getBundle(PROPERTIES);
			} catch (Exception e) {
				logger.error("Can't find " + PROPERTIES + ".properties in classpath", e);
				System.out.println("Classpath is:");
				URLClassLoader l = (URLClassLoader) MyGridConfiguration.class.getClassLoader();
				for (URL u : l.getURLs()) {
					System.out.println(u);
				}
				return null;
			}
			Enumeration keys = rb.getKeys();
			properties = new Properties();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = (String) rb.getString(key);
				properties.put(key, value);
			}					
		}
		return properties;
	}
	
	/**
	 * 
	 * Look up a myGrid property.
	 * <p>
	 * This is a shorthand for getProperties().getProperty(key).
	 * 
	 * @param key 
	 * @return value 
	 */
	public static String getProperty(String key) {
		Properties props = getProperties();
		if (props == null) {
			logger.warn("mygrid properties not found, returning null for " + key);
			return null;
		}
		return props.getProperty(key);
	}

	/**
	 * Load the myGrid properties and store them into the System properties.
	 * <p>
	 * This is provided for backwards compatibility <em>only</em>, as old code relies 
	 * on System.getProperties() to be prepopulated with the myGrid configuration.
	 * <p>
	 * Do not rely on properties to have been loaded globally, instad use 
	 * MyGridConfiguration.getProperties()
	 *
	 * @see MyGridConfiguration.getProperties()
	 *
	 */
	@Deprecated
	synchronized public static void loadMygridProperties() {
		Properties myGridProps = MyGridConfiguration.getProperties();
		if (myGridProps == null) {
			logger.warn("mygrid properties not found, expect NullPointerExceptions");
		}
		Properties sysProps = System.getProperties();
		sysProps.putAll(myGridProps);
	}

}
