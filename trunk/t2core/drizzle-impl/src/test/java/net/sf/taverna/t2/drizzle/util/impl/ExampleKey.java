/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertyKey;

/**
 * @author alanrw
 *
 */
public class ExampleKey implements PropertyKey, Comparable {
	private static int keyCount = 0;
	
	private int key;
	
	/**
	 * Construct an ExampleKey
	 */
	public ExampleKey() {
		key = keyCount++;
	}
	
	/**
	 * Return the integer key value
	 * 
	 * @return
	 */
	public int getKey() {
		return key;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals (Object o) {
		if (o instanceof ExampleKey) {
			return ((ExampleKey)o).getKey() == key;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o) {
		int result = 0;
		if (o instanceof ExampleKey) {
			ExampleKey exampleArg = (ExampleKey) o;
			result = getKey() - exampleArg.getKey();
		}
		else {
			throw new ClassCastException ("Argument is not an ExampleKey");
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return Integer.toString(getKey());
	}
}
