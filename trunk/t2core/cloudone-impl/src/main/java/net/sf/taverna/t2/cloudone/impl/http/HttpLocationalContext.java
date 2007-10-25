package net.sf.taverna.t2.cloudone.impl.http;

import java.net.URL;
import java.util.Map;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.entity.Entity;
/**
 * Defines the LocationalContext for an {@link Entity} in terms of {@link URL}
 * @author Ian Dunlop
 *
 */
public class HttpLocationalContext implements LocationalContext {

	private Map<String, String> map;
	
	public HttpLocationalContext(Map<String, String> map) {
		//I am not really sure if this is the behaviour!!
		this.map = map;
	}
	
	public String getContextType() {
		return map.get("type");
	}

	public String getValue(String... keyPath) {
		//not sure if it should be an array or what you do with multiple values
		return map.get(keyPath[0]);  //or Exception if null? since there is no keyMap in the list
	}


}
