package net.sf.taverna.t2.activities.beanshell.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.beanshell.query.BeanshellActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class BeanshellPropertyExtractorSPI implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof BeanshellActivityItem) {
			BeanshellActivityItem item = (BeanshellActivityItem)target;
			map.put("type", item.getType());
		}
		return map;
	}

}
