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
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyValue getValue() {
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKey(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException ("key cannot be null"); //$NON-NLS-1$
		}
		if (this.key != null) {
			throw new IllegalStateException ("key cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.key = key;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(PropertyValue value) {
		if (this.value != null) {
			throw new IllegalStateException ("value cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.value = value;
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String keyString = "missing"; //$NON-NLS-1$
		String valueString = "missing"; //$NON-NLS-1$
		
		if (this.key != null) {
			keyString = this.key.toString();
		}
		if (this.value != null ) {
			valueString = this.value.toString();
		}
		
		return keyString + "=" +valueString; //$NON-NLS-1$
	}
}
