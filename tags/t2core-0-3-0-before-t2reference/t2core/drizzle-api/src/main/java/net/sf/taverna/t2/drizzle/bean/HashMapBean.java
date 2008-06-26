/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author alanrw
 *
 */
public class HashMapBean<K,V> {
	private List<HashMapEntryBean<K,V>> entry = new ArrayList<HashMapEntryBean<K,V>>();
    public HashMapBean (Map<K,V> map) {
        for( Map.Entry<K,V> e : map.entrySet() )
            this.entry.add(new HashMapEntryBean<K,V>(e));
    }
    public HashMapBean() {
    	
    }
	/**
	 * @return the entry
	 */
	public synchronized final List<HashMapEntryBean<K, V>> getEntry() {
		return this.entry;
	}
	/**
	 * @param entry the entry to set
	 */
	public synchronized final void setEntry(List<HashMapEntryBean<K, V>> entry) {
		this.entry = entry;
	}
}
