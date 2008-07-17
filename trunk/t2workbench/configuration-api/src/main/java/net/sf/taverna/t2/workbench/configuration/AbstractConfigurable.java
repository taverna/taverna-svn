package net.sf.taverna.t2.workbench.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


public abstract class AbstractConfigurable implements Configurable {
	
	private Map<String,Object> propertyMap = new HashMap<String, Object>();

	private static Logger logger = Logger.getLogger(AbstractConfigurable.class);
	
	
	public AbstractConfigurable() {
		try {
			ConfigurationManager.getInstance().populate(this);
		} catch (Exception e) {
			logger.error("There was an error reading the properties for the Configurable:"+getName(),e);
		}
	}

	public synchronized Object getProperty(String key) {
		Object result = getPropertyMap().get(key);
		if (result == null) {
			result = getDefaultPropertyMap().get(key);
			if (result!=null) {
				getPropertyMap().put(key, result);
				store();
			}
		}
		return result;
	}

	protected void store() {
		try {
			ConfigurationManager.getInstance().store(this);
		} catch (Exception e) {
			logger.error("There was an error storing the new configuration for: "+this.getName(),e);
		}
	}

	public synchronized void setProperty(String key, Object value) {
		Object oldValue = getPropertyMap().get(key);
		if (value==null || !value.equals(oldValue)) {
			getPropertyMap().put(key, value);
			store();
		}
	}


	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}


	public void restoreDefaults() {
		propertyMap.clear();
		propertyMap.putAll(getDefaultPropertyMap());
		store();
	}
	
	

}
