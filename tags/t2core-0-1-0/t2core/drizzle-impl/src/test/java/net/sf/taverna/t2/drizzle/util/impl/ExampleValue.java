/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public class ExampleValue implements PropertyValue, Comparable<Object> {
	private static int valueCount = 0;
	
	private int value;
	
	/**
	 * Construct a new ExampleValue
	 */
	public ExampleValue() {
		this.value = valueCount++;
	}
	
	/**
	 * Return the integer identifying the ExampleValue
	 * 
	 * @return
	 */
	public int getValue() {
		return this.value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof ExampleValue) {
			return ((ExampleValue)o).getValue() == this.value;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object arg0) {
		int result = 0;
		if (arg0 instanceof ExampleValue) {
			ExampleValue exampleArg = (ExampleValue) arg0;
			result = getValue() - exampleArg.getValue();
		}
		else {
			throw new ClassCastException ("Argument is not an ExampleValue"); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Integer.toString(getValue());
	}
}
