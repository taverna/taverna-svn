/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertyKey;

/**
 * @author alanrw
 *
 */
public class ExampleKey implements PropertyKey {
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
}
