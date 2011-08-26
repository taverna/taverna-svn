package net.sf.taverna.t2.partition;

import java.util.HashMap;
import java.util.Map;

public class DummyExtractor2 implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> result = new HashMap<String, Object>();
		if (target instanceof String) {
			result.put("james", "brown");
		}
		return result;
	}

}
