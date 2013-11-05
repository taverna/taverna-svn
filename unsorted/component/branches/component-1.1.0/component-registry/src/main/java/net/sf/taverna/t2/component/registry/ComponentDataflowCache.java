/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateComponentVersion;
import static org.apache.log4j.Logger.getLogger;

import java.util.Map;
import java.util.WeakHashMap;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentDataflowCache {
	private static final Logger logger = getLogger(ComponentDataflowCache.class);
	private static final Map<Version.ID, Dataflow> cache = new WeakHashMap<Version.ID, Dataflow>();

	private ComponentDataflowCache() {
	}

	public static Dataflow getDataflow(Version.ID id) throws RegistryException {
		synchronized (id) {
			if (!cache.containsKey(id)) {
				logger.info("Before Calculate component version");
				Version componentVersion;
				try {
					componentVersion = calculateComponentVersion(id);
				} catch (Exception e) {
					throw new RegistryException(e.getMessage(), e);
				}
				logger.info("Calculated component version");
				Dataflow dataflow = componentVersion.getDataflow();
				dataflow.checkValidity();
				cache.put(id, dataflow);
			}
		}
		return cache.get(id);
	}

}
