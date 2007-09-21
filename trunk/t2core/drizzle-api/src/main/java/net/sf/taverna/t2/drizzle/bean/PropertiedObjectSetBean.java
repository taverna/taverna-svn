/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import java.util.HashMap;

/**
 * This should really deal with ObjectBean rather than Object. It is not clear
 * how this should be done.
 * 
 * @author alanrw
 * 
 */
public class PropertiedObjectSetBean <O>{
	HashMap<O, PropertiedObjectBean> propertiedObjectMap;
	
	public PropertiedObjectSetBean () {
		super();
		propertiedObjectMap = new HashMap<O, PropertiedObjectBean>();
	}

	/**
	 * @return the propertiedObjectMap
	 */
	public HashMap<O, PropertiedObjectBean> getPropertiedObjectMap() {
		return propertiedObjectMap;
	}

	/**
	 * @param propertiedObjectMap the propertiedObjectMap to set
	 */
	public void setPropertiedObjectMap(
			HashMap<O, PropertiedObjectBean> propertiedObjectMap) {
		this.propertiedObjectMap = propertiedObjectMap;
	}

}
