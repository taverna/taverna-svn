/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import java.util.Map;
import java.util.WeakHashMap;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class ComponentDataflowCache {
	
	private static Map<ComponentVersionIdentification, Dataflow> cache = new WeakHashMap<ComponentVersionIdentification, Dataflow>();
	
	private ComponentDataflowCache() {
		
	}
	
	public static Dataflow getDataflow(ComponentVersionIdentification id) throws ComponentRegistryException {
//		if (!cache.containsKey(id)) {
			Dataflow dataflow = ComponentUtil.calculateComponentVersion(id).getDataflow();
			dataflow.checkValidity();
			cache.put(id, dataflow);
//		}
		return cache.get(id);
	}

}
