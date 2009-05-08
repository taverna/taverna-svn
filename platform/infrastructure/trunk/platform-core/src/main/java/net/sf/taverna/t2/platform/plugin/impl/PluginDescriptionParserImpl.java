package net.sf.taverna.t2.platform.plugin.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.platform.plugin.PluginDescriptionParser;
import net.sf.taverna.t2.platform.plugin.PluginException;
import net.sf.taverna.t2.platform.plugin.PluginIdentifier;
import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;
import net.sf.taverna.t2.platform.pom.impl.PomParserImpl;
import net.sf.taverna.t2.platform.util.download.DownloadException;
import net.sf.taverna.t2.platform.util.download.DownloadManager;
import net.sf.taverna.t2.platform.util.download.URLMapper;

/**
 * Implementation of the plug-in description parser interface
 * 
 * @author Tom Oinn
 * 
 */
public class PluginDescriptionParserImpl implements PluginDescriptionParser {

	// A cache used to store plugin descriptions
	private Map<PluginIdentifier, PluginDescription> cache;

	// A download manager used to fetch and store the plug-in description
	// documents
	private DownloadManager manager;

	// The base location for cached plugin files
	private File baseLocation;

	private void check() throws PluginException {
		if (this.manager == null) {
			throw new PluginException(
					"Plugin parser has not been initialized correctly, download manager is null");
		}
		if (this.baseLocation == null) {
			throw new PluginException(
					"Plugin parser has not been initialized correctly, base file location is null");
		}
	}

	public PluginDescriptionParserImpl() {
		this.cache = new HashMap<PluginIdentifier, PluginDescription>();
	}

	/**
	 * The plug-in parser depends on a download manager which it can use to
	 * resolve plug-in description files and cache them to disk.
	 * 
	 * @param manager
	 */
	public void setDownloadManager(DownloadManager manager) {
		this.manager = manager;
	}

	/**
	 * The plug-in parser stores retrieved plug-in configuration files at
	 * [baseLocation]/groupId/pluginId-pluginVersion.xml, use this method to set
	 * the base directory used by the internal URL mapper.
	 * 
	 * @param baseLocation
	 */
	public void setBaseLocation(File baseLocation) {
		this.baseLocation = baseLocation;
	}

	/**
	 * Fetch or return a new PluginDescripton given an identifier and a list of
	 * plugin repositories. The repositories expect a directory structure that
	 * appears as &lt;groupId&gt;/&lt;pluginId&gt;-&lt;version&gt;.xml under the
	 * specified URL.
	 */
	public PluginDescription getDescription(PluginIdentifier id,
			List<URL> pluginRepositories) throws PluginException {
		check();
		if (cache.containsKey(id)) {
			// Return cached copy, ignoring repository locations
			return cache.get(id);
		} else {
			return constructAndReturn(id, pluginRepositories);
		}
	}

	private synchronized PluginDescription constructAndReturn(
			final PluginIdentifier id, List<URL> pluginRepositories)
			throws PluginException {
		// Re-check the cache!
		if (cache.containsKey(id)) {
			return cache.get(id);
		}
		// First look for local file based repositories, always check file
		// repositories before going to the network
		List<URL> remoteLocations = new ArrayList<URL>();
		String pathComponent = id.getGroupId() + "/" + id.getPluginId() + "-"
				+ id.getVersion() + ".xml";
		for (URL repository : pluginRepositories) {
			if (repository.getProtocol().equalsIgnoreCase("file")) {
				File repositoryFile = PomParserImpl.fileFromFileURL(repository);
				File pluginDescriptionFile = new File(repositoryFile, pathComponent);
				if (pluginDescriptionFile.exists()) {
					return descriptionFromFile(pluginDescriptionFile);
				}
			} else {
				// add path to URL and add to list of remote locations for later
				// use by the download manager
				try {
					remoteLocations.add(new URL(repository, pathComponent));
				} catch (MalformedURLException mue) {
					// Ignore this for now, suggests we can't create the remote
					// location URL so we'll skip it.
				}
			}
		}
			// Got here, so we didn't manage to find an appropriate local file
		// based plugin repository with the description already there.
		try {
			if (remoteLocations.isEmpty()) {
				throw new PluginException(
						"Unable to find plugin definition locally and no remote sources specified");
			}
			File pluginDescriptionFile = manager.getAsFile(remoteLocations,
					null, new PluginDescriptionURLMapper(id, baseLocation), 1);
			return descriptionFromFile(pluginDescriptionFile);
		} catch (DownloadException de) {
			// re-throw as plugin exception
			throw new PluginException(de);
		}

	}

	/**
	 * Given a file containing the XML representation of the plug-in description
	 * parse it and instantiate the description object. Wraps any JAXBException
	 * in a PluginException
	 * 
	 * @param pluginDescriptionFile
	 * @return
	 */
	private synchronized PluginDescription descriptionFromFile(
			File pluginDescriptionFile) throws PluginException {

		try {
			PluginDescription desc = PluginDescriptionXMLHandler
					.getDescription(pluginDescriptionFile);
			cache.put(desc.getId(), desc);
			return desc;
		} catch (JAXBException jaxbe) {
			throw new PluginException(jaxbe);
		}
	}

	private class PluginDescriptionURLMapper implements URLMapper {

		private File targetFile;

		public PluginDescriptionURLMapper(PluginIdentifier id, File baseLocation) {
			File directory = new File(baseLocation, "plugins/"
					+ id.getGroupId());
			directory.mkdirs();
			targetFile = new File(directory, id.getPluginId() + "-"
					+ id.getVersion() + ".xml");
		}

		public File map(URL source) {
			return targetFile;
		}

	}
}
