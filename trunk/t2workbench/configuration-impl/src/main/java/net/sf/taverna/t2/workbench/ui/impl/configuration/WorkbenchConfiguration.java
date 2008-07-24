package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * An implementation of Configurable for general Workbench configuration
 * properties
 * 
 * @author Stuart Owen
 * 
 */
public class WorkbenchConfiguration extends AbstractConfigurable {

	public static String uuid = "c14856f0-5967-11dd-ae16-0800200c9a66";
	private static WorkbenchConfiguration instance = new WorkbenchConfiguration();

	public static WorkbenchConfiguration getInstance() {
		return instance;
	}


	Map<String, String> defaultWorkbenchProperties = null;
	Map<String, String> workbenchProperties = new HashMap<String, String>();

	public String getCategory() {
		return "general";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultWorkbenchProperties == null) {
			defaultWorkbenchProperties = new HashMap<String, String>();
			String dotLocation = System.getProperty("taverna.dotlocation") != null ? System
					.getProperty("taverna.dotlocation")
					: "/Applications/Taverna-1.7.1.app/Contents/MacOS/dot";
			defaultWorkbenchProperties.put("taverna.dotlocation", dotLocation);
		}
		return defaultWorkbenchProperties;
	}

	public String getName() {
		return "Workbench";
	}

	public String getUUID() {
		return uuid;
	}

	
}
