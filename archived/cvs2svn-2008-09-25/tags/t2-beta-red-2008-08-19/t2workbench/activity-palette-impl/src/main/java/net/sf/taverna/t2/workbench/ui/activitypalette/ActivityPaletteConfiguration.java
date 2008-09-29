package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

public class ActivityPaletteConfiguration extends AbstractConfigurable {
	
	private Map<String,String> defaultPropertyMap;
	
	private static ActivityPaletteConfiguration instance = new ActivityPaletteConfiguration();

	private ActivityPaletteConfiguration() {

	}

	public static ActivityPaletteConfiguration getInstance() {
		return instance;
	}

	public String getCategory() {
		return "activities";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap==null) {
			defaultPropertyMap = new HashMap<String, String>();
		
			//wsdl
			defaultPropertyMap.put("taverna.defaultwsdl", "http://www.mygrid.org.uk/taverna-tests/testwsdls/KEGG.wsdl,http://www.mygrid.org.uk/taverna-tests/testwsdls/whatizit.wsdl");
			
			//soaplab
			defaultPropertyMap.put("taverna.defaultsoaplab", "http://www.ebi.ac.uk/soaplab/services/");
			
			//biomart
			defaultPropertyMap.put("taverna.defaultmartregistry","http://www.biomart.org/biomart");
			
			//add property names
			defaultPropertyMap.put("name.taverna.defaultwsdl", "WSDL");
			defaultPropertyMap.put("name.taverna.defaultsoaplab","Soaplab");
			defaultPropertyMap.put("name.taverna.defaultmartregistry", "Biomart");
		}
		return defaultPropertyMap;
	}

	public String getName() {
		return "Activity Palette";
	}

	public String getUUID() {
		return "ad9f3a60-5967-11dd-ae16-0800200c9a66";
	}

}
