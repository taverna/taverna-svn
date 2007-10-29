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
	
	/**
	 * Construct a new StringValue
	 * 
	 * @param value
	 */
	public StringValue(final String value) {
		this.value = value;
	}
	
	/**
	 * Return the String that identifies the StringValue
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals (Object o) {
		if (o instanceof StringValue) {
			return ((StringValue)o).getValue().equals(value);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
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
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return getValue();
	}
}
