package net.sf.taverna.t2.partition;

import java.util.Map;

/**
 * SPI for classes which can extract or infer a set of named properties from a
 * target object.
 * 
 * @author Tom Oinn
 * 
 */
public interface PropertyExtractorSPI {

	/**
	 * Given a target object extract or infer the property map from it. If the
	 * target is one which this plugin cannot act on then simply return an empty
	 * map.
	 * 
	 * @param target
	 * @return
	 */
	Map<String, Object> extractProperties(Object target);
	
}
