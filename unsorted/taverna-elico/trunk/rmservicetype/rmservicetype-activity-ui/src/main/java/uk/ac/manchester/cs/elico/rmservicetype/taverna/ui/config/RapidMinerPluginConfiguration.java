package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

public class RapidMinerPluginConfiguration extends AbstractConfigurable {

	public static final String RA_REPOSITORY_LOCATION = "repository_location";

	public static final String FL_LOCATION = "floraLocation";

	public static final String FL_TEMPDIR = "floraTempDir";
	
    private static RapidMinerPluginConfiguration instance;
    
    private Map<String, String> defaultPropertyMap;
    
    
	public RapidMinerPluginConfiguration() {

	}

	public String getCategory() {
		return "general";
	}
	
    public static RapidMinerPluginConfiguration getInstance() {
      
    	if (instance == null) {
            instance = new RapidMinerPluginConfiguration();
        }
        return instance;
        
    }

	public Map<String, String> getDefaultPropertyMap() {
		
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<String, String>();
	        defaultPropertyMap.put(RA_REPOSITORY_LOCATION, "default_repository_location");
	        defaultPropertyMap.put(FL_LOCATION, "fl_default_location");
	        defaultPropertyMap.put(FL_TEMPDIR, "fl_temp_dir");
	    }
	    return defaultPropertyMap;
		
	}

	public String getDisplayName() {
		return "e-LICO";
	}

	public String getFilePrefix() {
		return "eLICO";
	}

	public String getUUID() {
		return "8e8a3350-45af-11e0-9207-0800200c9a66";
	}

}
