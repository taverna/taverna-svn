package net.sf.taverna.t2.cloudone.peer;

import java.util.Map;

import net.sf.taverna.t2.cloudone.peer.LocationalContext;
/**
 * Implementation of {@link LocationalContext}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class LocationalContextImpl implements LocationalContext {

	private Map<String, String> map;
	private String type;
	
	public LocationalContextImpl(String type, Map<String, String> map) {
		this.type = type;
		this.map = map;
	}
	
	public String getContextType() {
		return type;
	}

	public String getValue(String... keyPath) {
		String key = null;
		for (String keyPart : keyPath) {
			if (keyPart.equals("")) {
				throw new IllegalArgumentException("Key can't be empty");
			}
			if (key == null) {
				key = keyPart;
			} else {
				key = key + "." + keyPart;
			}
		}
		if (key == null) {
			throw new IllegalArgumentException("At least one key is needed");
		}
		return map.get(key);
	}
	
	@Override
	public String toString() {
		// FIXME: Should have some unique identifier instead of printing full map..
		return getClass().getSimpleName() + " (" + getContextType() + "): " + map;
	}


}
