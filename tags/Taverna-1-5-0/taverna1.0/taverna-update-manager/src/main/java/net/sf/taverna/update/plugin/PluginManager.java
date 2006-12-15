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
 * Revision           $Revision: 1.15 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-13 16:25:45 $
 *               by   $Author: sowen70 $
 * Created on 23 Nov 2006
 *****************************************************************/
package net.sf.taverna.update.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.update.plugin.event.PluginEvent;
import net.sf.taverna.update.plugin.event.PluginListener;
import net.sf.taverna.update.plugin.event.PluginManagerEvent;
import net.sf.taverna.update.plugin.event.PluginManagerListener;
import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author David Withers
 */
public class PluginManager implements PluginListener {
	
	private static Log logger = Log.getLogger(PluginListener.class);

	private static PluginManager instance;

	private static Repository repository;

	private List<PluginManagerListener> pluginManagerListeners = new ArrayList<PluginManagerListener>();

	private File pluginsDir;

	private List<PluginSite> pluginSites = new ArrayList<PluginSite>();

	private List<Plugin> plugins = new ArrayList<Plugin>();

	private List<Plugin> updatedPlugins = new ArrayList<Plugin>();

	private Profile profile = ProfileFactory.getInstance().getProfile();

	/**
	 * Constructs an instance of PluginManager.
	 * 
	 */
	private PluginManager() {
		pluginsDir = MyGridConfiguration.getUserDir("plugins");
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

	public void addPlugin(Plugin plugin) {
		if (!plugins.contains(plugin)) {
			plugins.add(plugin);
			sortPlugins();
			for (String repositoryURL : plugin.getRepositories()) {
				try {
					repository.addRemoteRepository(new URL(repositoryURL));
				} catch (MalformedURLException e) {
					logger.warn("Invalid remote repository URL - "
							+ repositoryURL);
				}
			}
			for (Artifact artifact : plugin.getProfile().getArtifacts()) {
				repository.addArtifact(artifact);
			}
			repository.update();
			if (plugin.isEnabled()) {
				enablePlugin(plugin);
			}
			firePluginAddedEvent(new PluginManagerEvent(this, plugin,plugins.indexOf(plugin)));
			plugin.addPluginListener(this);
		}
	}

	public void removePlugin(Plugin plugin) {
		if (updatedPlugins.contains(plugin)) updatedPlugins.remove(plugin);
		
		if (plugins.contains(plugin)) {
			if (plugin.isEnabled()) {
				disablePlugin(plugin);
			}
			int index=plugins.indexOf(plugin);
			plugins.remove(plugin);
			firePluginRemovedEvent(new PluginManagerEvent(this, plugin,index));										
			plugin.removePluginListener(this);
		}		
	}

	private void enablePlugin(Plugin plugin) {
		if (plugins.contains(plugin)) {
			for (Artifact artifact : plugin.getProfile().getArtifacts()) {
				profile.addArtifact(artifact);
			}
		savePlugins();
	}
	}

	private void disablePlugin(Plugin plugin) {
		if (plugins.contains(plugin)) {
			for (Artifact artifact : plugin.getProfile().getArtifacts()) {
				profile.removeArtifact(artifact);
			}
		savePlugins();
	}
	}

	public void savePlugins() {		
		Element pluginsElement = new Element("plugins");
		for (Plugin plugin : plugins) {
			pluginsElement.addContent(plugin.toXml());
		}
		File pluginsFile = new File(pluginsDir, "plugins.xml");
		try {
			Writer writer = new FileWriter(pluginsFile);
			new XMLOutputter(Format.getPrettyFormat()).output(
					pluginsElement, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error("Error writing plugins to "
					+ pluginsFile.getPath());
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
	public List<Plugin> getPluginsFromSite(PluginSite pluginSite) {
		List<Plugin> plugins = new ArrayList<Plugin>();
		HttpClient client = new HttpClient();
		HttpMethod getPlugins = new GetMethod(pluginSite.getUrl()
				+ "plugins.xml");
		try {
			int statusCode = client.executeMethod(getPlugins);
			if (statusCode != HttpStatus.SC_OK) {
				// log
			}
			Document pluginsDocument = new SAXBuilder().build(getPlugins
					.getResponseBodyAsStream());
			List<Element> pluginList = pluginsDocument.getRootElement()
					.getChildren("plugin");
			for (Element plugin : pluginList) {
				URI pluginUri = new URI(plugin.getTextTrim());
				URL pluginURL=pluginSite.getUrl();
				if (pluginURL!=null) {
					pluginUri = pluginURL.toURI().resolve(pluginUri);
					HttpMethod getPlugin = new GetMethod(pluginUri.toString());
					statusCode = client.executeMethod(getPlugin);
					if (statusCode != HttpStatus.SC_OK) {
						// log
					}
					Document pluginDocument = new SAXBuilder().build(getPlugin
							.getResponseBodyAsStream());
					plugins.add(Plugin.fromXml(pluginDocument.getRootElement()));
				}
			}
		} catch (JDOMException e) {
			logger.error("Error parsing xml: "+e.getMessage());
		} catch (IOException e) {
			logger.error("Error contacting plugin site: "+e.getMessage());
		} catch (URISyntaxException e) {
			logger.error("Error parsing plugin site: "+e.getMessage());
		}
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
	 * @param plugin the plugin to update
	 */
	public void updatePlugin(Plugin plugin) {
		if (isUpdateAvailable(plugin)) {
			synchronized (updatedPlugins) {
				Plugin newPlugin = getUpdate(plugin);
				updatedPlugins.remove(newPlugin);
				newPlugin.setEnabled(plugin.isEnabled());
				removePlugin(plugin);
				addPlugin(newPlugin);								
				savePlugins();
			}
		}
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
	 * Checks the <code>PluginSite</code>s to find updates for installed plugins.
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
									plugin,plugins.indexOf(plugin)));
						}
					} else {
						int index = plugins.indexOf(plugin);
						Plugin updatedPlugin = plugins.get(index);
						if (updatedPlugin.compareVersion(plugin) < 0) {
							updatedPlugins.add(plugin);
							firePluginChangedEvent(new PluginManagerEvent(this,
									plugin,plugins.indexOf(plugin)));
						}
					}
				}
			}
		}
		return updatedPlugins.size() > 0;
	}

	public void addPluginManagerListener(PluginManagerListener listener) {
		synchronized (pluginManagerListeners) {
			if (!pluginManagerListeners.contains(listener)) {
				pluginManagerListeners.add(listener);
			}
		}
	}	

	public void removePluginManagerListener(PluginManagerListener listener) {
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

	/* (non-Javadoc)
	 * @see net.sf.taverna.update.plugin.event.PluginListener#pluginChanged(net.sf.taverna.update.plugin.event.PluginEvent)
	 */
	public void pluginChanged(PluginEvent event) {
		if (event.getAction() == PluginEvent.ENABLED) {
			enablePlugin(event.getPlugin());
		} else if (event.getAction() == PluginEvent.DISABLED) {
			disablePlugin(event.getPlugin());
		}
		firePluginChangedEvent(new PluginManagerEvent(event, event.getPlugin(),plugins.indexOf(event.getPlugin())));
	}

	private void initializePlugins() {
		File pluginsFile = new File(pluginsDir, "plugins.xml");
		if (pluginsFile.exists()) {
			try {
				Document document = new SAXBuilder().build(pluginsFile);
				Element root = document.getRootElement();
				List<Element> pluginList = root.getChildren("plugin");
				for (Element pluginElement : pluginList) {
					Plugin plugin = Plugin.fromXml(pluginElement);
					addPlugin(plugin);
				}
				savePlugins();
			} catch (JDOMException e) {
				logger.error("Error parsing plugins.xml",e);
			} catch (IOException e) {
				logger.error("Error reading plugins.xml",e);
			}
		}
	}

	public List<TavernaPluginSite> getTavernaPluginSites() {
		List<TavernaPluginSite> result = new ArrayList<TavernaPluginSite>();
		String prefix="raven.pluginsite.";
		Map<Integer,String> pluginSiteMap = new TreeMap<Integer,String>(); //tree map will do the sorting for us
		for (Entry prop : Bootstrap.properties.entrySet()) {
			String propertyName=(String)prop.getKey();
			if (propertyName.startsWith(prefix) && !propertyName.endsWith("name")) {		
				try {
					Integer index=new Integer(propertyName.replace(prefix,""));
					pluginSiteMap.put(index, (String)prop.getValue());
				}
				catch(NumberFormatException e) {
					logger.error("Error with index for property: "+propertyName);
				}								
			}
		}
		
		//create a list of URL objects from the comma seperated list of alternatives for each site		
		for (Integer siteIndex : pluginSiteMap.keySet()) {
			String siteList = pluginSiteMap.get(siteIndex);
			String nameKey=prefix+siteIndex+".name";
			String name=(String)Bootstrap.properties.get(nameKey);
			if (name==null) name="Taverna Plugin Update Site";
			
			List<URL> urls = new ArrayList<URL>();
			logger.info("Adding plugin sitelist: "+siteList);
			String [] siteUrls = siteList.split(",");
			for (String siteUrl : siteUrls) {
				siteUrl=siteUrl.trim();
				if (!siteUrl.endsWith("/")) siteUrl+="/";
				try {
					URL url = new URL(siteUrl);
					urls.add(url);
				}
				catch(MalformedURLException e) {
					logger.error("Malformed URL for plugin site (or mirror):"+siteUrl);
				}
			}
			if (urls.size()>0) {
				result.add(new TavernaPluginSite(name,urls.toArray(new URL[]{})));
			}
		}
		return result;
	}
	
	private void initializePluginSites() {
		pluginSites.addAll(getTavernaPluginSites());
		
		File pluginSitesFile = new File(pluginsDir, "plugin-sites.xml");
		if (pluginSitesFile.exists()) {
			try {
				Document document = new SAXBuilder().build(pluginSitesFile);
				Element root = document.getRootElement();
				List<Element> siteList = root.getChildren("pluginSite");
				for (Element site : siteList) {
					pluginSites.add(PluginSite.fromXml(site));
				}
			} catch (JDOMException e) {
				logger.error("Error parsing plugin-sites.xml",e);
			} catch (IOException e) {
				logger.error("Error reading plugin-sites.xml",e);
			}
		}					
	}

	private void sortPlugins() {
		Collections.sort(plugins, new Comparator<Plugin>() {

			public int compare(Plugin o1, Plugin o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});
	}	
}
