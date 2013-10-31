/**
 * 
 */
package net.sf.taverna.t2.component.preference;

import static net.sf.taverna.t2.component.preference.ComponentDefaults.getDefaultProperties;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateRegistry;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
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
	private final Logger logger = getLogger(ComponentPreference.class);

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
		properties = new Properties();
		if (configFile.exists()) {
			try {
				FileReader reader = new FileReader(configFile);
				properties.load(reader);
				reader.close();
			} catch (final FileNotFoundException e) {
				logger.error(e);
			} catch (final IOException e) {
				logger.error(e);
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
		properties.putAll(getDefaultProperties());
	}

	private File getConfigFile() {
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File config = new File(home, "conf");
		if (!config.exists())
			config.mkdir();
		return new File(config, getFilePrefix() + "-" + getUUID() + ".config");
	}

	public void store() {
		properties.clear();
		for (Entry<String, Registry> entry : registryMap.entrySet())
			properties.put(entry.getKey(), entry.getValue()
					.getRegistryBaseString());

		try {
			FileOutputStream out = new FileOutputStream(getConfigFile());
			properties.store(out, "");
			out.close();
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
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
		// Trim trailing '/' characters to ensure match.
		String base = registryBase.toString();
		while (base.endsWith("/"))
			base = base.substring(0, base.length() - 1);

		for (Entry<String, Registry> entry : registryMap.entrySet())
			if (entry.getValue().getRegistryBaseString().equals(base))
				return entry.getKey();
		return base;
	}

	public void setRegistryMap(SortedMap<String, Registry> registries) {
		registryMap.clear();
		registryMap.putAll(registries);
	}
}
