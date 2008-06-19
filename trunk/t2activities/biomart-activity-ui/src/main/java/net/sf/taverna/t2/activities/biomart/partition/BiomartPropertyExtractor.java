package net.sf.taverna.t2.activities.biomart.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.biomart.query.BiomartActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class BiomartPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof BiomartActivityItem) {
			BiomartActivityItem item = (BiomartActivityItem)target;
			map.put("type", item.getType());
			map.put("dataset", item.getDataset());
			map.put("url",item.getUrl());
			map.put("location", item.getLocation());
		}
		return map;
	}

}
