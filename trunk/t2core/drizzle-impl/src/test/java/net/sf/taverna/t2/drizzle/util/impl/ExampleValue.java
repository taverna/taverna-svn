/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public class ExampleValue implements PropertyValue {
	private static int valueCount = 0;
	
	private int value;
	
	public ExampleValue() {
		value = valueCount++;
	}
	
	public int getValue() {
		return value;
	}
	
	public boolean equals (Object o) {
		if (o instanceof ExampleValue) {
			return ((ExampleValue)o).getValue() == value;
		} else {
			return false;
		}
	}
}
