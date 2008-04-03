/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

/**
 * This should really deal with ObjectBean rather than Object. It is not clear
 * how this should be done.
 * 
 * @author alanrw
 * 
 */
/**
 * @author alanrw
 *
 * @param <O> The object class that can be contained by the PropertiedObjectSet
 */
public class PropertiedObjectSetBean <O>{
	HashMapBean<Object, PropertiedObjectBean> propertiedObjectMap;
	
	/**
	 * 
	 */
	public PropertiedObjectSetBean () {
		super();
		this.propertiedObjectMap = new HashMapBean<Object, PropertiedObjectBean>();
	}

	/**
	 * @return the propertiedObjectMap
	 */
	public HashMapBean<Object, PropertiedObjectBean> getPropertiedObjectMap() {
		return this.propertiedObjectMap;
	}

	/**
	 * @param propertiedObjectMap the propertiedObjectMap to set
	 */
	public void setPropertiedObjectMap(
			HashMapBean<Object, PropertiedObjectBean> propertiedObjectMap) {
		this.propertiedObjectMap = propertiedObjectMap;
	}

}
