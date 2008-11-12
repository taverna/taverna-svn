package net.sf.taverna.t2.platform.plugin;

import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;

/**
 * The plug-in manager uses raven and the download manager along with the
 * plug-in parser to manage a set of installed plug-in packages.
 * 
 * @author Tom Oinn
 * 
 */
public interface PluginManager {

	/**
	 * Return or construct a class loader containing all jar and artifact
	 * entries from the specified plug-in identifier, fetching the description
	 * for the identifier first if required and also fetching any required code
	 * artifacts, either maven 2 or raw jar files.
	 * <p>
	 * This method is also used to install a plug-in without activating it,
	 * simply ignore the returned class loader.
	 * 
	 * @param id
	 *            the plug-in identifier to obtain a classloader for
	 * @param pluginRepositories
	 *            a list of URLs to remote or local plug-in description
	 *            repositories to be used if the specified plug-in doesn't exist
	 *            yet.
	 * @return a ClassLoader providing access to the plugin's classes
	 * @throws PluginException
	 *             if the plug-in cannot be loaded, parsed or if any
	 *             dependencies are unavailable.
	 */
	ClassLoader getPluginClassLoader(PluginIdentifier id,
			List<URL> pluginRepositories) throws PluginException;

	/**
	 * If the supplied object was loaded through a plug-in based class loader
	 * then return the plug-in identifier for that loader. If the object was not
	 * loaded by the plug-in manager then this method returns null. This method
	 * is intended for use by serialization frameworks that need to store the
	 * plug-in information when automatically serializing an object instance.
	 * 
	 * @return the PluginIdenfier if the object was loaded through a plug-in
	 *         based class loader, or null otherwise
	 */
	PluginIdentifier definingPlugin(Object o);

	/**
	 * Get an SPI registry driven from the set of active plug-ins within this
	 * plug-in manager. The SPI will be created on demand or returned from a
	 * cache, there is always a one to one relationship between SPI registry and
	 * SPI class for a given plugin manager
	 */
	<SPIType extends Object> SPIRegistry<SPIType> getSPIRegistry(
			Class<SPIType> spiClass);

	/**
	 * Activate a plugin, this will download and construct the plugin if
	 * required and activate it. If the plugin is already installed and active
	 * this has no effect. As with other methods taking remote repository
	 * arguments these arguments will be ignored unless the download mechanism
	 * is used.
	 * <p>
	 * Plug-in activation can cause changes to SPI registry membership, these
	 * will be notified through any listeners attached to SPI registries bound
	 * to this plug-in manager, this is the correct way to be notified of
	 * plug-in changes.
	 * 
	 * @throws PluginException
	 *             if the plug-in cannot be loaded, parsed or if any
	 *             dependencies are unavailable.
	 */
	void activatePlugin(PluginIdentifier id, List<URL> pluginRepositories)
			throws PluginException;

	/**
	 * De-activate a plug-in, this doesn't unload any classes and neither does
	 * it remove the downloaded files for the specified plug-in. All it does is
	 * cause any bound SPI registries to remove entries originating from the
	 * specified plug-in and notify any listeners of the change.
	 * <p>
	 * If the specified plug-in has not been installed or is installed but not
	 * active this method does nothing.
	 */
	void deactivatePlugin(PluginIdentifier id);

	/**
	 * Get a list of active plug-ins, the list returned is an unmodifiable copy
	 * of any internal representation and cannot be used to modify the state of
	 * the plug-in manager.
	 * 
	 * @return a list of PluginDescription objects describing each active
	 *         plug-in. Use the getId() method on the description object to get
	 *         the plugin identifier if required.
	 */
	List<PluginDescription> getActivePluginList();

	/**
	 * Get a list of all installed plug-ins, whether activated or not. The list
	 * returned is an unmodifiable copy of any internal representation and
	 * cannot be used to modify the state of the plug-in manager.
	 * 
	 * @return a list of PluginDescription objects describing each plug-in. Use
	 *         the getId() method on the description object to get the plugin
	 *         identifier if required.
	 */
	List<PluginDescription> getCompletePluginList();

}
