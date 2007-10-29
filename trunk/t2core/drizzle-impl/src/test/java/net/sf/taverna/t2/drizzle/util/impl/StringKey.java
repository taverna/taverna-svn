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
	
	/**
	 * Construct a new StringKey
	 * 
	 * @param key
	 */
	public StringKey(final String key) {
		this.key = key;
	}
	
	/**
	 * Return the String that identifies the StringKey
	 * 
	 * @return
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals (Object o) {
		if (o instanceof StringKey) {
			return ((StringKey)o).getKey().equals(key);
		} else {
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
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
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return getKey();
	}
}
