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
/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: PluginManager.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/11/03 16:27:57 $
 *               by   $Author: stain $
 * Created on 23 Nov 2006
 *****************************************************************/
package net.sf.taverna.raven.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.appconfig.bootstrap.Bootstrap;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.plugins.Plugin.PluginId;
import net.sf.taverna.raven.plugins.event.PluginEvent;
import net.sf.taverna.raven.plugins.event.PluginListener;
import net.sf.taverna.raven.plugins.event.PluginManagerEvent;
import net.sf.taverna.raven.plugins.event.PluginManagerListener;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.x2008.xml.plugins.DescribedPlugin;
import net.sf.taverna.x2008.xml.plugins.PluginDocument;
import net.sf.taverna.x2008.xml.plugins.Plugins;
import net.sf.taverna.x2008.xml.plugins.PluginsDocument;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author David Withers
 * @author Stian Soiland-Reyes
 */
public class PluginManager implements PluginListener {

	private static final String PLUGINS_XML = "plugins.xml";

	private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema-instance";

	private static final String SCHEMA_LOCATION = "http://taverna.sourceforge.net/2008/xml/plugins/plugins-2008-10-16.xsd";

	private static final String PLUGINS_NS = "http://taverna.sf.net/2008/xml/plugins";

	private ApplicationRuntime appRuntime = ApplicationRuntime.getInstance();

	private static Log logger = Log.getLogger(PluginManager.class);

	private static PluginManager instance;

	private static Repository repository;

	private static List<PluginManagerListener> pluginManagerListeners = new ArrayList<PluginManagerListener>();

	private File pluginsDir;
	private URL defaultPluginsDir; // defaults are read from
	// $taverna.startup/plugins

	private List<PluginSite> pluginSites = new ArrayList<PluginSite>();

	private List<Plugin> plugins = new ArrayList<Plugin>();

	private List<Plugin> updatedPlugins = new ArrayList<Plugin>();

	private Profile profile = ProfileFactory.getInstance().getProfile();

	/**
	 * Constructs an instance of PluginManager.
	 * 
	 */
	private PluginManager() {
		pluginsDir = appRuntime.getPluginsDir();
		defaultPluginsDir = appRuntime.getDefaultPluginsDir();
		if (pluginsDir != null) {
			initializePluginSites();
			initializePlugins();
		}
	}

	/**
	 * Returns the singleton instance of the PluginManager.
	 * 
	 * @return the singleton instance of the PluginManager
	 */
	public static PluginManager getInstance() {
		if (instance == null) {
			instance = new PluginManager();
		}
		return instance;
	}

	public Repository getRepository() {
		return repository;
	}

	public static void setRepository(Repository repository) {
		PluginManager.repository = repository;
	}

	/**
	 * Returns the plugins.
	 * 
	 * @return the plugins
	 */
	public List<Plugin> getPlugins() {
		return plugins;
	}

	public void addPlugin(final Plugin plugin) {
		if (!plugins.contains(plugin)) {
			plugins.add(plugin);
			sortPlugins();
			for (String repositoryURL : plugin.getRepositories()) {
				try {
					if (repository instanceof LocalRepository) {
						// fix for TAV-684, but didn't want to change the
						// Repository interface API.
						((LocalRepository) repository)
								.prependRemoteRepository(new URL(repositoryURL));
					} else {
						repository.addRemoteRepository(new URL(repositoryURL));
					}
				} catch (MalformedURLException e) {
					logger.warn("Invalid remote repository URL - "
							+ repositoryURL);
				}
			}
			for (Artifact artifact : plugin.getProfile().getArtifacts()) {
				repository.addArtifact(artifact);
				if (plugin.getProfile().getSystemArtifacts().contains(artifact)) {
					profile.addSystemArtifact(artifact);
				}
			}

			if (!checkPluginCompatibility(plugin)) {

				if (plugin.isEnabled()) {
					plugin.setEnabled(false);
					firePluginIncompatibleEvent(new PluginManagerEvent(this,
							plugin, plugins.indexOf(plugin)));
				}
			}

			repository.update();

			if (plugin.isEnabled()) {
				enablePlugin(plugin);
			}
			firePluginAddedEvent(new PluginManagerEvent(this, plugin, plugins
					.indexOf(plugin)));
			plugin.addPluginListener(this);
		}
	}

