package net.sf.taverna.t2.workbench.configuration;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


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
	
	private Map<String,String> propertyMap = new HashMap<String, String>();

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
	public synchronized String getProperty(String key) {
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
	public synchronized void setProperty(String key, String value) {
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
	public Map<String, String> getPropertyMap() {
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
	
	/**
	 * Returns an unmodifiable List<String> for the given key. Internally the value is stored as a single String, but converted to a list when calling this method.
	 * <br>
	 * The list is unmodifiable to prevent the mistake of trying <pre>getPropertyStringList(..).add("new element");</pre> which will not affect the stored
	 * list. For the property to be updated this{@link #setPropertyStringList(String, List)} must be used.
	 */
	public List<String> getPropertyStringList(String key) {
		String value = getProperty(key);
		if (value!=null) {
			return Collections.unmodifiableList(fromListText(value));
		}
		else {
			return null;
		}
	}

	private List<String> fromListText(String property) {
		List<String> result = new ArrayList<String>();
		if (property.length()>0) { //an empty string as assumed to be an empty list, rather than a list with 1 empty string in it!
			StringReader reader = new StringReader(property);
			CSVReader csvReader = new CSVReader(reader);
			try {
				for (String v : csvReader.readNext()) {
					result.add(v);
				}
			} catch (IOException e) {
				logger.error("Exception occurred parsing CSV properties:"+property,e);
			}
		}
		return result;
	}

	/**
	 * Set a value that is known to be a list. The value can be retrieved using this{@link #getPropertyStringList(String)}
	 * <br>
	 * Within the file, the value is stored as a single Comma Separated Value
	 */
	public void setPropertyStringList(String key, List<String> value) {
		setProperty(key, toListText(value));
	}

	private String toListText(List<String> values) {
		StringWriter writer = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(writer);
		csvWriter.writeNext(values.toArray(new String[]{}));
		return writer.getBuffer().toString().trim();
	}

}
