/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public class ExampleValue implements PropertyValue, Comparable {
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

	public int compareTo(Object arg0) {
		int result = 0;
		if (arg0 instanceof ExampleValue) {
			ExampleValue exampleArg = (ExampleValue) arg0;
			result = getValue() - exampleArg.getValue();
		}
		else {
			throw new ClassCastException ("Argument is not an ExampleValue");
		}
		return result;
	}
	
	public String toString() {
		return Integer.toString(getValue());
	}
}
