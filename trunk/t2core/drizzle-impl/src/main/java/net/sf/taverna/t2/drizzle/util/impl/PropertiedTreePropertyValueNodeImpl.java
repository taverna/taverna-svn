/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 * @param <O> The class of object to which the property key + value pair apply.
 */
public final class PropertiedTreePropertyValueNodeImpl<O> extends PropertiedTreeNodeImpl<O> implements
		PropertiedTreePropertyValueNode<O> {
	
	private PropertyKey key;
	private PropertyValue value;

	/**
	 * 
	 */
	public PropertiedTreePropertyValueNodeImpl() {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyKey getKey() {
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyValue getValue() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKey(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException ("key cannot be null");
		}
		if (this.key != null) {
			throw new IllegalStateException ("key cannot be initialized more than once");
		}
		this.key = key;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(PropertyValue value) {
		if (this.value != null) {
			throw new IllegalStateException ("value cannot be initialized more than once");
		}
		this.value = value;
		
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		String keyString = "missing";
		
		if (key != null) {
			keyString = key.toString();
		}
		String valueString = "missing";
		if (value != null ) {
			valueString = value.toString();
		}
		
		return keyString + " = " +valueString;
	}
}
