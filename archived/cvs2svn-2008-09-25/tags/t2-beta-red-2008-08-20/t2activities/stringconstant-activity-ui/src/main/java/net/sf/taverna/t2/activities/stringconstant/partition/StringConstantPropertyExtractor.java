package net.sf.taverna.t2.activities.stringconstant.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.stringconstant.query.StringConstantActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class StringConstantPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof StringConstantActivityItem) {
			StringConstantActivityItem item = (StringConstantActivityItem)target;
			map.put("type", item.getType());
		}
		return map;
	}

}
