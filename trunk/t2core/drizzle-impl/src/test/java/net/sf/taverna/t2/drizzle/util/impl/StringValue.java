/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public class StringValue implements PropertyValue, Comparable {
	private String value;
	
	public StringValue(final String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean equals (Object o) {
		if (o instanceof StringValue) {
			return ((StringValue)o).getValue().equals(value);
		} else {
			return false;
		}
	}

	public int compareTo(Object arg0) {
		int result = 0;
		if (arg0 instanceof StringValue) {
			StringValue exampleArg = (StringValue) arg0;
			result = getValue().compareTo(exampleArg.getValue());
		}
		else {
			throw new ClassCastException ("Argument is not a StringValue");
		}
		return result;
	}
	
	public String toString() {
		return getValue();
	}
}
