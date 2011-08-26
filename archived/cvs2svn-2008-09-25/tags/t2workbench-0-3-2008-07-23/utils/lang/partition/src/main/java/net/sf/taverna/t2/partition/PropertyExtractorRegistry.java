package net.sf.taverna.t2.partition;

import java.util.Map;

/**
 * Convenience to allow caching of property extractors. Implementations should
 * scan for available PropertyExtractorSPI implementations and use these to get
 * the properties for each target, caching as applicable.
 * 
 * @author Tom Oinn
 * 
 */
public interface PropertyExtractorRegistry {

	public Map<String, Object> getAllPropertiesFor(Object target);

}
