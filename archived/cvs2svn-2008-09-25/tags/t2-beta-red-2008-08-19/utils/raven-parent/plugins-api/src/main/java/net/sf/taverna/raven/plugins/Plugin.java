package net.sf.taverna.raven.plugins;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.raven.plugins.event.PluginEvent;
import net.sf.taverna.raven.plugins.event.PluginListener;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.spi.Profile;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * 
 * @author David Withers
 */
public class Plugin implements Comparable<Plugin> {

	private static Logger logger = Logger.getLogger(Plugin.class);

	private List<PluginListener> pluginListeners = new ArrayList<PluginListener>();

	private String name;

	private String description;

	private String identifier;

	private String version;

	private String provider;

	private List<String> versions = new ArrayList<String>();

	private boolean enabled;

	private List<String> repositories = new ArrayList<String>();

	private Profile profile = new Profile(true);

	private boolean builtIn = false;

	public boolean compatible = true;

	/**
	 * Indicates whether a plugin is a default plugin, and if so cannot be
	 * uninstalled, but can be disabled.
	 * 
	 * @return
	 */
	public boolean isBuiltIn() {
		return builtIn;
	}

	public void setBuiltIn(boolean val) {
		this.builtIn = val;
	}

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

	public List<String> getVersions() {
		return this.versions;
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
	 * @param listener
	 *            the <code>PluginListener</code> to add
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
	 * @param listener
	 *            the <code>PluginListener</code> to remove
	 */
	public void removePluginListener(PluginListener listener) {
		synchronized (pluginListeners) {
			pluginListeners.remove(listener);
		}
	}

	/**
	 * Fires a <code>PluginEvent</code> to the <code>PluginListener</code>s.
	 * 
	 * @param event
	 *            the <code>PluginEvent</code> to fire
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
	 * @param pluginElement
	 *            the XML element
	 * @return a new <code>Plugin</code>
	 */
	public static Plugin fromXml(Element pluginElement) {
		Plugin plugin = new Plugin();
		plugin.name = pluginElement.getChildTextTrim("name");
		plugin.description = pluginElement.getChildTextTrim("description");
		plugin.identifier = pluginElement.getChildTextTrim("identifier");
		plugin.version = pluginElement.getChildTextTrim("version");
		plugin.provider = pluginElement.getChildTextTrim("provider");

		Element application = pluginElement.getChild("application");
		if (application != null) {
			List<Element> versionElements = application.getChildren("version");
			for (Element applicationVersion : versionElements) {
				plugin.versions.add(applicationVersion.getTextTrim());
			}
		}

		plugin.enabled = Boolean.valueOf(pluginElement
				.getChildTextTrim("enabled"));
		List<Element> repositoryElements = pluginElement.getChild(
				"repositories").getChildren("repository");
		for (Element repository : repositoryElements) {
			String repo = repository.getTextTrim();
			if (repo != null) {
				if (!repo.endsWith("/"))
					repo = repo + "/";
				plugin.repositories.add(repo);
			} else {
				logger.error("Null repository found for plugin:" + plugin.name);
			}
		}
		List<Element> artifactElements = pluginElement.getChild("profile")
				.getChildren("artifact");
		for (Element artifact : artifactElements) {
			Artifact basicArtifact = new BasicArtifact(artifact
					.getAttributeValue("groupId"), artifact
					.getAttributeValue("artifactId"), artifact
					.getAttributeValue("version"));
			if (artifact.getAttributeValue("system") != null
					&& artifact.getAttributeValue("system").equalsIgnoreCase(
							"true")) {
				plugin.profile.addSystemArtifact(basicArtifact);
			}
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
			if (getProfile().getSystemArtifacts().contains(artifact)) {
				artifactElement.setAttribute("system", "true");
			}
			profileElement.addContent(artifactElement);
		}
		pluginElement.addContent(profileElement);

		Element appliationVersions = new Element("application");
		for (String v : this.versions) {
			Element applicationVersion = new Element("version");
			applicationVersion.setText(v);
			appliationVersions.addContent(applicationVersion);
		}
		pluginElement.addContent(appliationVersions);

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
		return this.identifier + ":" + this.name + " v." + this.version;
	}

}
