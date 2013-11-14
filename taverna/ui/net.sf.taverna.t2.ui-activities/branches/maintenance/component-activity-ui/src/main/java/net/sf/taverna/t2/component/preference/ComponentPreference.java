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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentPreference extends AbstractConfigurable {
	
	public static final String DISPLAY_NAME = "Components";

	private final Logger logger = getLogger(ComponentPreference.class);

	private static ComponentPreference instance = null;

	private final SortedMap<String, Registry> registryMap = new TreeMap<String, Registry>();
	
	public static final String REGISTRY_NAMES = "REGISTRY_NAMES";

	public static ComponentPreference getInstance() {
		if (instance == null)
			instance = new ComponentPreference();
		return instance;
	}

	private ComponentPreference() {
		super();

		recoverRegistryMap();
	}

	private void recoverRegistryMap() {
		registryMap.clear();

		for (String key : getRegistryKeys()) {
			String value = super.getProperty(key);
			try {
				registryMap.put(key, calculateRegistry(new URL(
						value)));
			} catch (MalformedURLException e) {
				logger.error("bogus url (" + value
						+ ") in configuration file", e);
			} catch (RegistryException e) {
				logger.error("failed to construct registry handle for "
						+ value, e);
			}
		}
	}
	
	private String[] getRegistryKeys() {
		String registryNamesConcatenated = super.getProperty(REGISTRY_NAMES);
		if (registryNamesConcatenated == null) {
			return (String[])getDefaultPropertyMap().keySet().toArray(new String[]{});
		} else {
			return registryNamesConcatenated.split(",");
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
		super.clear();
		List<String> keyList = new ArrayList<String>();
		for (Entry<String, Registry> entry : registryMap.entrySet()) {
			final String key = entry.getKey();
			keyList.add (key);
			super.setProperty(key, entry.getValue()
					.getRegistryBaseString());
		}
		String registryNamesConcatenated = StringUtils.join(keyList, ",");
		super.setProperty(REGISTRY_NAMES, registryNamesConcatenated);
	}

	@Override
	public Map<String, String> getDefaultPropertyMap() {
		return getDefaultProperties();
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getCategory() {
		return "general";
	}
}
