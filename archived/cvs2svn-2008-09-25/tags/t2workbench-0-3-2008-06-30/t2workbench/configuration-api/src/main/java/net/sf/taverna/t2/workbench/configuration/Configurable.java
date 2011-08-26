package net.sf.taverna.t2.workbench.configuration;

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
	public Map<String,Object> getPropertyMap();
	/**
	 * @return a Map containing the default value/key pairs of the configured properties
	 */
	public Map<String,Object> getDefaultPropertyMap();
	/**
	 * @return a globally unique identifier that ensures that when stored this items configuration details will never clash with another
	 */
	public String getUUID();
	/**
	 * @return a friendly name for the item
	 */
	public String getName();
	/**
	 * @return a String defining the category of configurations that this item belongs to.
	 */
	public String getCategory();
	/**
	 * Restore the default property map
	 */
	public void restoreDefaults();
}
