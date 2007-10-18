/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.Comparator;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public final class PropertyKeySettingImpl implements PropertyKeySetting {
	
	private PropertyKey key;
	
	private Comparator<PropertyValue> comparator;

	/**
	 * 
	 */
	public PropertyKeySettingImpl() {
	}

	public Comparator<PropertyValue> getComparator() {
		return comparator;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertyKeySetting#getPropertyKey()
	 */
	public PropertyKey getPropertyKey() {
		return this.key;
	}

	public void setComparator(Comparator<PropertyValue> comparator) {
		if (comparator == null) {
			throw new NullPointerException ("comparator cannot be null");
		}
		if (this.comparator != null) {
			throw new IllegalStateException ("comparator cannot be initialized more than once");
		}
		this.comparator = comparator;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertyKeySetting#setPropertyKey(net.sf.taverna.t2.drizzle.util.PropertyKey)
	 */
	public void setPropertyKey(final PropertyKey propertyKey) {
		if (propertyKey == null) {
			throw new NullPointerException ("propertyKey cannot be null");
		}
		if (this.key != null) {
			throw new IllegalStateException ("key cannot be initialized more than once");
		}
		this.key = propertyKey;

	}

}
