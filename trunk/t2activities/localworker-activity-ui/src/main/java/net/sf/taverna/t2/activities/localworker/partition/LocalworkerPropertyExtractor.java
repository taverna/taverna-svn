package net.sf.taverna.t2.activities.localworker.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.localworker.query.LocalworkerActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class LocalworkerPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof LocalworkerActivityItem) {
			LocalworkerActivityItem item = (LocalworkerActivityItem)target;
			map.put("type", item.getType());
		}
		return map;
	}

}