	/**
	 * Returns a list of all currently installed and enabled plugins that would
	 * become incompatible with the version String supplied. If teh
	 * omitDisbaledPlugins flag is set to true, then only enabled plugins will
	 * be returned
	 * 
	 * @param version
	 *            - the version String to check plugins against
	 * @param omitDisabledPlugins
	 *            - flag to indicate that disabled plugins should be ignored
	 * @return
	 */
	public List<Plugin> getIncompatiblePlugins(String version,
			boolean omitDisabledPlugins) {
		List<Plugin> result = new ArrayList<Plugin>();
		for (Plugin plugin : getPlugins()) {
			if (omitDisabledPlugins && !plugin.isEnabled())
				continue;
			if (!checkPluginCompatibilityWithProfile(plugin, version)) {
				result.add(plugin);
			}
		}
		return result;
	}

	/**
	 * Checks a plugins compatiblilty with the current version and returns
	 * whether it is compatible. Sets the plugins compatibility flag accordingly
	 * 
	 * @param plugin
	 * @return
	 */
	private boolean checkPluginCompatibility(Plugin plugin) {
		String profileVersion = profile.getVersion();
		boolean result;
		result = checkPluginCompatibilityWithProfile(plugin, profileVersion);
		plugin.setCompatible(result);
		return result;
	}

	private boolean checkPluginCompatibilityWithProfile(Plugin plugin,
			String version) {
		for (String v : plugin.getVersions()) {
			if (version.startsWith(v)) {
				return true;
			}
		}
		return false;
	}

	private void firePluginIncompatibleEvent(PluginManagerEvent event) {
		for (PluginManagerListener listener : pluginManagerListeners) {
			listener.pluginIncompatible(event);
		}
	}

	public void removePlugin(Plugin plugin) {
		// might need a pop up to warn if there are any system artifacts -
		// restart might be required
		if (updatedPlugins.contains(plugin))
			updatedPlugins.remove(plugin);

		if (plugins.contains(plugin)) {
			if (plugin.isEnabled()) {
				disablePlugin(plugin);
			}
			int index = plugins.indexOf(plugin);
			plugins.remove(plugin);
			firePluginRemovedEvent(new PluginManagerEvent(this, plugin, index));
			plugin.removePluginListener(this);
		}
	}

	private void enablePlugin(Plugin plugin) {
		enablePluginAndDeps(plugin, new HashSet<Plugin>());
		savePlugins();
	}

	private void enablePluginAndDeps(Plugin plugin,
			HashSet<Plugin> alreadyEnabled) {
		if (alreadyEnabled.contains(plugin)) {
			return;
		}
		alreadyEnabled.add(plugin);

		// Also enable dependencies
		pluginDepLoop: for (PluginId pluginDep : plugin.getPluginDependencies()) {
			for (Plugin updatedDep : plugins) {
				if (satisfiesDependency(updatedDep, pluginDep)) {
					enablePluginAndDeps(updatedDep, alreadyEnabled);
					continue pluginDepLoop;
				}
			}
			logger.warn("Plugin " + plugin + " depends on unknown plugin "
					+ pluginDep);
			// TODO: Set disabled? (but without invoking the event handler
			// again!)

		}

		if (! plugins.contains(plugin)) {
			return;
		}
		for (Artifact artifact : plugin.getProfile().getArtifacts()) {
			profile.addArtifact(artifact);
			if (plugin.getProfile().getSystemArtifacts().contains(artifact)) {
				try {
					Bootstrap.addSystemArtifact(artifact.getGroupId(), artifact
							.getArtifactId(), artifact.getVersion());
				} catch (MalformedURLException e) {
					logger.error(
							"Error composing url for artifact " + artifact, e);
				}
			}
		}
		
	}

