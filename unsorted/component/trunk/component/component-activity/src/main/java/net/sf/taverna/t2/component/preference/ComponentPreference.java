/**
 * 
 */
package net.sf.taverna.t2.component.preference;

import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateRegistry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentPreference {
	private static final String MYEXPERIMENT_NAME = "myExperiment";
	private static final String MYEXPERIMENT_SITE = "http://www.myexperiment.org";
	private final Logger logger = Logger.getLogger(ComponentPreference.class);

	private static ComponentPreference instance = null;

	private final Properties properties;
	private final SortedMap<String, Registry> registryMap = new TreeMap<String, Registry>();

	public static ComponentPreference getInstance() {
		if (instance == null)
			instance = new ComponentPreference();
		return instance;
	}

	private ComponentPreference() {
		File configFile = getConfigFile();
		this.properties = new Properties();
		if (configFile.exists()) {
			try {
				final FileReader reader = new FileReader(configFile);
				this.properties.load(reader);
				reader.close();
			} catch (final FileNotFoundException e) {
				this.logger.error(e);
			} catch (final IOException e) {
				this.logger.error(e);
			}
		} else {
			fillDefaultProperties();
		}
		updateRegistryMap();
	}

	private void updateRegistryMap() {
		registryMap.clear();
		for (Object key : properties.keySet())
			try {
				String name = (String) key;
				registryMap.put(name, calculateRegistry(new URL(
						(String) properties.get(name))));
			} catch (MalformedURLException e) {
				logger.error(e);
			} catch (RegistryException e) {
				logger.error(e);
			}
	}

	private void fillDefaultProperties() {
		properties.setProperty("local registry",
				calculateComponentsDirectoryPath());
		properties.setProperty(MYEXPERIMENT_NAME, MYEXPERIMENT_SITE);
	}

	public String calculateComponentsDirectoryPath() {
		return new File(ApplicationRuntime.getInstance()
				.getApplicationHomeDir(), "components").toURI().toASCIIString();
	}

	private File getConfigFile() {
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File config = new File(home, "conf");
		if (!config.exists())
			config.mkdir();
		return new File(config, getFilePrefix() + "-" + getUUID() + ".config");
	}

	public void store() {
		this.properties.clear();
		for (String key : registryMap.keySet())
			this.properties.put(key, registryMap.get(key).getRegistryBase()
					.toString());

		try {
			final FileOutputStream out = new FileOutputStream(getConfigFile());
			this.properties.store(out, "");
			out.close();
		} catch (final FileNotFoundException e) {
			this.logger.error(e);
		} catch (final IOException e) {
			this.logger.error(e);
		}
	}

	public String getFilePrefix() {
		return "Component";
	}

	public String getUUID() {
		return "2317A297-2AE0-42B5-86DC-99C9B7C0524A";
	}

	/**
	 * @return the registryMap
	 */
	public SortedMap<String, Registry> getRegistryMap() {
		return registryMap;
	}

	public String getRegistryName(URL registryBase) {
		String result = registryBase.toString();
		for (String name : registryMap.keySet())
			if (registryMap.get(name).getRegistryBase().toString()
					.equals(registryBase.toString())) {
				result = name;
				break;
			}
		return result;
	}

	public void setRegistryMap(SortedMap<String, Registry> registries) {
		registryMap.clear();
		registryMap.putAll(registries);
	}
}
