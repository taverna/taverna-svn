package net.sf.taverna.t2.activities.biomoby.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.biomoby.query.BiomobyActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class BiomobyPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof BiomobyActivityItem) {
			BiomobyActivityItem item = (BiomobyActivityItem)target;
			map.put("type", item.getType());
		}
		return map;
	}

}
