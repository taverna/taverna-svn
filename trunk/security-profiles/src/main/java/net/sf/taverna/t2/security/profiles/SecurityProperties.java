package net.sf.taverna.t2.security.profiles;

import java.util.HashMap;

/**
 * A generic container for various security properties of a service.
 * 
 * @author Alexandra Nenadic
 *
 */
public class SecurityProperties {
	
	/* True/false constants */
	public static final String TRUE = "True";
	public static final String FALSE = "False";

	/** A collection of various security properties */
	private HashMap<String,String> properties;
	
	// Constructor that creates an empty properties collection
	public SecurityProperties(){
		properties = new HashMap<String,String>();
	}
	
	public void setProperty(String property, String value) {
			properties.put(property, value);
	}
	
	public String getProperty(String property) throws NoSuchSecurityPropertyException{
		if (!properties.keySet().contains(property)){
			throw new NoSuchSecurityPropertyException();
		}
		else
			return properties.get(property);
	}
	
	public void setProperties(HashMap<String,String> pr) {
		properties = pr;
	}
	
	public HashMap<String,String> getProperties(){
		return properties;
	}
}
