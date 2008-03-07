/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement()
public class PropertiedObjectBean {
	private HashMapBean<PropertyKey, PropertyValue> properties;

	/**
	 * @return the properties
	 */
	@XmlAnyElement
	public HashMapBean<PropertyKey, PropertyValue> getProperties() {
		return this.properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(
			HashMapBean<PropertyKey, PropertyValue> properties) {
		this.properties = properties;
	}

}
