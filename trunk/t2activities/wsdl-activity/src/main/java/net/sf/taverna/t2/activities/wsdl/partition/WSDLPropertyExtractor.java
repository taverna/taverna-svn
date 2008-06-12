package net.sf.taverna.t2.activities.wsdl.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.wsdl.query.WSDLActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class WSDLPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof WSDLActivityItem) {
			WSDLActivityItem item = (WSDLActivityItem)target;
			map.put("type", item.getType());
			map.put("use", item.getUse());
			map.put("style", item.getStyle());
			map.put("operation", item.getOperation());
			map.put("url",item.getUrl());
		}
		return map;
	}

}
