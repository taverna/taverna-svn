/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import java.util.Map;

/**
 * @author alanrw
 *
 */
public final class HashMapEntryBean<K, V> {
    private K key; 
    
    private V value;
    
    /**
	 * @return the key
	 */
	public synchronized final K getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public synchronized final void setKey(K key) {
		this.key = key;
	}
	/**
	 * @return the value
	 */
	public synchronized final V getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public synchronized final void setValue(V value) {
		this.value = value;
	}
	public HashMapEntryBean() {}
    public HashMapEntryBean(Map.Entry<K,V> e) {
       this.key = e.getKey();
       this.value = e.getValue();
    }
}
