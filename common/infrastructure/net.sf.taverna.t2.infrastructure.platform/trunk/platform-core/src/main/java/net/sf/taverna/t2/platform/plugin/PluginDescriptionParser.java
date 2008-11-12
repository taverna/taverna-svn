package net.sf.taverna.t2.platform.plugin;

import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;

/**
 * Parser and loader used to obtain PluginDescription instances from
 * PluginIdentifier and list of plugin repositories
 * 
 * @author Tom Oinn
 * 
 */
public interface PluginDescriptionParser {

	/**
	 * Get a plugin description given the supplied identifier and list of
	 * repositories. The repositories may be ignored if the description has
	 * previously been downloaded and cached.
	 * 
	 * @param id
	 *            the PluginIdentifier for this plug-in description request
	 * @param pluginRepositories
	 *            a list of URLs pointing to plug-in repositories
	 * @return a PluginDescription object
	 * @throws PluginException
	 *             if any unrecoverable problems occur during the fetch or parse
	 *             of the plug-in description file.
	 */
	public PluginDescription getDescription(PluginIdentifier id,
			List<URL> pluginRepositories) throws PluginException;

}
