package net.sf.taverna.t2.activities.dataflow.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.dataflow.query.DataflowActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class DataflowPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof DataflowActivityItem) {
			DataflowActivityItem item = (DataflowActivityItem)target;
			map.put("type", item.getType());
		}
		return map;
	}

}
