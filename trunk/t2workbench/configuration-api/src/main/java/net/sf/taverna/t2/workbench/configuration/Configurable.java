package net.sf.taverna.t2.workbench.configuration;

import java.util.List;
import java.util.Map;

/**
 * An interface that defines an Object as being configurable.
 * It supports the core properties that allows this items configuration to be stored and re-populated by the ConfigurationManager
 *
 * @author Stuart Owen
 *
 */
public interface Configurable {
	
	/**
	 * @return a Map containing the value/key pairs of the configured properties
	 */
	Map<String,String> getPropertyMap();
	/**
	 * @return a Map containing the default value/key pairs of the configured properties
	 */
	Map<String,String> getDefaultPropertyMap();
	/**
	 * @return a globally unique identifier that ensures that when stored this items configuration details will never clash with another
	 */
	String getUUID();
	/**
	 * @return a friendly name for the item
	 */
	String getName();
	/**
	 * @return a String defining the category of configurations that this item belongs to.
	 */
	String getCategory();
	/**
	 * Restore the default property map
	 */
	void restoreDefaults();
	
	/**
	 * Looks up the property for the given key. 
	 * <br>
	 * Using this method is preferable to using the property map directly.
	 * @param key
	 * @return the String represented by the key, the default value, or null
	 */
	String getProperty(String key);
	
	/**
	 * Overwrites or applies a new value against the given key in the property map.
	 * <br>
	 * Setting a value to null is equivalent to calling this{@link #deleteProperty(String)}
	 * <br>
	 * If the value is new, or changed, the the property map is stored.
	 * <br>
	 * Using this method is preferable to using the property map directly.
	 * @param key
	 * @param value
	 */
	void setProperty(String key, String value);
	
	/**
	 * Deletes a property value for a given key.
	 * <br>
	 * Subsequent calls to this{@link #getProperty(String)} will return null.
	 * @param key
	 */
	void deleteProperty(String key);
	
	public List<String> getPropertyStringList(String key);
	
	public void setPropertyStringList(String key, List<String>value);
}
