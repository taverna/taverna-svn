package net.sf.taverna.t2.component.preference;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

/**
 * Factored out defaults location system.
 * 
 * @author Donal Fellows
 * 
 */
public class ComponentDefaults {
	private static final String LOCAL_NAME = "local registry";
	private static final String MYEXPERIMENT_NAME = "myExperiment";
	private static final String MYEXPERIMENT_SITE = "http://www.myexperiment.org";

	public static Map<String, String> getDefaultProperties() {
		// Capacity = 2; we know that this is going to have 2 entries
		Map<String, String> defaults = new LinkedHashMap<String, String>(2);
		defaults.put(LOCAL_NAME, calculateComponentsDirectoryPath());
		defaults.put(MYEXPERIMENT_NAME, MYEXPERIMENT_SITE);
		return defaults;
	}

	public static String calculateComponentsDirectoryPath() {
		return new File(ApplicationRuntime.getInstance()
				.getApplicationHomeDir(), "components").toURI().toASCIIString();
	}

	private ComponentDefaults() {
	}
}
