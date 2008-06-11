package net.sf.taverna.t2.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.spi.SPIRegistry;

public class PropertyExtractorSPIRegistry extends SPIRegistry<PropertyExtractorSPI> implements
		PropertyExtractorRegistry {
	
	private static PropertyExtractorSPIRegistry instance = new PropertyExtractorSPIRegistry();


	public static PropertyExtractorSPIRegistry getInstance() {
		return instance;
	}
	private PropertyExtractorSPIRegistry() {
		super(PropertyExtractorSPI.class);
	}

	public Map<String, Object> getAllPropertiesFor(Object target) {
		Map<String,Object> result = new HashMap<String, Object>();
		//TODO:provide caching
		for (PropertyExtractorSPI extractor : getInstances()) {
			result.putAll(extractor.extractProperties(target));
		}
		return result;
	}

}
