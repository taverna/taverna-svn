package net.sf.taverna.t2.partition;

import java.util.Map;

public interface PropertyExtractorRegistry {

	public Map<String, Object> getAllPropertiesFor(Object target);
	
}
