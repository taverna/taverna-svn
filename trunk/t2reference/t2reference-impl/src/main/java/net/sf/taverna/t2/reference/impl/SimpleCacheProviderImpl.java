package net.sf.taverna.t2.reference.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceServiceCacheProvider;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Completely naive cache provider that just stores everything in a map. This
 * <em>will</em> run out of memory as it makes no attempt to evict old items,
 * it's really just here as a test!
 * 
 * @author Tom Oinn
 * 
 */
public class SimpleCacheProviderImpl implements ReferenceServiceCacheProvider {

	private final Log log = LogFactory.getLog(SimpleCacheProviderImpl.class);

	private Map<T2Reference, Identified> cache = new HashMap<T2Reference, Identified>();

	public Identified get(T2Reference id) {
		log.debug("Get " + id.toString() + " (" + cache.containsKey(id) + ")");
		return cache.get(id);
	}

	public void put(Identified i) {
		log.debug("Put " + i.getId().toString());
		cache.put(i.getId(), i);
	}

}
