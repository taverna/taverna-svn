package net.sf.taverna.t2.matlabactivity.partition;

import java.util.HashMap;
import java.util.Map;
import net.sf.taverna.t2.matlabactivity.query.MatActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

/**
 *
 * @author petarj
 */
public class MatPluginPropertyExtractorSPI implements PropertyExtractorSPI {

    public Map<String, Object> extractProperties(Object target) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (target instanceof MatActivityItem) {
            MatActivityItem item = (MatActivityItem) target;
            map.put("type", item.getType());
        }
        return map;
    }
}
