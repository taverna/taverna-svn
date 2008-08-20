package net.sf.taverna.t2.workbench.reference.config;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * Configuration for the reference service.
 * 
 * @author David Withers
 */
public class ReferenceConfiguration extends AbstractConfigurable {

	public static final String REFERENCE_SERVICE_CONTEXT = "referenceService.context";

	public static final String IN_MEMORY_CONTEXT = "inMemoryReferenceServiceContext.xml";

	public static final String HIBERNATE_CONTEXT = "hibernateReferenceServiceContext.xml";

	private Map<String, String> defaultPropertyMap;

	private static ReferenceConfiguration instance;

	public static ReferenceConfiguration getInstance() {
		if (instance == null) {
			instance = new ReferenceConfiguration();
		}
		return instance;
	}

	private ReferenceConfiguration() {
	}

	public String getCategory() {
		return "general";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<String, String>();
			defaultPropertyMap.put(REFERENCE_SERVICE_CONTEXT,
					IN_MEMORY_CONTEXT);
		}
		return defaultPropertyMap;
	}

	public String getName() {
		return "Data Storage";
	}

	public String getUUID() {
		return "6BD3F5C1-C68D-4893-8D9B-2F46FA1DDB19";
	}

}
