/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import java.util.HashMap;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * It is deliberate that the object to which the beaned PropertiedObject
 * corresponds is not visible. This is because PropertiedObjectBean should only
 * be visible under PropertiedObjectSetBean. It is only public because it is
 * seen by PropertiedObject.
 * 
 * @author alanrw
 * 
 */
public class PropertiedObjectBean {
	private HashMap<PropertyKey, PropertyValue> properties;

	/**
	 * @return the properties
	 */
	public HashMap<PropertyKey, PropertyValue> getProperties() {
		return this.properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(
			HashMap<PropertyKey, PropertyValue> properties) {
		this.properties = properties;
	}

}
