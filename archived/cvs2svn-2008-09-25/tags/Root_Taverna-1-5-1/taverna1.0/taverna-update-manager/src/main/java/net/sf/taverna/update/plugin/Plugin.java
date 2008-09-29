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
 * Filename           $RCSfile: Plugin.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-16 13:55:11 $
 *               by   $Author: sowen70 $
 * Created on 28 Nov 2006
 *****************************************************************/
package net.sf.taverna.update.plugin;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.update.plugin.event.PluginEvent;
import net.sf.taverna.update.plugin.event.PluginListener;

import org.jdom.Element;

/**
 *
 * @author David Withers
 */
public class Plugin implements Comparable<Plugin> {
	private List<PluginListener> pluginListeners = new ArrayList<PluginListener>();

	private String name;

	private String description;

	private String identifier;

	private String version;

	private String provider;
	
	private List<String> tavernaVersions = new ArrayList<String>();

	private boolean enabled;

	private List<String> repositories = new ArrayList<String>();

	private Profile profile = new Profile(true);	
	
	public boolean compatible = true;		

	public boolean isCompatible() {
		return compatible;
	}

	public void setCompatible(boolean compatible) {
		this.compatible = compatible;
	}

	/**
	 * Constructs an instance of Plugin.
	 *
	 */
	private Plugin() {
	}

	/**
	 * Returns the identifier.
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the profile.
	 * 
	 * @return the profile
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * Returns the provider.
	 * 
	 * @return the provider
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * Returns <code>true</code> if the plugin is enabled.
	 * 
	 * @return <code>true</code> if the plugin is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled state.
	 * 
	 * @param enabled
	 *            the new enabled state
	 */
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			firePluginChangedEvent(new PluginEvent(this, this,
					enabled ? PluginEvent.ENABLED : PluginEvent.DISABLED));
		}
	}
	
	public List<String> getTavernaVersions() {
		return this.tavernaVersions;
	}

	/**
	 * Returns the repositories.
	 * 
	 * @return the repositories
	 */
	public List<String> getRepositories() {
		return repositories;
	}

	/**
	 * Returns the version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Adds a <code>PluginListener</code>.
	 * 
	 * @param listener the <code>PluginListener</code> to add
	 */
	public void addPluginListener(PluginListener listener) {
		synchronized (pluginListeners) {
			if (!pluginListeners.contains(listener)) {
				pluginListeners.add(listener);
			}
		}
	}

	/**
	 * Removes a <code>PluginListener</code>.
	 * 
	 * @param listener the <code>PluginListener</code> to remove
	 */
	public void removePluginListener(PluginListener listener) {
		synchronized (pluginListeners) {
			pluginListeners.remove(listener);
		}
	}

	/**
	 * Fires a <code>PluginEvent</code> to the <code>PluginListener</code>s.
	 * 
	 * @param event the <code>PluginEvent</code> to fire
	 */
	protected void firePluginChangedEvent(PluginEvent event) {
		synchronized (pluginListeners) {
			for (PluginListener listener : pluginListeners) {
				listener.pluginChanged(event);
			}
		}
	}

	/**
	 * Creates a <code>Plugin</code> from an XML element.
	 * 
	 * @param pluginElement the XML element
	 * @return a new <code>Plugin</code>
	 */
	public static Plugin fromXml(Element pluginElement) {
		Plugin plugin = new Plugin();
		plugin.name = pluginElement.getChildTextTrim("name");
		plugin.description = pluginElement.getChildTextTrim("description");
		plugin.identifier = pluginElement.getChildTextTrim("identifier");
		plugin.version = pluginElement.getChildTextTrim("version");
		plugin.provider = pluginElement.getChildTextTrim("provider");
		
		if (pluginElement.getChild("taverna")==null) {
			//if missing, then assume only compatible with 1.5.0 since this tag didn't exist in that version
			plugin.tavernaVersions.add("1.5.0");
		}
		else {
			List<Element> tavernaElements = pluginElement.getChild("taverna").getChildren("version");
			for (Element tavernaVersion : tavernaElements) {
				plugin.tavernaVersions.add(tavernaVersion.getTextTrim());
			}
		}
		
		
		plugin.enabled = Boolean.valueOf(pluginElement
				.getChildTextTrim("enabled"));
		List<Element> repositoryElements = pluginElement.getChild(
				"repositories").getChildren("repository");
		for (Element repository : repositoryElements) {
			plugin.repositories.add(repository.getTextTrim());
		}
		List<Element> artifactElements = pluginElement.getChild("profile")
				.getChildren("artifact");
		for (Element artifact : artifactElements) {
			Artifact basicArtifact = new BasicArtifact(artifact
					.getAttributeValue("groupId"), artifact
					.getAttributeValue("artifactId"), artifact
					.getAttributeValue("version"));
			plugin.profile.addArtifact(basicArtifact);
		}		
		return plugin;
	}

	/**
	 * Creates an XML element from this <code>Plugin</code>.
	 * 
	 * @return an XML element for this <code>Plugin</code>
	 */
	public Element toXml() {
		Element pluginElement = new Element("plugin");
		pluginElement.addContent(new Element("name").addContent(getName()));
		pluginElement.addContent(new Element("description")
				.addContent(getDescription()));
		pluginElement.addContent(new Element("identifier")
				.addContent(getIdentifier()));
		pluginElement.addContent(new Element("version")
				.addContent(getVersion()));
		
		pluginElement.addContent(new Element("provider")
				.addContent(getProvider()));
		pluginElement.addContent(new Element("enabled").addContent(Boolean
				.toString(isEnabled())));
		Element repositoriesElement = new Element("repositories");
		for (String repository : getRepositories()) {
			repositoriesElement.addContent(new Element("repository")
					.addContent(repository));
		}
		pluginElement.addContent(repositoriesElement);
		Element profileElement = new Element("profile");
		for (Artifact artifact : getProfile().getArtifacts()) {
			Element artifactElement = new Element("artifact");
			artifactElement.setAttribute("groupId", artifact.getGroupId());
			artifactElement
					.setAttribute("artifactId", artifact.getArtifactId());
			artifactElement.setAttribute("version", artifact.getVersion());
			profileElement.addContent(artifactElement);
		}
		pluginElement.addContent(profileElement);
		
		Element tavernaversions=new Element("taverna");
		for (String v : this.tavernaVersions) {
			Element tavernaVersion=new Element("version");
			tavernaVersion.setText(v);
			tavernaversions.addContent(tavernaVersion);
		}
		pluginElement.addContent(tavernaversions);
		
		return pluginElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Plugin other = (Plugin) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

	public int compareTo(Plugin o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return 1;
		}
		return identifier.compareTo(o.identifier);
	}

	public int compareVersion(Plugin plugin) {
		if (this == plugin) {
			return 0;
		}
		if (plugin == null) {
			return -1;
		}
		return version.compareTo(plugin.version);
	}
	
	public String toString() {
		return this.identifier+":"+this.name+" v."+this.version;
	}

}