package net.sf.taverna.t2.activities.ncbi.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.ncbi.query.NCBIActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class NCBIPropertyExtractor implements PropertyExtractorSPI {
	/**
	 * {@link NCBIActivityItem}s can be queried by type, wsdl url or category
	 */
	public Map<String, Object> extractProperties(Object target) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (target instanceof NCBIActivityItem) {
			NCBIActivityItem item = (NCBIActivityItem) target;
			map.put("type", item.getType());
			map.put("url", item.getUrl());
			map.put("category", item.getCategory());
		}
		return map;
	}
}
