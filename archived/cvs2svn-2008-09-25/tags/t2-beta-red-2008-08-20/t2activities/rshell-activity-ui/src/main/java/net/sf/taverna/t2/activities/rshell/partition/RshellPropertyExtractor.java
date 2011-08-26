package net.sf.taverna.t2.activities.rshell.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.rshell.query.RshellActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class RshellPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof RshellActivityItem) {
			RshellActivityItem item = (RshellActivityItem)target;
			map.put("type", item.getType());
		}
		return map;
	}

}
