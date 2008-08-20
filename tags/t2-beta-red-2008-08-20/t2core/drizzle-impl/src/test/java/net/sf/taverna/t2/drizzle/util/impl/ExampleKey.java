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
	
	/**
	 * Construct an ExampleKey
	 */
	public ExampleKey() {
		this.key = keyCount++;
	}
	
	/**
	 * Return the integer key value
	 * 
	 * @return
	 */
	public int getKey() {
		return this.key;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof ExampleKey) {
			return ((ExampleKey)o).getKey() == this.key;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(PropertyKey o) {
		int result = 0;
		if (o instanceof ExampleKey) {
			ExampleKey exampleArg = (ExampleKey) o;
			result = getKey() - exampleArg.getKey();
		}
		else {
			throw new ClassCastException ("Argument is not an ExampleKey"); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Integer.toString(getKey());
	}

	public PropertyKey getAsBean() {
		return this;
	}

	public void setFromBean(PropertyKey bean) throws IllegalArgumentException {
		if (bean instanceof ExampleKey) {
			//TODO
		} else {
			throw new IllegalArgumentException("ExampleKey expected");
		}
	}
}
