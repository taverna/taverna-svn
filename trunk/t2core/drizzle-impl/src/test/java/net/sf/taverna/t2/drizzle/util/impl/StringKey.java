/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertyKey;

/**
 * @author alanrw
 *
 */
public final class StringKey implements PropertyKey, Comparable {

	private String key;
	
	public StringKey(final String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public boolean equals (Object o) {
		if (o instanceof StringKey) {
			return ((StringKey)o).getKey().equals(key);
		} else {
			return false;
		}
	}
	
	public int compareTo(Object o) {
		int result = 0;
		if (o instanceof StringKey) {
			StringKey stringArg = (StringKey) o;
			result = getKey().compareTo(stringArg.getKey());
		}
		else {
			throw new ClassCastException ("Argument is not a StringKey");
		}
		return result;
	}
	
	public String toString() {
		return getKey();
	}
}
