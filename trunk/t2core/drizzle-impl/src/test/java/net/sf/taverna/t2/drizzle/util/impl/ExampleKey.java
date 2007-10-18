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
	
	public ExampleKey() {
		key = keyCount++;
	}
	
	public int getKey() {
		return key;
	}
	
	public boolean equals (Object o) {
		if (o instanceof ExampleKey) {
			return ((ExampleKey)o).getKey() == key;
		} else {
			return false;
		}
	}

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
	
	public String toString() {
		return Integer.toString(getKey());
	}
}