	private void disablePlugin(Plugin plugin) {
		// might need a pop up to warn if there are any system artifacts -
		// restart might be required
		if (plugins.contains(plugin)) {
			for (Artifact artifact : plugin.getProfile().getArtifacts()) {
				profile.removeArtifact(artifact);
			}
			savePlugins();
		}
	}

	public void savePlugins() {
		PluginsDocument pluginsDoc = PluginsDocument.Factory
				.newInstance(makeXMLOptions());
		Plugins pluginsDescr = pluginsDoc.addNewPlugins();

		for (Plugin plugin : plugins) {
			plugin.populateXMLBean(pluginsDescr.addNewPlugin());
		}
		File pluginsFile = new File(pluginsDir, PLUGINS_XML);
		try {
			if (!pluginsDoc.validate(makeXMLOptions())) {
				logger.error("Saved invalid XML to " + pluginsFile);
			}
			pluginsDoc.save(pluginsFile, makeXMLOptions());
		} catch (IOException e) {
			logger.error("Error writing plugins to " + pluginsFile.getPath());
		}

	}

	/**
	 * Returns the pluginSites.
	 * 
	 * @return the pluginSites
	 */
	public List<PluginSite> getPluginSites() {
		return pluginSites;
	}

	/**
	 * Adds a <code>PluginSite</code>.
	 * 
	 * @param pluginSite
	 *            the <code>PluginSite</code> to add
	 */
	public void addPluginSite(PluginSite pluginSite) {
		pluginSites.add(pluginSite);
		savePluginSites();
	}

	/**
	 * Removes a <code>PluginSite</code>.
	 * 
	 * @param pluginSite
	 *            the <code>PluginSite</code> to remove
	 */
	public void removePluginSite(PluginSite pluginSite) {
		pluginSites.remove(pluginSite);
		savePluginSites();
	}

