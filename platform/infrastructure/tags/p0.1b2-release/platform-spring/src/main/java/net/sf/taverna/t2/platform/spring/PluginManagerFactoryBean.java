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
package net.sf.taverna.t2.platform.spring;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.platform.plugin.PluginDescriptionParser;
import net.sf.taverna.t2.platform.plugin.PluginIdentifier;
import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.plugin.impl.PluginManagerImpl;
import net.sf.taverna.t2.platform.pom.JarManager;
import net.sf.taverna.t2.platform.raven.Raven;
import net.sf.taverna.t2.platform.util.download.DownloadManager;

import org.springframework.core.io.Resource;

/**
 * A factory bean used to configure an instance of PluginManager. This bean
 * allows for the specification of 'default' plug-in packages to be loaded if
 * this is the first time this plug-in manager is instantiated. The plug-in
 * manager writes a list of currently installed and activated plug-ins out to
 * disk when accessed, if this file is present the defaults here are ignored.
 * 
 * @author Tom Oinn
 */
public class PluginManagerFactoryBean extends AbstractPlatformFactoryBean {

	private List<PluginIdentifier> defaultPlugins = new ArrayList<PluginIdentifier>();;
	private List<URL> remoteRepositories = new ArrayList<URL>();
	private PluginDescriptionParser pluginParser = null;
	private JarManager jarManager = null;
	private Raven raven = null;
	private DownloadManager downloadManager = null;
	private File base = null;
	private List<String> artifactList = new ArrayList<String>();

	public void setDefaultPluginList(List<String> defaultPluginStrings) {
		for (String pluginString : defaultPluginStrings) {
			defaultPlugins.add(new PluginIdentifier(getString(pluginString)));
		}
	}

	public void setRemoteRepositoryList(List<String> remoteRepositoryStrings) {
		try {
			for (String repositoryString : remoteRepositoryStrings) {
				remoteRepositories.add(new URL(getString(repositoryString)));
			}
		} catch (MalformedURLException mue) {
			throw new RuntimeException("Can't set URL to plug-in repository",
					mue);
		}
	}

	public void setArtifactList(List<String> artifactList) {
		this.artifactList = artifactList;
	}

	public void setJarManager(JarManager manager) {
		this.jarManager = manager;
	}

	public void setRaven(Raven raven) {
		this.raven = raven;
	}

	public void setPluginParser(PluginDescriptionParser parser) {
		this.pluginParser = parser;
	}

	public void setDownloadManager(DownloadManager manager) {
		this.downloadManager = manager;
	}

	public void setBase(File base) {
		this.base = base;
	}

	public Object getObject() throws Exception {
		// Check that all properties are specified
		if (base == null || downloadManager == null || jarManager == null
				|| pluginParser == null || raven == null) {
			System.out.println("base = " + base);
			System.out.println("downloadManager = " + downloadManager);
			System.out.println("jarManager = " + jarManager);
			System.out.println("pluginParser = " + pluginParser);
			System.out.println("raven = " + raven);
			throw new RuntimeException(
					"Plugin manager configuration incomplete");
		}
		// Construct a new PluginManager
		PluginManagerImpl pmi = new PluginManagerImpl() {

			@SuppressWarnings("unchecked")
			@Override
			public Object getResourceForType(Class<?> resourceType) {
				Map<String, Object> beans = getContext().getBeansOfType(
						resourceType, false, false);
				if (beans.isEmpty()) {
					return null;
				} else {
					return beans.values().toArray()[0];
				}
			}

		};
		pmi.setArtifactList(artifactList);
		pmi.setBaseJarLocation(base);
		pmi.setDownloadManager(downloadManager);
		pmi.setJarManager(jarManager);
		pmi.setPluginParser(pluginParser);
		pmi.setRaven(raven);

		// Check whether the plugin configuration file exists, in which case we
		// ignore any default plugins. If it does not then we use the supplied
		// default plugin list to install and activate all plugins on the list
		// (this will have the side effect that this file will then be created,
		// so subsequent runs will ignore the definition in the context)
		File pluginManagerConfig = new File(base, "pluginManager.plugins");
		if (!pluginManagerConfig.exists()) {
			for (PluginIdentifier plugin : defaultPlugins) {
				pmi.activatePlugin(plugin, remoteRepositories);
			}
		} else {
			pmi.loadConfiguration(remoteRepositories);
		}
		return pmi;
	}

	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return PluginManager.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setDefaultPluginListResource(Resource res) {
		for (String line : getStrings(res)) {
			defaultPlugins.add(new PluginIdentifier(line));
		}
	}

	public void setRemoteRepositoryListResource(Resource res)
			throws MalformedURLException {
		for (String line : getStrings(res)) {
			remoteRepositories.add(new URL(line));
		}
	}
}
