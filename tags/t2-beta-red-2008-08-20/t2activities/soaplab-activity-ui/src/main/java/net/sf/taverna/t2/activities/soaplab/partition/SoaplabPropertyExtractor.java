package net.sf.taverna.t2.activities.soaplab.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.soaplab.query.SoaplabActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class SoaplabPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof SoaplabActivityItem) {
			SoaplabActivityItem item = (SoaplabActivityItem)target;
			map.put("type", item.getType());
			map.put("category", item.getCategory());
			map.put("operation", item.getOperation());
			map.put("url",item.getUrl());
		}
		return map;
	}

}
