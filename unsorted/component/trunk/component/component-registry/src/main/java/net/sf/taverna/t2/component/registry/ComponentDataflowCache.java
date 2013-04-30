/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import java.util.Map;
import java.util.WeakHashMap;

import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentDataflowCache {
	
	private static Logger logger = Logger.getLogger(ComponentDataflowCache.class);
	
	private static Map<ComponentVersionIdentification, Dataflow> cache = new WeakHashMap<ComponentVersionIdentification, Dataflow>();
	
	private ComponentDataflowCache() {
		
	}
	
	public static Dataflow getDataflow(ComponentVersionIdentification id) throws ComponentRegistryException {
//		if (!cache.containsKey(id)) {
		logger.info("Before Calculate component version");
			ComponentVersion componentVersion;
			try {
			componentVersion = ComponentUtil.calculateComponentVersion(id);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ComponentRegistryException (e.getMessage(), e);
			}
			logger.info("Calculated component version");
			Dataflow dataflow = componentVersion.getDataflow();
			dataflow.checkValidity();
			cache.put(id, dataflow);
//		}
		return cache.get(id);
	}

}
