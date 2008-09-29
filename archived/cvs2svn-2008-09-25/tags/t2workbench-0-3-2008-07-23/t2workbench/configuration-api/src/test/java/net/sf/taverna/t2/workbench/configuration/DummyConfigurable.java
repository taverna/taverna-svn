/**
 * 
 */
package net.sf.taverna.t2.workbench.configuration;

import java.util.HashMap;
import java.util.Map;

class DummyConfigurable extends AbstractConfigurable {

	private static DummyConfigurable instance = new DummyConfigurable();

	private DummyConfigurable() {

	}

	public static DummyConfigurable getInstance() {
		return instance;
	}
	
	Map<String,Object> defaults = null;
	
	public String getCategory() {
		return "test";
	}

	public Map<String, Object> getDefaultPropertyMap() {
		if (defaults==null) {
			defaults = new HashMap<String, Object>();
			defaults.put("name","john");
			defaults.put("colour","blue");
		}
		return defaults;
	}

	public String getName() {
		return "dummy";
	}

	public String getUUID() {
		return "cheese";
	}
	
}