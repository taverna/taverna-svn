package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

public class ActivityPaletteConfiguration extends AbstractConfigurable {
	
	private Map<String,Object> defaultPropertyMap;
	
	private static ActivityPaletteConfiguration instance = new ActivityPaletteConfiguration();

	private ActivityPaletteConfiguration() {

	}

	public static ActivityPaletteConfiguration getInstance() {
		return instance;
	}

	public String getCategory() {
		return "activities";
	}

	public Map<String, Object> getDefaultPropertyMap() {
		if (defaultPropertyMap==null) {
			defaultPropertyMap = new HashMap<String, Object>();
			List<String> wsdlList = new ArrayList<String>();
//			wsdlList.add("http://www.mygrid.org.uk/taverna-tests/testwsdls/KEGG.wsdl");
//			wsdlList.add("http://www.mygrid.org.uk/taverna-tests/testwsdls/bind.wsdl");
			wsdlList.add("http://www.mygrid.org.uk/taverna-tests/testwsdls/whatizit.wsdl");
//	//		wsdlList.add("http://www.mygrid.org.uk/taverna-tests/testwsdls/GUIDGenerator.wsdl");
//			List<String> soaplabList = new ArrayList<String>();
//			soaplabList.add("http://www.ebi.ac.uk/soaplab/services/");
			defaultPropertyMap.put("taverna.defaultwsdl", wsdlList);
//	//		map.put("taverna.defaultsoaplab", soaplabList);
//			List<String> biomart = new ArrayList<String>();
//			biomart.add("http://www.biomart.org/biomart");
//			defaultPropertyMap.put("taverna.defaultmartregistry",biomart);
		}
		return defaultPropertyMap;
	}
	
	/**
	 * Forces and returns a List<String> for the property value, even if the value contains only 1 item.
	 *
	 * @param key
	 * @return
	 */
	@Override
	public synchronized Object getProperty(String key) {
		Object value = super.getProperty(key);
		List<String> result = null;
		if (value instanceof String) {
			result=new ArrayList<String>();
			result.add((String)value);
		}
		else if (value!=null) {
			result= (List<String>)value;
		}
		return result;
	}

	public String getName() {
		return "Acitivity Palette";
	}

	public String getUUID() {
		return "f8a318b0-5345-11dd-ae16-0800200c9a66";
	}

}
