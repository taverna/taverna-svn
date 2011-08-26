package net.sf.taverna.t2.workbench.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * A utility abstract class that simplifies implementing a Configurable.
 * <br>
 * <p>A concrete version of this class needs to define the name,category, 
 * UUID string and the set of default values.</p>
 * 
 * @author Stuart Owen
 *
 */
public abstract class AbstractConfigurable implements Configurable {
	
	private Map<String,Object> propertyMap = new HashMap<String, Object>();

	private static Logger logger = Logger.getLogger(AbstractConfigurable.class);
	
	
	/**
	 * Constructs the AbstractConfigurable by either reading from a previously stored set of properties,
	 * or by using the default values which results in them being stored for subsequent usage.
	 */
	public AbstractConfigurable() {
		try {
			ConfigurationManager.getInstance().populate(this);
		} catch (Exception e) {
			logger.error("There was an error reading the properties for the Configurable:"+getName(),e);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getProperty(java.lang.String)
	 */
	public synchronized Object getProperty(String key) {
		return getPropertyMap().get(key);
	}

	protected void store() {
		try {
			ConfigurationManager.getInstance().store(this);
		} catch (Exception e) {
			logger.error("There was an error storing the new configuration for: "+this.getName(),e);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#setProperty(java.lang.String, java.lang.Object)
	 */
	public synchronized void setProperty(String key, Object value) {
		Object oldValue = getPropertyMap().get(key);
		if (value==null) {
			deleteProperty(key);
		}
		else {
			getPropertyMap().put(key,value);
		}
		if (value==null || !value.equals(oldValue)) {
			store();
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getPropertyMap()
	 */
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#restoreDefaults()
	 */
	public void restoreDefaults() {
		propertyMap.clear();
		propertyMap.putAll(getDefaultPropertyMap());
		store();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#deleteProperty(java.lang.String)
	 */
	public void deleteProperty(String key) {
		propertyMap.remove(key);
	}

}