	public void savePluginSites() {
		Element pluginSitesElement = new Element("pluginSites");
		for (PluginSite pluginSite : pluginSites) {
			if (!(pluginSite instanceof TavernaPluginSite))
				pluginSitesElement.addContent(pluginSite.toXml());
		}
		File pluginSitesFile = new File(pluginsDir, "plugin-sites.xml");
		try {
			Writer writer = new FileWriter(pluginSitesFile);
			new XMLOutputter(Format.getPrettyFormat()).output(
					pluginSitesElement, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error("Error writing plugin sites to "
					+ pluginSitesFile.getPath());
		}
	}

	/**
	 * Returns all the <code>Plugin</code>s available from the
	 * <code>PluginSite</code>.
	 * 
	 * @param pluginSite
	 * @return all the <code>Plugin</code>s available from the
	 *         <code>PluginSite</code>
	 */
	@SuppressWarnings("unchecked")
	public List<Plugin> getPluginsFromSite(PluginSite pluginSite) {
		List<Plugin> plugins = new ArrayList<Plugin>();
		HttpClient client = new HttpClient();
		setProxy(client);

		if (pluginSite.getUrl() == null) {
			logger.error("No plugin site URL" + pluginSite);
			return plugins;
		}

		URI pluginSiteURI;
		try {
			pluginSiteURI = pluginSite.getUrl().toURI();
		} catch (URISyntaxException e) {
			logger.error("Invalid plugin site URL" + pluginSite);
			return plugins;
		}

		URI pluginsXML = pluginSiteURI.resolve("pluginlist.xml");

		HttpMethod getPlugins = new GetMethod(pluginsXML.toString());
		int statusCode;
		try {
			statusCode = client.executeMethod(getPlugins);
		} catch (UnknownHostException e) {
			logger.warn("Could not fetch plugins from non-existing host", e);
			return plugins;
		} catch (IOException e) {
			logger.warn("Could not fetch plugins " + pluginsXML, e);
			return plugins;
		}
		if (statusCode != HttpStatus.SC_OK) {
			logger.warn("HTTP status " + statusCode + " while getting plugins "
					+ pluginsXML);
			return plugins;
		}

		Document pluginsDocument;
		try {
			pluginsDocument = new SAXBuilder().build(getPlugins
					.getResponseBodyAsStream());
		} catch (JDOMException e) {
			logger.warn("Could not parse plugins " + pluginsXML, e);
			return plugins;
		} catch (IOException e) {
			logger.warn("Could not read plugins " + pluginsXML, e);
			return plugins;
		}
		List<Element> pluginList = pluginsDocument.getRootElement()
				.getChildren("plugin");
		for (Element pluginElement : pluginList) {
			URI pluginUri;
			try {
				pluginUri = pluginSiteURI.resolve(pluginElement.getTextTrim());
			} catch (IllegalArgumentException ex) {
				logger
						.warn("Invalid plugin URI "
								+ pluginElement.getTextTrim());
				continue;
			}

			HttpMethod getPlugin = new GetMethod(pluginUri.toString());
			try {
				statusCode = client.executeMethod(getPlugin);
			} catch (IOException e) {
				logger.warn("Could not fetch plugin " + pluginUri, e);
				continue;
			}
			if (statusCode != HttpStatus.SC_OK) {
				logger.warn("HTTP status " + statusCode
						+ " while getting plugin " + pluginUri);
				continue;
			}

			Plugin plugin;
			try {
				XmlOptions xmlOptions = makeXMLOptions();
				xmlOptions.setLoadReplaceDocumentElement(new QName(PLUGINS_NS,
				"plugin"));
				PluginDocument pluginDoc = PluginDocument.Factory
						.parse(getPlugin.getResponseBodyAsStream(), xmlOptions);
				plugin = Plugin.fromXmlBean(pluginDoc.getPlugin());
			} catch (XmlException e1) {
				logger.warn("Could not parse plugin " + pluginUri, e1);
				continue;
			} catch (IOException e1) {
				logger.warn("Could not read plugin " + pluginUri, e1);
				continue;
			}
			if (checkPluginCompatibility(plugin)) {
				plugins.add(plugin);
				logger.debug("Added plugin from " + pluginUri);
			} else {
				logger
						.debug("Plugin deemed incompatible so not added to available plugin list");
			}
		}
		logger.info("Added plugins from " + pluginSiteURI);
		return plugins;
	}

	/**
	 * Returns all the <code>Plugin</code>s available from the
	 * <code>PluginSite</code> that haven't already been installed.
	 * 
	 * @param pluginSite
	 * @return all the uninstalled <code>Plugin</code>s from the
	 *         <code>PluginSite</code>
	 */
	public List<Plugin> getUninstalledPluginsFromSite(PluginSite pluginSite) {
		List<Plugin> uninstalledPlugins = new ArrayList<Plugin>();
		List<Plugin> pluginsFromSite = getPluginsFromSite(pluginSite);
		for (Plugin plugin : pluginsFromSite) {
			if (!plugins.contains(plugin)) {
				if (uninstalledPlugins.contains(plugin)) {
					int index = uninstalledPlugins.indexOf(plugin);
					Plugin uninstalledPlugin = uninstalledPlugins.get(index);
					if (uninstalledPlugin.compareVersion(plugin) < 0) {
						uninstalledPlugins.remove(index);
						uninstalledPlugins.add(plugin);
					}
				} else {
					uninstalledPlugins.add(plugin);
				}
			}
		}
		return uninstalledPlugins;
	}

	/**
	 * Returns the update for the plugin;
	 * 
	 * @param plugin
	 * @return
	 */
	public Plugin getUpdate(Plugin plugin) {
		synchronized (updatedPlugins) {
			return updatedPlugins.get(updatedPlugins.indexOf(plugin));
		}
	}

	/**
	 * If an update is available, removes the plugin an installs the update.
	 * 
	 * @param plugin
	 *            the plugin to update
	 */
	public void updatePlugin(Plugin plugin) {
		updatePluginAndDeps(plugin, new HashSet<Plugin>());
	}

	private void updatePluginAndDeps(Plugin plugin, HashSet<Plugin> alreadyUpdated) {
		if (alreadyUpdated.contains(plugin)) {
			return;
		}
		alreadyUpdated.add(plugin);
		// Also update any available dependencies
		for (PluginId pluginDep : plugin.getPluginDependencies()) {
			for (Plugin updatedDep : updatedPlugins) {
				if (satisfiesDependency(updatedDep, pluginDep)) {
					updatePluginAndDeps(updatedDep, alreadyUpdated);
				}
			}
		}
		
		if (isUpdateAvailable(plugin)) {
			synchronized (updatedPlugins) {
				Plugin newPlugin = getUpdate(plugin);
				updatedPlugins.remove(newPlugin);
				newPlugin.setEnabled(true); // enable newly updated plugin
				removePlugin(plugin);
				addPlugin(newPlugin);
				savePlugins();
			}
		}

		
	}

	private boolean satisfiesDependency(Plugin updatedDep, PluginId pluginDep) {
		return updatedDep.getIdentifier()
				.equals(pluginDep.getIdentifier())
				&& updatedDep.getVersion().equals(
						pluginDep.getVersion());
	}

	/**
	 * Returns <code>true</code> if an update is available for the plugin.
	 * 
	 * @param plugin
	 * @return
	 */
	public boolean isUpdateAvailable(Plugin plugin) {
		return updatedPlugins.contains(plugin);
	}

	/**
	 * Checks the <code>PluginSite</code>s to find updates for installed
	 * plugins.
	 * 
	 * @return true if updates are found
	 */
	public boolean checkForUpdates() {
		updatedPlugins = new ArrayList<Plugin>();
		for (PluginSite pluginSite : getPluginSites()) {
			List<Plugin> pluginsFromSite = getPluginsFromSite(pluginSite);
			for (Plugin plugin : pluginsFromSite) {
				if (plugins.contains(plugin)) {
					if (updatedPlugins.contains(plugin)) {
						int index = updatedPlugins.indexOf(plugin);
						Plugin updatedPlugin = updatedPlugins.get(index);
						if (updatedPlugin.compareVersion(plugin) < 0) {
							updatedPlugins.remove(index);
							updatedPlugins.add(plugin);
							firePluginChangedEvent(new PluginManagerEvent(this,
									plugin, plugins.indexOf(plugin)));
						}
					} else {
						int index = plugins.indexOf(plugin);
						Plugin updatedPlugin = plugins.get(index);
						if (updatedPlugin.compareVersion(plugin) < 0) {
							updatedPlugins.add(plugin);
							firePluginChangedEvent(new PluginManagerEvent(this,
									plugin, plugins.indexOf(plugin)));
						}
					}
				}
			}
		}
		return !updatedPlugins.isEmpty();
	}

	public static void addPluginManagerListener(PluginManagerListener listener) {
		synchronized (pluginManagerListeners) {
			if (!pluginManagerListeners.contains(listener)) {
				pluginManagerListeners.add(listener);
			}
		}
	}

	public static void removePluginManagerListener(
			PluginManagerListener listener) {
		synchronized (pluginManagerListeners) {
			pluginManagerListeners.remove(listener);
		}
	}

	protected void firePluginAddedEvent(PluginManagerEvent event) {
		synchronized (pluginManagerListeners) {
			for (PluginManagerListener listener : pluginManagerListeners) {
				listener.pluginAdded(event);
			}
		}
	}

	protected void firePluginRemovedEvent(PluginManagerEvent event) {
		synchronized (pluginManagerListeners) {
			for (PluginManagerListener listener : pluginManagerListeners) {
				listener.pluginRemoved(event);
			}
		}
	}

	protected void firePluginChangedEvent(PluginManagerEvent event) {
		synchronized (pluginManagerListeners) {
			for (PluginManagerListener listener : pluginManagerListeners) {
				listener.pluginChanged(event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.taverna.update.plugin.event.PluginListener#pluginChanged(net.sf
	 * .taverna.update.plugin.event.PluginEvent)
	 */
	public void pluginChanged(PluginEvent event) {
		if (event.getAction() == PluginEvent.ENABLED) {
			enablePlugin(event.getPlugin());
		} else if (event.getAction() == PluginEvent.DISABLED) {
			disablePlugin(event.getPlugin());
		}
		firePluginChangedEvent(new PluginManagerEvent(event, event.getPlugin(),
				plugins.indexOf(event.getPlugin())));
	}

	@SuppressWarnings("unchecked")
	private void initializePlugins() {
		URL pluginsFile;
		List<Plugin> extractedPlugins;
		try {
			pluginsFile = new URL(pluginsDir.toURI().toURL(), PLUGINS_XML);
			extractedPlugins = extractPluginsFromFile(pluginsFile);
		} catch (MalformedURLException e) {
			logger.warn("Invalid URL for " + pluginsDir, e);
			extractedPlugins = new ArrayList<Plugin>();
		}

		List<Plugin> builtInPlugins;
		try {
			pluginsFile = new URL(defaultPluginsDir, PLUGINS_XML);
			builtInPlugins = extractPluginsFromFile(pluginsFile);
		} catch (MalformedURLException e) {
			logger.warn("Invalid URL for " + defaultPluginsDir, e);

			builtInPlugins = new ArrayList<Plugin>();
		}

		for (Plugin plugin : extractedPlugins) {
			plugin.setBuiltIn(false); // user provided plugins are not
			// considered built in, and can be
			// uninstalled
			if (builtInPlugins.contains(plugin)) {
				int i = builtInPlugins.indexOf(plugin);
				// allow it to be uninstalled if it is the same plugin but
				// different version.
				// the built in plugin will then reappear as the original
				// version when Taverna restarts
				if (builtInPlugins.get(i).getVersion().equals(
						plugin.getVersion())) {
					plugin.setBuiltIn(true);
				}
			}
			addPlugin(plugin);
		}

		for (Plugin plugin : builtInPlugins) {
			plugin.setBuiltIn(true); // default plugins are considered built
			// in and cannot be uninstalled.
			addPlugin(plugin);
		}
		savePlugins();
		savePluginSites();
	}

	private List<Plugin> extractPluginsFromFile(URL pluginsFile) {
		List<Plugin> result = new ArrayList<Plugin>();
		PluginsDocument pluginsDoc;
		try {
			XmlOptions xmlOptions = makeXMLOptions();
			xmlOptions.setLoadReplaceDocumentElement(new QName(PLUGINS_NS,
			"plugins"));
			pluginsDoc = PluginsDocument.Factory.parse(pluginsFile,
					xmlOptions);
		} catch (FileNotFoundException ex) {
			logger.debug("Could not find " + pluginsFile);
			return result;
		} catch (XmlException e1) {
			logger.error("Error parsing " + pluginsFile, e1);
			return result;
		} catch (IOException e1) {
			logger.warn("Error reading " + pluginsFile, e1);
			return result;
		}
		if (pluginsDoc.getPlugins().getPluginArray() != null) {
			for (DescribedPlugin pluginDescr : pluginsDoc.getPlugins()
					.getPluginArray()) {
				Plugin plugin = Plugin.fromXmlBean(pluginDescr);
				result.add(plugin);
			}
		}

		return result;
	}

	public List<TavernaPluginSite> getTavernaPluginSites() {
		List<TavernaPluginSite> result = new ArrayList<TavernaPluginSite>();
		String prefix = "raven.pluginsite.";
		if (Bootstrap.properties != null) {
			Map<Integer, String> pluginSiteMap = new TreeMap<Integer, String>();
			// tree map will do the sorting for us
			for (Entry prop : Bootstrap.properties.entrySet()) {
				String propertyName = (String) prop.getKey();
				if (propertyName.startsWith(prefix)
						&& !propertyName.endsWith("name")) {
					try {
						Integer index = new Integer(propertyName.replace(
								prefix, ""));
						pluginSiteMap.put(index, (String) prop.getValue());
					} catch (NumberFormatException e) {
						logger.error("Error with index for property: "
								+ propertyName);
					}
				}
			}

			// create a list of URL objects from the space seperated list of
			// alternatives for each site
			for (Integer siteIndex : pluginSiteMap.keySet()) {
				String siteList = pluginSiteMap.get(siteIndex);
				String nameKey = prefix + siteIndex + ".name";
				String name = (String) Bootstrap.properties.get(nameKey);
				if (name == null)
					name = "Taverna Plugin Update Site";

				List<URL> urls = new ArrayList<URL>();
				logger.info("Adding plugin sitelist: " + siteList);
				String[] siteUrls = siteList.split(" ");
				for (String siteUrl : siteUrls) {
					siteUrl = siteUrl.trim();
					if (!siteUrl.endsWith("/"))
						siteUrl += "/";
					try {
						URL url = new URL(siteUrl);
						urls.add(url);
					} catch (MalformedURLException e) {
						logger
								.error("Malformed URL for plugin site (or mirror):"
										+ siteUrl);
					}
				}
				if (urls.size() > 0) {
					result.add(new TavernaPluginSite(name, urls
							.toArray(new URL[] {})));
				}
			}
		}
		return result;
	}

	private void setProxy(HttpClient client) {
		String host = System.getProperty("http.proxyHost");
		String port = System.getProperty("http.proxyPort");
		String user = System.getProperty("http.proxyUser");
		String password = System.getProperty("http.proxyPassword");

		if (host != null && port != null) {
			try {
				int portInteger = Integer.parseInt(port);
				client.getHostConfiguration().setProxy(host, portInteger);
				if (user != null && password != null) {
					client.getState().setProxyCredentials(
							new AuthScope(host, portInteger),
							new UsernamePasswordCredentials(user, password));
				}
			} catch (NumberFormatException e) {
				logger.error("Proxy port not an integer", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initializePluginSites() {
		pluginSites.addAll(getTavernaPluginSites());

		URL pluginSitesFile;
		try {
			pluginSitesFile = new URL(pluginsDir.toURI().toURL(),
					"plugin-sites.xml");
			extractPluginSitesFromFile(pluginSitesFile);
		} catch (MalformedURLException e) {
			logger.warn("Invalid URL for " + pluginsDir, e);
		}

		try {
			pluginSitesFile = new URL(defaultPluginsDir, "plugin-sites.xml");
			extractPluginSitesFromFile(pluginSitesFile);
		} catch (MalformedURLException e) {
			logger.warn("Invalid URL for " + defaultPluginsDir, e);
		}
	}

	@SuppressWarnings("unchecked")
	private void extractPluginSitesFromFile(URL pluginSitesFile) {
		try {
			Document document = new SAXBuilder().build(pluginSitesFile);
			Element root = document.getRootElement();
			List<Element> siteList = root.getChildren("pluginSite");
			for (Element site : siteList) {
				PluginSite pluginSite = PluginSite.fromXml(site);
				if (!pluginSites.contains(pluginSite))
					pluginSites.add(pluginSite);
			}
		} catch (FileNotFoundException ex) {
			logger.debug("Could not find " + pluginSitesFile);
		} catch (JDOMException e) {
			logger.error("Error parsing plugin-sites.xml", e);
		} catch (IOException e) {
			logger.warn("Error reading plugin-sites.xml", e);
		}

	}

	private void sortPlugins() {
		Collections.sort(plugins, new Comparator<Plugin>() {

			public int compare(Plugin o1, Plugin o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});
	}

	// what is the component it runs within?
	// private void showWarning(Plugin plugin) {
	// JOptionPane.showMessageDialog(PluginManager.this, plugin
	// + " depends on system artifacts, "
	// + "disabling/removing may affect Taverna performance. "
	// + "Please restart the workbench.",
	// "Plugin has System Artifacts", JOptionPane.WARNING_MESSAGE);
	// }

	protected static XmlOptions makeXMLOptions() {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSavePrettyPrintIndent(4);
		xmlOptions.setSaveOuter();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put(PLUGINS_NS, "plugins");
		xmlOptions.setSaveSuggestedPrefixes(prefixes);
		xmlOptions.setUseDefaultNamespace();
		xmlOptions.setLoadStripWhitespace();

		return xmlOptions;
	}

}
