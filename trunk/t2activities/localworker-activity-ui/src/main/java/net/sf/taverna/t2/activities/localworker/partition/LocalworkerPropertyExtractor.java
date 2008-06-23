package net.sf.taverna.t2.activities.localworker.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.localworker.query.LocalworkerActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

/**
 * States what type of "things" a Local Worker can be queried for in the
 * Activity Tree. In this case it is "type" and "operation" - which return
 * "Localworker" and "actual-localworker-activity-type"
 * 
 * @author Ian Dunlop
 * 
 */
public class LocalworkerPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (target instanceof LocalworkerActivityItem) {
			LocalworkerActivityItem item = (LocalworkerActivityItem) target;
			map.put("type", item.getType());
			map.put("operation", item.getOperation());
		}
		return map;
	}

}
